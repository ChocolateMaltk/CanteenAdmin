package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import id.librocanteen.adminapp.databinding.FragmentChannelListBinding

class ChannelList : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var channelAdapter: ChannelRecyclerViewAdapter
    private val usersReference = FirebaseDatabase.getInstance().reference.child("users")

    // ViewBinding reference
    private var _binding: FragmentChannelListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the binding
        _binding = FragmentChannelListBinding.inflate(inflater, container, false)

        // Setup RecyclerView using the binding
        recyclerView = binding.channelListRecyclerView
        channelAdapter = ChannelRecyclerViewAdapter(usersReference)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = channelAdapter

        // Fetch channels dynamically from Firebase
        channelAdapter.fetchChannels()

        // Return the root view of the fragment
        return binding.root
    }

    // Cleanup binding reference when view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


