package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.MenuItem
import id.librocanteen.adminapp.dashboard.objects.Vendor
import id.librocanteen.adminapp.Settings

class VendorListFragment : Fragment() {

    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorRecyclerViewAdapter
    private lateinit var navController: NavController
    private val vendorList: MutableList<Vendor> = mutableListOf()
    private val database = FirebaseDatabase.getInstance()
    private val settings = Settings()
    var vendorCounter = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        navController = findNavController()
        val view = inflater.inflate(R.layout.fragment_vendor_list, container, false)

        // Initialize RecyclerView and Adapter
        vendorRecyclerView = view.findViewById(R.id.vendorList) // RecyclerView in XML layout
        vendorRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 2) // Grid with 2 columns
        vendorAdapter = VendorRecyclerViewAdapter(requireContext(), vendorList, this, navController)
        vendorRecyclerView.adapter = vendorAdapter

        val vendorAddButton: FloatingActionButton = view.findViewById(R.id.addNewVendorFAB)
        vendorAddButton.setOnClickListener {
            vendorAddButton.elevation = 8f
            checkVendorListAndShowDialog()
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchVendors()
    }

    private fun fetchVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(
                        requireContext(),
                        "No vendors found. Creating a dummy vendor.",
                        Toast.LENGTH_SHORT
                    ).show()
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

    // Function you call when there are no initial vendors
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
                    MenuItem(
                        itemName = "Croissant",
                        itemDescription = "Buttery and flaky",
                        itemPrice = 5000
                    )
                )
            )
        )

        initialVendors.forEach { vendor ->
            vendorsRef.push().setValue(vendor)
                .addOnSuccessListener {
                    Log.d("VendorListFragment", "Vendor ${vendor.name} added successfully!")
                    loadVendors()
                }
                .addOnFailureListener {
                    Log.e(
                        "VendorListFragment",
                        "Failed to add vendor ${vendor.name}: ${it.message}"
                    )
                }
        }
    }

    fun loadVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                vendorList.clear()
                for (childSnapshot in snapshot.children) {
                    val vendor = childSnapshot.getValue<Vendor>()
                    vendor?.let { vendorList.add(it) }
                }
                vendorList.sortBy { it.vendorNumber }
                vendorAdapter.notifyDataSetChanged()
                vendorCounter = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("VendorListFragment", "Database Error vendors: ${error.message}")
            }
        })
    }

    private fun checkVendorListAndShowDialog() {
        val vendorsRef = database.reference.child("vendors")
        vendorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentVendors = snapshot.childrenCount.toInt()

                if (currentVendors >= settings.maxVendors) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum number of vendors reached (${settings.maxVendors}). Cannot add more vendors!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    showAddVendorDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Error fetching vendor data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showAddVendorDialog() {
        Log.d("VendorListFragment", "Showing add vendor dialog")
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater

        val dialogView = inflater.inflate(R.layout.dialog_add_vendor, null)

        // Set the view and title for the dialog
        dialogBuilder.setView(dialogView)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        cancelButton.setOnClickListener {
            dialogBuilder.create().dismiss()
        }

        confirmButton.setOnClickListener {
            Log.d("VendorListFragment", "Confirm button clicked")
            val amountEditText = dialogView.findViewById<EditText>(R.id.amountEditText)
            val amountText = amountEditText.text.toString()

            if (amountText.isNotEmpty()) {
                val amount = amountText.toIntOrNull()
                if (amount != null && amount > 0 && amount < settings.maxVendors && vendorCounter < settings.maxVendors) {
                    addVendorsToFirebase(amount)
                    Toast.makeText(
                        requireContext(),
                        "Vendor(s) added successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialogBuilder.create().dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Amount must be a positive number, and not exceed the hard cap.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Amount cannot be empty.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun addVendorsToFirebase(amount: Int) {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.get().addOnSuccessListener { snapshot ->
            // Create a set of existing vendor numbers
            val existingVendorNumbers = snapshot.children
                .mapNotNull { it.child("vendorNumber").getValue(Int::class.java) }
                .toMutableSet()

            val vendors = mutableListOf<Vendor>()

            // Find available vendor numbers
            var currentVendorNumber = 1
            repeat(amount) {
                // Find the first available vendor number
                while (existingVendorNumbers.contains(currentVendorNumber)) {
                    currentVendorNumber++
                }

                val vendor = Vendor(
                    vendorNumber = currentVendorNumber,
                    name = "Vendor $currentVendorNumber",
                    standNumber = currentVendorNumber,
                    description = "Vendor $currentVendorNumber description",
                    profilePictureURL = "",
                    bannerPictureURL = "",
                    menuItems = mutableListOf<MenuItem>()
                )
                vendors.add(vendor)

                // Mark this number as used
                existingVendorNumbers.add(currentVendorNumber)
                currentVendorNumber++
            }

            vendors.forEach { vendor ->
                vendorsRef.push().setValue(vendor)
                    .addOnSuccessListener {
                        Log.d("VendorListFragment", "Vendor ${vendor.name} added successfully!")
                        loadVendors()
                    }
                    .addOnFailureListener {
                        Log.e(
                            "VendorListFragment",
                            "Failed to add vendor ${vendor.name}: ${it.message}"
                        )
                    }
            }
        }.addOnFailureListener {
            Log.e("VendorListFragment", "Failed to fetch existing vendors: ${it.message}")
        }
    }

    /*
     * TODO: ADD THE U IN CRUD.
     *  - Create fragments for Vendor details, and edit
     *  - And what not.
     *  -
     */
}
