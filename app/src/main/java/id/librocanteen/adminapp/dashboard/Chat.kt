package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Chat : Fragment() {

    private lateinit var chatTitle: TextView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageRecyclerViewAdapter: MessageRecyclerViewAdapter

    private lateinit var currentChannelId: String
    private val database = FirebaseDatabase.getInstance()
    private val channelsRef = database.reference.child("channels")

    companion object {
        fun newInstance(channelId: String): Chat {
            val fragment = Chat()
            val args = Bundle()
            args.putString("CHANNEL_ID", channelId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        chatTitle = view.findViewById(R.id.senderName)
        messageInput = view.findViewById(R.id.messageInput)
        sendButton = view.findViewById(R.id.sendButton)
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView)

        currentChannelId = arguments?.getString("CHANNEL_ID") ?: ""

        channelsRef.child(currentChannelId).child("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                val uid =
                    users.firstOrNull { it != "Admin" } // Exclude "Admin" and get the other user
                chatTitle.text = uid
            }

        chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        messageRecyclerViewAdapter = MessageRecyclerViewAdapter(::formatDatetimeForDisplay)
        chatRecyclerView.adapter = messageRecyclerViewAdapter

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "Message cannot be empty", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        fetchMessages()
    }

    private fun fetchMessages() {
        channelsRef.child(currentChannelId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
                    messageRecyclerViewAdapter.submitList(messages)
                    chatRecyclerView.scrollToPosition(messages.size - 1) // Auto-scroll to latest message
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatFragment", "Error fetching messages: ${error.message}")
                }
            })
    }

    private fun sendMessage(content: String) {
        channelsRef.child(currentChannelId).child("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                val uid =
                    users.firstOrNull { it != "Admin" } // Exclude "Admin" and get the other user
                chatTitle.text = uid

                if (uid != null) {
                    // Create the message object
                    val message = Message(
                        content = content,
                        datetime = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            Locale.getDefault()
                        ).format(Date()),
                        sender = "Admin",
                        receiver = uid
                    )

                    // Push the message to the Firebase database
                    val newMessageRef = channelsRef.child(currentChannelId).child("messages").push()
                    newMessageRef.setValue(message).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("ChatFragment", "Message sent successfully!")
                        } else {
                            Log.e(
                                "ChatFragment",
                                "Error sending message: ${task.exception?.message}"
                            )
                        }
                    }
                } else {
                    Log.e("ChatFragment", "No valid recipient found in the channel")
                }
            }
            .addOnFailureListener { error ->
                Log.e("ChatFragment", "Error fetching channel users: ${error.message}")
            }
        messageRecyclerViewAdapter.submitList(emptyList()) // Clear the list to trigger refresh
    }


    private fun formatDatetimeForDisplay(datetime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            val date = inputFormat.parse(datetime)

            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            outputFormat.format(date ?: datetime)
        } catch (e: Exception) {
            Log.e("ChatFragment", "Error formatting datetime: ${e.message}")
            datetime // Return original string if formatting fails
        }
    }
}