package id.librocanteen.adminapp.dashboard.objects

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class ImageUploadHelper(private val fragment: Fragment) {
    private var storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageReference: StorageReference = storage.reference

    fun createImagePickerLauncher(
        onImageSelected: (Uri) -> Unit,
        onImageDisplayRequired: (Uri, ImageView) -> Unit
    ) = fragment.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            onImageSelected(it)
        }
    }

    fun displayImage(uri: Uri, imageView: ImageView) {
        Glide.with(fragment.requireContext())
            .load(uri)
            .into(imageView)
    }

    fun uploadMenuItemImage(
        uri: Uri,
        vendorNodeKey: String,
        menuItemNumber: String,
        existingUrl: String? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Create a fixed image name instead of UUID to prevent multiple files
        val imageName = "item_image.jpg"

        // Create a fixed path for the menu item image
        val imageRef = storageReference.child(
            "images/vendors/$vendorNodeKey/menuItems/$menuItemNumber/$imageName"
        )

        // First delete the existing image if any
        deleteMenuItemImage(
            vendorNodeKey = vendorNodeKey,
            menuItemNumber = menuItemNumber,
            existingUrl = existingUrl,
            onSuccess = {
                // Upload the new image
                imageRef.putFile(uri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        imageRef.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    fun deleteMenuItemImage(
        vendorNodeKey: String? = null,
        menuItemNumber: String? = null,
        existingUrl: String? = null,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        try {
            if (existingUrl.isNullOrBlank()) {
                onSuccess()
                return
            }

            // Try to get reference from existing URL first
            val imageRef = if (vendorNodeKey != null && menuItemNumber != null) {
                // If we have vendor and item info, use the direct path
                storageReference.child("images/vendors/$vendorNodeKey/menuItems/$menuItemNumber")
            } else {
                // Otherwise try to get reference from URL
                storage.getReferenceFromUrl(existingUrl).parent
            }

            // Delete the entire menu item folder
            imageRef?.listAll()
                ?.addOnSuccessListener { listResult ->
                    // If folder is empty or doesn't exist
                    if (listResult.items.isEmpty()) {
                        onSuccess()
                        return@addOnSuccessListener
                    }

                    // Delete all files in the folder
                    var deletedCount = 0
                    listResult.items.forEach { itemRef ->
                        itemRef.delete().addOnCompleteListener { task ->
                            deletedCount++
                            if (deletedCount == listResult.items.size) {
                                // All files deleted
                                onSuccess()
                            }
                        }
                    }
                }
                ?.addOnFailureListener { exception ->
                    Log.e("ImageUploadHelper", "Error listing files: ${exception.message}")
                    onFailure(exception)
                }
        } catch (e: Exception) {
            Log.e("ImageUploadHelper", "Error deleting image: ${e.message}")
            onFailure(e)
        }
    }

    // General purpose image upload method (for other types of images)
    fun uploadImage(
        uri: Uri,
        vendorNodeKey: String,
        imageType: String,
        existingUrl: String? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val imageName = "image_${System.currentTimeMillis()}.jpg"

        deleteExistingImage(
            existingUrl,
            onSuccess = {
                val imageRef = storageReference.child(
                    "images/vendors/$vendorNodeKey/${imageType}/$imageName"
                )
                imageRef.putFile(uri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        imageRef.downloadUrl
                    }
                    .addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    private fun deleteExistingImage(
        existingUrl: String?,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        if (!existingUrl.isNullOrBlank()) {
            try {
                val existingImageRef = storage.getReferenceFromUrl(existingUrl)
                existingImageRef.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            } catch (e: Exception) {
                Log.e("ImageUploadHelper", "Invalid image URL: $existingUrl", e)
                onFailure(e)
            }
        } else {
            onSuccess()
        }
    }
}