package id.librocanteen.adminapp.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor
import id.librocanteen.adminapp.dashboard.objects.ImageUploadHelper

class EditVendorDetails : Fragment() {

    private lateinit var navController: NavController

    private lateinit var headline: TextView

    private lateinit var vendorNameEditText: EditText
    private lateinit var vendorNumberEditText: EditText
    private lateinit var standNumberEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var profilePicButton: ImageButton
    private lateinit var bannerPicButton: ImageButton

    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var imageUploadHelper: ImageUploadHelper
    private var selectedProfileImageUri: Uri? = null
    private var selectedBannerImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_vendor_details, container, false)
        navController = findNavController()

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        imageUploadHelper = ImageUploadHelper(this)

        val resultProfilePicLauncher = imageUploadHelper.createImagePickerLauncher(
            onImageSelected = { uri ->
                selectedProfileImageUri = uri
            },
            onImageDisplayRequired = { uri, imageView ->
                imageUploadHelper.displayImage(uri, profilePicButton)
            }
        )

        val resultBannerPicLauncher = imageUploadHelper.createImagePickerLauncher(
            onImageSelected = { uri ->
                selectedBannerImageUri = uri
            },
            onImageDisplayRequired = { uri, imageView ->
                imageUploadHelper.displayImage(uri, bannerPicButton)
            }
        )

        // Initialize the edit texts
        vendorNameEditText = view.findViewById<EditText>(R.id.nameEditText)
        vendorNumberEditText = view.findViewById<EditText>(R.id.vendorNumberEditText)
        standNumberEditText = view.findViewById<EditText>(R.id.standNumberEditText)
        descriptionEditText = view.findViewById<EditText>(R.id.descriptionEditText)

        // Initialize the UI Buttons
        profilePicButton = view.findViewById<ImageButton>(R.id.profileImageButton)
        bannerPicButton = view.findViewById<ImageButton>(R.id.bannerImageButton)
        saveButton = view.findViewById<Button>(R.id.saveVendorDetailEdits)
        cancelButton = view.findViewById<Button>(R.id.cancelVendorDetailEdits)
        headline = view.findViewById<TextView>(R.id.editVendorDetailsHeadline)

        val vendor = arguments?.getParcelable<Vendor>("vendor")

        vendor?.let {
            vendorNameEditText.setText(it.name ?: "")
            vendorNumberEditText.setText(it.vendorNumber.toString())
            standNumberEditText.setText(it.standNumber.toString())
            descriptionEditText.setText(it.description)
            headline.text = getString(R.string.editing_vendor_details, it.name)
        }

        profilePicButton.setOnClickListener {
            resultProfilePicLauncher.launch("image/*")
        }

        bannerPicButton.setOnClickListener {
            resultBannerPicLauncher.launch("image/*")
        }

        cancelButton.setOnClickListener {
            navController.navigateUp()
        }

        saveButton.setOnClickListener {
            if (validateInputs()) {
                val updatedVendor = Vendor(
                    name = vendorNameEditText.text.toString(),
                    vendorNumber = vendorNumberEditText.text.toString().toInt(),
                    standNumber = standNumberEditText.text.toString().toInt(),
                    description = descriptionEditText.text.toString(),
                    profilePictureURL = vendor?.profilePictureURL ?: "",
                    bannerPictureURL = vendor?.bannerPictureURL ?: ""
                )

                // Add this block to handle image uploads
                if (selectedProfileImageUri != null || selectedBannerImageUri != null) {
                    uploadImagesAndUpdateVendor(updatedVendor)
                } else {
                    updateVendorInDatabase(updatedVendor)
                }

                navController.navigate(R.id.action_editVendorDetailsFragment_to_vendorListFragment)
            }
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1001
            )
        }

        return view

    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (vendorNameEditText.text.toString().trim().isEmpty()) {
            vendorNameEditText.error = "Vendor name cannot be empty!"
            isValid = false
        }

        if (vendorNumberEditText.text.toString().trim().isEmpty()) {
            vendorNumberEditText.error = "Vendor number cannot be empty!"
            isValid = false
        }

        if (standNumberEditText.text.toString().trim().isEmpty()) {
            standNumberEditText.error = "Stand number cannot be empty!"
            isValid = false
        }

        return isValid
    }

    private fun updateVendorInDatabase(updatedVendor: Vendor) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("vendors")

        databaseReference.orderByChild("vendorNumber")
            .equalTo(updatedVendor.vendorNumber.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val uniqueKey = childSnapshot.key
                            if (uniqueKey != null) {
                                // Create a map of the fields to update
                                val updates = mapOf(
                                    "name" to updatedVendor.name,
                                    "vendorNumber" to updatedVendor.vendorNumber,
                                    "standNumber" to updatedVendor.standNumber,
                                    "description" to updatedVendor.description,
                                    "profilePictureURL" to updatedVendor.profilePictureURL,
                                    "bannerPictureURL" to updatedVendor.bannerPictureURL
                                )

                                // Perform partial update
                                databaseReference.child(uniqueKey)
                                    .updateChildren(updates)
                                    .addOnSuccessListener {
                                        showToast("Vendor details updated successfully")
                                    }
                                    .addOnFailureListener { exception ->
                                        showToast("Failed to update vendor: ${exception.message}")
                                    }
                            } else {
                                showToast("Failed to find vendor key.")
                            }
                        }
                    } else {
                        showToast("Vendor not found in database.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Error querying database: ${error.message}")
                }
            })
    }

    private fun uploadImagesAndUpdateVendor(existingVendor: Vendor) {
        val uploadTasks = mutableListOf<Task<Uri>>()
        var updatedVendor = existingVendor

        selectedProfileImageUri?.let { uri ->
            imageUploadHelper.uploadImage(
                uri = uri,
                vendorNumber = existingVendor.vendorNumber,
                imageType = "profile",
                onSuccess = { url ->
                    updatedVendor = updatedVendor.copy(profilePictureURL = url)
                    updateVendorInDatabase(updatedVendor)
                },
                onFailure = { exception ->
                    showToast("Profile image upload failed: ${exception.message}")
                }
            )
        }

        selectedBannerImageUri?.let { uri ->
            imageUploadHelper.uploadImage(
                uri = uri,
                vendorNumber = existingVendor.vendorNumber,
                imageType = "banner",
                onSuccess = { url ->
                    updatedVendor = updatedVendor.copy(bannerPictureURL = url)
                    updateVendorInDatabase(updatedVendor)
                },
                onFailure = { exception ->
                    showToast("Banner image upload failed: ${exception.message}")
                }
            )
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Idk man, it was asking for it.")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                showToast("Permission denied")
            }
        }
    }
}