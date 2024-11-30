package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.MenuItem
import id.librocanteen.adminapp.dashboard.objects.Vendor

class VendorListFragment : Fragment() {

    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorRecyclerViewAdapter
    private val vendorList: MutableList<Vendor> = mutableListOf()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        val view = inflater.inflate(R.layout.fragment_vendor_list, container, false)

        // Initialize RecyclerView and Adapter
        vendorRecyclerView = view.findViewById(R.id.vendorList) // RecyclerView in XML layout
        vendorRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2) // Grid with 2 columns
        vendorAdapter = VendorRecyclerViewAdapter(requireContext(), vendorList) // what?
        vendorRecyclerView.adapter = vendorAdapter

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase database reference
        fetchVendors()
    }

    private fun fetchVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    createInitialVendors()
                } else {
                    loadVendors()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                Log.e("VendorListFragment", "Database Error vendors: ${error.message}")
            }
        })
    }

    private fun createInitialVendors() {
        val vendorsRef = database.reference.child("vendors")

        val initialVendors = listOf<Vendor>(
            Vendor(
                vendorNumber = 1,
                name = "Mukidin Bakery",
                standNumber = 1,
                description = "Fresh baked goods and pastries",
                profilePictureURL = "",
                bannerPictureURL = "",
                menuItems = mutableListOf(
                    MenuItem(itemName = "Croissant", itemDescription = "Buttery and flaky", itemPrice = 5000)
                )
            )
        )

        initialVendors.forEach{ vendor ->
            vendorsRef.push().setValue(vendor)
                .addOnSuccessListener{
                    Log.d("VendorListFragment", "Vendor ${vendor.name} added successfully!")
                    loadVendors()
                }
                .addOnFailureListener{
                    Log.e("VendorListFragment", "Failed to add vendor ${vendor.name}: ${it.message}")
                }
        }
    }

    private fun loadVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vendorList.clear()
                for (childSnapshot in snapshot.children) {
                    val vendor = childSnapshot.getValue<Vendor>()
                    vendor?.let { vendorList.add(it)}
                }
                vendorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("VendorListFragment", "Database Error vendors: ${error.message}")
            }
        })
    }
}
