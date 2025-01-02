package id.librocanteen.adminapp.dashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Channel
import java.text.SimpleDateFormat
import java.util.*

class ChannelRecyclerViewAdapter(
    private val usersReference: DatabaseReference
) : RecyclerView.Adapter<ChannelRecyclerViewAdapter.ChannelViewHolder>() {

    private var channels: MutableList<Channel> = mutableListOf()

    // Function to fetch all channels dynamically
    fun fetchChannels() {
        val channelsReference = FirebaseDatabase.getInstance().reference.child("channels")

        Log.d("ChannelAdapter", "Fetching channels from Firebase...")

        channelsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ChannelAdapter", "Data fetched successfully.")

                val channelList = mutableListOf<Channel>()

                // Loop through each channel node
                for (channelSnapshot in snapshot.children) {
                    val channelName = channelSnapshot.key ?: continue
                    val users = mutableListOf<String>()

                    // Retrieve the users for the current channel
                    for (userSnapshot in channelSnapshot.child("users").children) {
                        val userId = userSnapshot.key ?: continue
                        users.add(userId)
                    }

                    Log.d("ChannelAdapter", "Channel found: $channelName with users: $users")

                    val channel = Channel(channelName, users)
                    channelList.add(channel)
                }

                // Set the channels list and notify the adapter of the data change
                channels.clear() // Clear existing data before adding new data
                channels.addAll(channelList)
                Log.d("ChannelAdapter", "Total channels retrieved: ${channelList.size}")
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChannelAdapter", "Error fetching channels: ${error.message}")
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return ChannelViewHolder(view)
    }

    // Function to fetch the username by using the custom user ID
    private fun fetchUserNameByUserId(userId: String, holder: ChannelViewHolder) {
        val usersReference = FirebaseDatabase.getInstance().reference.child("users")

        usersReference.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").getValue(String::class.java)
                    holder.userNameTextView.text = userName ?: "Unknown User"
                    Log.d("ChannelAdapter", "User name fetched: $userName")
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.userNameTextView.text = "Error fetching user."
                    Log.e("ChannelAdapter", "Error fetching user name: ${error.message}")
                }
            })
    }

    // Mapping function to fetch the custom user ID from the firebase UID
    private fun fetchCustomUserId(firebaseUid: String, holder: ChannelViewHolder) {
        val userIdMappingRef = FirebaseDatabase.getInstance().reference.child("userIdMapping")

        userIdMappingRef.child(firebaseUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val customUserId = snapshot.getValue(String::class.java)
                    if (customUserId != null) {
                        // If customUserId is found, use it to fetch user details
                        fetchUserNameByUserId(customUserId, holder)
                    } else {
                        holder.userNameTextView.text = "User ID not found"
                        Log.e("ChannelAdapter", "Custom userId not found for Firebase UID: $firebaseUid")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.userNameTextView.text = "Error fetching user."
                    Log.e("ChannelAdapter", "Error fetching userIdMapping: ${error.message}")
                }
            })
    }

    // Modify the onBindViewHolder to incorporate the mapping process
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]

        Log.d("ChannelAdapter", "Binding view for channel: ${channel.channelName}")

        // Fetch the last message content and timestamp for the channel
        fetchLastMessageDetails(channel, holder)
    }

    private fun fetchLastMessageDetails(channel: Channel, holder: ChannelViewHolder) {
        val messagesReference = FirebaseDatabase.getInstance().reference.child("channels")
            .child(channel.channelName).child("messages")

        Log.d("ChannelAdapter", "Fetching last message for channel: ${channel.channelName}")

        messagesReference.limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastMessage = snapshot.children.firstOrNull()
                    if (lastMessage != null) {
                        val messageContent =
                            lastMessage.child("content").getValue(String::class.java)
                                ?: "No message"
                        val timestamp =
                            lastMessage.child("datetime").getValue(String::class.java) ?: "0"
                        val senderId =
                            lastMessage.child("sender").getValue(String::class.java) ?: ""

                        Log.d("ChannelAdapter", "Last message fetched: $messageContent, timestamp: $timestamp")

                        // Fetch sender name (map UID to custom userId, then fetch user details)
                        if (senderId.isNotEmpty()) {
                            fetchCustomUserId(senderId, holder) // Map and fetch sender's name
                        }

                        // Parse the ISO 8601 datetime string for message timestamp
                        val inputFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                        inputFormat.timeZone =
                            TimeZone.getTimeZone("UTC") // Set UTC time zone for parsing

                        try {
                            val date = inputFormat.parse(timestamp)

                            // Format the parsed date to your desired format (Asia/Jakarta timezone)
                            val outputFormat =
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
                            outputFormat.timeZone =
                                TimeZone.getTimeZone("Asia/Jakarta") // Convert to Asia/Jakarta timezone
                            val formattedDate =
                                outputFormat.format(date ?: Date()) // Format the date

                            holder.messageContentTextView.text = messageContent
                            holder.messageDatetimeTextView.text = formattedDate
                            Log.d("ChannelAdapter", "Formatted timestamp: $formattedDate")
                        } catch (e: Exception) {
                            Log.e("ChannelAdapter", "Error parsing timestamp: ${e.message}")
                            holder.messageDatetimeTextView.text = "Invalid Date"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    holder.messageContentTextView.text = "Error fetching message"
                    holder.messageDatetimeTextView.text = "Error"
                    Log.e("ChannelAdapter", "Error fetching message: ${error.message}")
                }
            })
    }


    override fun getItemCount(): Int = channels.size

    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameTextView: TextView = view.findViewById(R.id.userName)
        val messageContentTextView: TextView = view.findViewById(R.id.messageContent)
        val messageDatetimeTextView: TextView = view.findViewById(R.id.messageDatetime)
    }
}



