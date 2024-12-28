package id.librocanteen.adminapp.dashboard.menuItems

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.ImageUploadHelper
import id.librocanteen.adminapp.dashboard.objects.MenuItem
import id.librocanteen.adminapp.dashboard.objects.Vendor

class MenuItemDetailFragment : Fragment() {

    // ImageUploadHelper
    private lateinit var imageUploadHelper: ImageUploadHelper
    private var selectedImageUri: Uri? = null

    // UI Components
    private lateinit var itemImage_imageView: ImageView
    private lateinit var btnEditImage: ImageButton
    private lateinit var btnToggleEdit: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDecreaseStock: ImageButton
    private lateinit var btnIncreaseStock: ImageButton

    // Input Fields
    private lateinit var itemNumber_textView: TextInputLayout
    private lateinit var itemName_textView: TextInputLayout
    private lateinit var itemDescription_textView: TextInputLayout
    private lateinit var itemStock_textView: TextInputLayout
    private lateinit var itemPrice_textView: TextInputLayout

    // NavController
    private lateinit var navController: NavController

    // Firebase RTDB & Storage
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference = FirebaseStorage.getInstance().reference


    // Toggle for Edit Mode.
    private var isEditMode = false

    private var currentMenuItem: MenuItem? = null
    private var currentVendor: Vendor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        currentMenuItem = arguments?.getParcelable<MenuItem>("menuItem")
        currentVendor = arguments?.getParcelable<Vendor>("vendor")

        Log.d("MenuItemDetailFragment", "MenuItem: $currentMenuItem")
        Log.d("MenuItemDetailFragment", "Vendor name: ${currentVendor?.name}")
        Log.d("MenuItemDetailFragment", "Vendor NodeKey: ${currentVendor?.nodeKey}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu_item_detail, container, false)

        navController = findNavController()
        imageUploadHelper = ImageUploadHelper(this)

        // Initialize all views
        initializeViews(view)
        populateViews()
        // Set up edit mode toggle
        setupEditModeToggle()

        return view
    }

    private fun initializeViews(view: View) {
        // Image components
        itemImage_imageView = view.findViewById(R.id.ivItemImage)
        btnEditImage = view.findViewById(R.id.btnEditImage)

        // Text Input Layouts
        itemNumber_textView = view.findViewById(R.id.tilItemNumber)
        itemName_textView = view.findViewById(R.id.tilItemName)
        itemDescription_textView = view.findViewById(R.id.tilItemDescription)
        itemStock_textView = view.findViewById(R.id.tilItemStock)
        itemPrice_textView = view.findViewById(R.id.tilItemPrice)

        // Buttons
        btnToggleEdit = view.findViewById(R.id.btnToggleEdit)
        btnDelete = view.findViewById(R.id.btnDelete)
        btnSave = view.findViewById(R.id.btnSave)
        btnDecreaseStock = view.findViewById(R.id.btnDecreaseStock)
        btnIncreaseStock = view.findViewById(R.id.btnIncreaseStock)

        val imagePickerLauncher = imageUploadHelper.createImagePickerLauncher(
            onImageSelected = { uri ->
                selectedImageUri = uri
                imageUploadHelper.displayImage(uri, itemImage_imageView)
            },
            onImageDisplayRequired = { uri, imageView ->
                imageUploadHelper.displayImage(uri, imageView)
            }
        )

        btnEditImage.setOnClickListener{
            imagePickerLauncher.launch("image/*")
        }

        btnDelete.setOnClickListener {
            deleteMenuItemFromFirebase()
        }
    }

    private fun populateViews() {
        currentMenuItem?.let { menuItem ->
            // Render the Image
            if (menuItem.itemPictureURL.isNullOrEmpty()) {
                itemImage_imageView.setImageResource(R.drawable.menuitemplaceholder)
            } else {
                Glide.with(requireContext())
                    .load(menuItem.itemPictureURL)
                    .into(itemImage_imageView)
            }

            // Populate EditTexts
            itemName_textView.editText?.setText(menuItem.itemName)
            itemNumber_textView.editText?.setText(menuItem.itemNumber.toString())
            itemDescription_textView.editText?.setText(menuItem.itemDescription)
            itemStock_textView.editText?.setText(menuItem.itemStock.toString())
            itemPrice_textView.editText?.setText(menuItem.itemPrice.toString())

            itemNumber_textView.editText?.isEnabled = false
        }
    }

    private fun setupEditModeToggle() {
        btnToggleEdit.setOnClickListener {
            isEditMode = !isEditMode
            updateUIForEditMode()
        }

        btnSave.setOnClickListener {
            saveChangesToFirebase()
            isEditMode = false
            updateUIForEditMode()
        }

        // Stock adjustment buttons
        btnDecreaseStock.setOnClickListener {
            val currentStock = itemStock_textView.editText?.text.toString().toIntOrNull() ?: 0
            if (currentStock > 0) {
                itemStock_textView.editText?.setText((currentStock - 1).toString())
            }
        }

        btnIncreaseStock.setOnClickListener {
            val currentStock = itemStock_textView.editText?.text.toString().toIntOrNull() ?: 0
            itemStock_textView.editText?.setText((currentStock + 1).toString())
        }
    }

    private fun updateUIForEditMode() {
        // Toggle edit mode for all input fields except item number
        val editFields = listOf(
            itemName_textView,
            itemDescription_textView,
            itemStock_textView,
            itemPrice_textView
        )
        editFields.forEach { field ->
            field.editText?.isEnabled = isEditMode
        }

        // Keep item number always disabled
        itemNumber_textView.editText?.isEnabled = false

        // Visibility of edit-related components
        btnEditImage.visibility = if (isEditMode) View.VISIBLE else View.GONE
        btnDecreaseStock.visibility = if (isEditMode) View.VISIBLE else View.GONE
        btnIncreaseStock.visibility = if (isEditMode) View.VISIBLE else View.GONE

        // Toggle save and delete buttons
        btnSave.visibility = if (isEditMode) View.VISIBLE else View.GONE
        btnDelete.visibility = if (!isEditMode) View.VISIBLE else View.GONE

        // Update edit toggle button text
        btnToggleEdit.text =
            if (isEditMode) getString(R.string.cancelCaps) else getString(R.string.editCaps)
    }

    private fun saveChangesToFirebase() {
        currentMenuItem?.let { menuItem ->
            // If new image is selected, upload it first
            selectedImageUri?.let { uri ->
                imageUploadHelper.uploadMenuItemImage(
                    uri = uri,
                    vendorNodeKey = currentVendor?.nodeKey.toString(),
                    menuItemNumber = menuItem.itemNumber.toString(),
                    existingUrl = menuItem.itemPictureURL,
                    onSuccess = { downloadUrl ->
                        // Save menu item with new image URL
                        saveMenuItemToDatabase(menuItem, downloadUrl)
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to upload image: ${exception.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } ?: run {
                // No new image selected, save with existing URL
                saveMenuItemToDatabase(menuItem, menuItem.itemPictureURL)
            }
        }
    }

    // Helper method to save menu item to database
    private fun saveMenuItemToDatabase(menuItem: MenuItem, imageUrl: String?) {
        // Create the updated menu item using the ORIGINAL item number
        val updatedMenuItem = MenuItem(
            itemNumber = menuItem.itemNumber,  // Keep the original item number
            itemName = itemName_textView.editText?.text.toString(),
            itemDescription = itemDescription_textView.editText?.text.toString(),
            itemStock = itemStock_textView.editText?.text.toString().toIntOrNull() ?: 0,
            itemPrice = itemPrice_textView.editText?.text.toString().toIntOrNull() ?: 0,
            itemPictureURL = imageUrl ?: ""
        )

        // Update the item at its original location
        databaseReference
            .child("vendors")
            .child(currentVendor?.nodeKey.toString())
            .child("menuItems")
            .child(menuItem.itemNumber.toString())
            .setValue(updatedMenuItem)
            .addOnSuccessListener {
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(),
                        "Item updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    isEditMode = false
                    currentMenuItem = updatedMenuItem
                    updateUIForEditMode()
                    selectedImageUri = null
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update item: ${exception.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun deleteMenuItemFromFirebase() {
        currentMenuItem?.let { menuItem ->
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete this menu item?")
                .setPositiveButton("Delete") { _, _ ->
                    // Use ImageUploadHelper to delete the image and folder
                    imageUploadHelper.deleteMenuItemImage(
                        existingUrl = menuItem.itemPictureURL,
                        onSuccess = {
                            // After image is deleted, delete from database
                            deleteMenuItemFromDatabase(menuItem)
                        },
                        onFailure = { exception ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete image: ${exception.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    // Helper method to delete menu item from database
    private fun deleteMenuItemFromDatabase(menuItem: MenuItem) {
        databaseReference
            .child("vendors")
            .child(currentVendor?.nodeKey.toString())
            .child("menuItems")
            .child(menuItem.itemNumber.toString())
            .removeValue()
            .addOnSuccessListener {
                if (isAdded && context != null) {
                    Log.d("MenuItemDetailFragment", "Item deleted successfully.")
                    Toast.makeText(
                        requireContext(),
                        "Item deleted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                    navController.navigateUp()
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded && context != null) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete item: ${exception.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}