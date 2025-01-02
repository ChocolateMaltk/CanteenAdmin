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
                loadVendors()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                Log.e("VendorListFragment", "Database Error vendors: ${error.message}")
            }
        })
    }

    fun generateCustomVendorKey(counter: Int): String {
        return "V${counter.toString().padStart(2, '0')}"
    }

    fun generateMenuItemNodeKey(counter: Int): String {
        return "M${counter.toString().padStart(2, '0')}"
    }


    fun loadVendors() {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount < 1) {
                    Toast.makeText(
                        requireContext(),
                        "No vendors available. Go make some!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
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

        val dialog = dialogBuilder.setView(dialogView).create()
        dialogBuilder.setView(dialogView)

        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
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
                    dialog.dismiss()
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
            dialog.dismiss()
        }
        dialog.show()
    }


    private fun addVendorsToFirebase(amount: Int) {
        val vendorsRef = database.reference.child("vendors")

        vendorsRef.get().addOnSuccessListener { snapshot ->
            // Create a set of existing vendor numbers
            val existingKeys = snapshot.children.map { it.key }.toSet()

            val vendors = mutableListOf<Vendor>()

            // Generate new custom keys and ensure they are unique
            var currentCounter = 0
            repeat(amount) {
                var customVendorKey: String
                var menuItemKeys = mutableListOf<String>()
                do {
                    currentCounter++
                    customVendorKey = generateCustomVendorKey(currentCounter)
                    for (i in 0 until 3) { // Generate 3 menu item keys
                        val customMenuItemKey = generateMenuItemNodeKey(i)
                        menuItemKeys.add(customMenuItemKey)
                    }
                } while (existingKeys.contains(customVendorKey)) // Ensure vendor key uniqueness

//                val literallyOne = 1

                val menuItems = mutableListOf<MenuItem>()
//                val testMenuItem = MenuItem(
//                    nodeKey = "M01",
//                    itemNumber = literallyOne,
//                    itemName = "Menu Item $literallyOne",
//                    itemDescription = "Description for menu item $literallyOne",
//                    itemStock = 100,
//                    itemPrice = 500,
//                    itemPictureURL = ""
//                )

                val vendor = Vendor(
                    nodeKey = customVendorKey,
                    vendorAccessKey = generateVendorAccessKey(),
                    vendorNumber = currentCounter,
                    name = "Vendor $currentCounter",
                    standNumber = currentCounter,
                    description = "Vendor $currentCounter description",
                    profilePictureURL = "",
                    bannerPictureURL = "",
                    isSelling = true,
                    boundToUser = "",
                    menuItems = menuItems
                )
                vendors.add(vendor)

                // Use the custom vendor key as the key in the database
                vendorsRef.child(customVendorKey).setValue(vendor)
                    .addOnSuccessListener {
                        Log.d(
                            "VendorListFragment",
                            "Vendor ${vendor.name} added with key $customVendorKey"
                        )
                        loadVendors()
                    }
                    .addOnFailureListener {
                        Log.e(
                            "VendorListFragment",
                            "Failed to add vendor ${vendor.name}: ${it.message}"
                        )
                    }

//                addMenuItemToVendor(customVendorKey, testMenuItem)
                addMenuItemToVendor(customVendorKey)
            }
        }.addOnFailureListener {
            Log.e("VendorListFragment", "Failed to fetch existing vendors: ${it.message}")
        }
    }

    fun generateVendorAccessKey(): String {
        val characters = ('a'..'z') + ('A'..'Z') + ('0'..'9') + listOf(
            '!', '@', '#', '$', '%', '^', '&', '*', '-', '_', '=', '+'
        )
        // Generate a random string of 20 characters
        val randomString = (1..20).map { characters.random() }.joinToString("")

        // Split the string into chunks of 4 characters and join with dashes
        val formattedString = randomString.chunked(4).joinToString("-")
        return formattedString
    }

    fun addMenuItemToVendor(vendorKey: String, menuItem: MenuItem) {
        // Reference to the vendor's menuItems node (list of menu items)
        val menuItemsRef = FirebaseDatabase.getInstance().reference
            .child("vendors")
            .child(vendorKey)
            .child("menuItems")

        // Get the current list of menu items from Firebase
        menuItemsRef.get().addOnSuccessListener { snapshot ->
            val currentMenuItems =
                snapshot.getValue<List<MenuItem>>()?.toMutableList() ?: mutableListOf()

            // Add the new menu item to the list
            currentMenuItems.add(menuItem)

            // Set the updated list of menu items back to Firebase
            menuItemsRef.setValue(currentMenuItems)
                .addOnSuccessListener {
                    Log.d(
                        "MenuItemsListFragment",
                        "Menu item ${menuItem.itemName} added successfully!"
                    )
                }
                .addOnFailureListener { exception ->
                    Log.e(
                        "MenuItemsListFragment",
                        "Failed to add menu item ${menuItem.itemName}: ${exception.message}"
                    )
                }
        }.addOnFailureListener { exception ->
            Log.e(
                "MenuItemsListFragment",
                "Failed to fetch current menu items: ${exception.message}"
            )
        }
    }

    fun addMenuItemToVendor(vendorKey: String) {
        // Reference to the vendor's menuItems node (list of menu items)
        val menuItemsRef = FirebaseDatabase.getInstance().reference
            .child("vendors")
            .child(vendorKey)
            .child("menuItems")

        // Get the current list of menu items from Firebase
        menuItemsRef.get().addOnSuccessListener { snapshot ->
            val currentMenuItems =
                snapshot.getValue<List<MenuItem>>()?.toMutableList() ?: mutableListOf()
        }
    }
}
