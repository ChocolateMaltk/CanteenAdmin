package id.librocanteen.adminapp.dashboard.objects

import android.net.Uri
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
            onImageDisplayRequired(it, ImageView(fragment.requireContext()))
        }
    }

    fun displayImage(uri: Uri, imageView: ImageView) {
        Glide.with(fragment.requireContext())
            .load(uri)
            .into(imageView)
    }

    fun uploadImage(
        uri: Uri,
        vendorNumber: Int,
        imageType: String,
        existingUrl: String? = null,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // First, delete the existing image if it exists
        deleteExistingImage(
            existingUrl,
            onSuccess = {
                // After successful deletion (or if no existing image), proceed with upload
                val imageRef = storageReference.child(
                    "images/vendors/$vendorNumber/${imageType}Pictures/${UUID.randomUUID()}"
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
                // If deletion fails, call the failure callback
                onFailure(exception)
            }
        )
    }

    fun deleteExistingImage(
        existingUrl: String?,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        existingUrl?.let { url ->
            val existingImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            existingImageRef.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }
    }
}