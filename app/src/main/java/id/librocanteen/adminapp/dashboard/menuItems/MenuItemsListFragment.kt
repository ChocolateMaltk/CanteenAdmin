package id.librocanteen.adminapp.dashboard.menuItems

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor
import id.librocanteen.adminapp.dashboard.objects.MenuItem
import id.librocanteen.adminapp.dashboard.menuItems.MenuItemAdapter

class MenuItemsListFragment : Fragment() {
    private var vendorName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapter
    private var menuItems: MutableList<MenuItem> = mutableListOf()

    companion object {
        // Factory method to create fragment with arguments
        fun newInstance(vendorName: String, nodeKey: String): MenuItemsListFragment {
            val fragment = MenuItemsListFragment()
            val args = Bundle()
            args.putString("vendorName", vendorName)
            args.putString("vendorKey", nodeKey)
            fragment.arguments = args
            return fragment
        }
    }

    private var vendorKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vendorName = arguments?.getString("vendorName")
        vendorKey = arguments?.getString("vendorKey")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu_items_list, container, false)

        recyclerView = view.findViewById(R.id.menuItemsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        menuItemAdapter = MenuItemAdapter(menuItems, Vendor(vendorKey ?: "", vendorName ?: ""))
        recyclerView.adapter = menuItemAdapter

        // Dynamically set title
        val titleTextView: TextView = view.findViewById(R.id.vendorMenuTitle)
        titleTextView.text =
            vendorName?.let { "$it's Menu" } ?: getString(R.string.vendor_menu_title)

        // Get the FAB
        val menuItemFAB: FloatingActionButton = view.findViewById(R.id.addNewMenuItemFAB)
        menuItemFAB.setOnClickListener {
            addNewMenuItemToFirebase()
        }

        // Fetch menu items from Firebase
        fetchMenuItemsFromFirebase()

        return view
    }

    private fun fetchMenuItemsFromFirebase() {
        vendorKey?.let { nodeKey ->
            val databaseReference = FirebaseDatabase.getInstance().reference
                .child("vendors")
                .child(nodeKey)
                .child("menuItems")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    menuItems.clear()
                    var itemCount = 0
                    for (childSnapshot in snapshot.children) {
                        val menuItem = childSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let {
                            Log.d("Firebase Data", "Fetched item: ${it.itemName}")
                            it.itemNumber = itemCount++ // Update item number dynamically
                            menuItems.add(it)
                        }
                    }
                    Log.d("FirebaseData: ", "Total items fetched: ${menuItems.size}")

                    // Update the adapter with new data
                    menuItemAdapter.fetchItems(menuItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log error and handle it gracefully
                    error.toException().printStackTrace()
                }
            })
        }
    }

    private fun addNewMenuItemToFirebase() {
        vendorKey?.let { nodeKey ->
            val databaseReference = FirebaseDatabase.getInstance().reference
                .child("vendors")
                .child(nodeKey)
                .child("menuItems")

            // Use a single value event listener to get the current menu items
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Create a list to hold existing menu items
                    val existingMenuItems = mutableListOf<MenuItem>()

                    // Populate the list with existing menu items
                    for (childSnapshot in snapshot.children) {
                        val menuItem = childSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let { existingMenuItems.add(it) }
                    }

                    // Create a new MenuItem
                    val newItem = MenuItem(
                        generateMenuItemNodeKey(existingMenuItems.size + 1), // You can leave this empty if you don't want to use a unique key
                        existingMenuItems.size + 1, // Set the item number based on the count
                        "Menu Item ${existingMenuItems.size + 1}", // Name of the new item
                        "Description for menu item ${existingMenuItems.size + 1}", // Description of the new item
                        100, // Stock
                        500, // Price
                        "https://via.placeholder.com/150" // Picture URL
                    )

                    // Add the new item to the list
                    existingMenuItems.add(newItem)

                    // Update the database with the new list
                    databaseReference.setValue(existingMenuItems)

                    // Optionally, fetch the updated menu items from Firebase
                    fetchMenuItemsFromFirebase()
                    Toast.makeText(context, "New item added!", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log error and handle it gracefully
                    error.toException().printStackTrace()
                }
            })
        }
    }

    fun generateMenuItemNodeKey(counter: Int): String {
        return "M${counter.toString().padStart(2, '0')}"
    }
}