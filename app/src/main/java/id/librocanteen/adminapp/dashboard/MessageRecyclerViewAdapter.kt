package id.librocanteen.adminapp.dashboard


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Message

class MessageRecyclerViewAdapter(private val formatDatetime: (String) -> String) :
    RecyclerView.Adapter<MessageRecyclerViewAdapter.MessageViewHolder>() {
    private val messages = mutableListOf<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, formatDatetime)
    }

    override fun getItemCount(): Int = messages.size

    fun submitList(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val datetimeTextView: TextView =
            itemView.findViewById(R.id.messageSentDatetimeTextView)
        private val messageContainer: View = itemView.findViewById(R.id.messageContainer) // Parent container for the message

        fun bind(message: Message, formatDatetime: (String) -> String) {
            messageTextView.text = message.content
            datetimeTextView.text = formatDatetime(message.datetime)

            // Change background color if sender is "Admin"
            if (message.sender == "Admin") {
                messageContainer.setBackgroundResource(R.drawable.light_lime_green_bubble_background)
            } else {
                messageContainer.setBackgroundResource(R.drawable.message_bubble_background)
            }
        }
    }
}

