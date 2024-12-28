package id.librocanteen.adminapp.dashboard.menuItems

import android.net.Uri
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
import id.librocanteen.adminapp.dashboard.objects.ImageUploadHelper

class MenuItemsListFragment : Fragment() {
    private var vendorName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuItemAdapter: MenuItemAdapter
    private var menuItems: MutableList<MenuItem> = mutableListOf()
    private lateinit var imageUploadHelper: ImageUploadHelper
    private lateinit var menuItemFAB: FloatingActionButton
    private var isAddingMenuItem = false

    companion object {
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

        val titleTextView: TextView = view.findViewById(R.id.vendorMenuTitle)
        titleTextView.text = vendorName?.let { "$it's Menu" } ?: getString(R.string.vendor_menu_title)

        menuItemFAB = view.findViewById(R.id.addNewMenuItemFAB)
        menuItemFAB.setOnClickListener {
            if (!isAddingMenuItem) {
                addNewMenuItemToFirebase()
            }
        }

        imageUploadHelper = ImageUploadHelper(this)
        fetchMenuItemsFromFirebase()

        return view
    }

    private fun setAddingMenuItemState(isAdding: Boolean) {
        isAddingMenuItem = isAdding
        menuItemFAB.isEnabled = !isAdding
        menuItemFAB.alpha = if (isAdding) 0.5f else 1.0f
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
                            it.itemNumber = itemCount++
                            menuItems.add(it)
                        }
                    }
                    Log.d("FirebaseData: ", "Total items fetched: ${menuItems.size}")
                    menuItemAdapter.fetchItems(menuItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().printStackTrace()
                    setAddingMenuItemState(false)
                }
            })
        }
    }

    private fun addNewMenuItemToFirebase() {
        setAddingMenuItemState(true)

        vendorKey?.let { nodeKey ->
            val databaseReference = FirebaseDatabase.getInstance().reference
                .child("vendors")
                .child(nodeKey)
                .child("menuItems")

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Find the first available index
                    val existingIndices = mutableSetOf<Int>()
                    for (childSnapshot in snapshot.children) {
                        val menuItem = childSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let {
                            existingIndices.add(it.itemNumber)
                        }
                    }

                    // Find the first available index by checking for gaps
                    var newItemNumber = 0
                    while (existingIndices.contains(newItemNumber)) {
                        newItemNumber++
                    }

                    val placeholderResId = R.drawable.menuitemplaceholder
                    val placeholderUri = Uri.parse("android.resource://${requireContext().packageName}/$placeholderResId")

                    imageUploadHelper.uploadMenuItemImage(
                        uri = placeholderUri,
                        vendorNodeKey = nodeKey,
                        menuItemNumber = newItemNumber.toString(),
                        existingUrl = null,
                        onSuccess = { downloadUrl ->
                            val newItem = MenuItem(
                                nodeKey = newItemNumber.toString(),
                                itemNumber = newItemNumber,
                                itemName = "Menu Item $newItemNumber",
                                itemDescription = "Description for menu item $newItemNumber",
                                itemStock = 100,
                                itemPrice = 500,
                                itemPictureURL = downloadUrl
                            )

                            databaseReference.child(newItemNumber.toString()).setValue(newItem)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "New menu item added!", Toast.LENGTH_SHORT).show()
                                    setAddingMenuItemState(false)
                                    fetchMenuItemsFromFirebase()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        "Failed to add item: ${exception.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    setAddingMenuItemState(false)
                                }
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                context,
                                "Failed to upload image: ${exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                            setAddingMenuItemState(false)
                        }
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        context,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setAddingMenuItemState(false)
                }
            })
        }
    }
}