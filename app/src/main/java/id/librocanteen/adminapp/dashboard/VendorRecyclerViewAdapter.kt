package id.librocanteen.adminapp.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor

class VendorRecyclerViewAdapter(
    private val context: Context,
    private val vendors: List<Vendor>,
    private val vendorListFragment: VendorListFragment
) : RecyclerView.Adapter<VendorRecyclerViewAdapter.VendorViewHolder>() {

    inner class VendorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.vendorName)
        val description: TextView = view.findViewById(R.id.vendorDescription)
        val bannerPicture: ImageView = view.findViewById(R.id.vendorBannerPicture)
        val cardView: View = view.rootView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_vendorcard, parent, false)
        return VendorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VendorViewHolder, position: Int) {
        val vendor = vendors[position]
        holder.name.text = vendor.name
        holder.description.text = vendor.description
        Glide.with(context).load(vendor.bannerPictureURL).into(holder.bannerPicture)

        holder.cardView.setOnClickListener {
            showVendorOptionsDialog(vendor)
        }
    }

    private fun showVendorOptionsDialog(vendor: Vendor) {
        val options = arrayOf("View Details", "Edit Vendor", "Delete Vendor")
        val dialog = AlertDialog.Builder(context)
            .setTitle("Vendor Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showVendorDetails(vendor)
                    1 -> editVendor(vendor)
                    2 -> deleteVendor(vendor)
                }
            }
            .create()
        dialog.show()
    }

    private fun showVendorDetails(vendor: Vendor) {
        val detailDialog = AlertDialog.Builder(context)
            .setTitle(vendor.name)
            .setMessage(
                """
                Stand Number: ${vendor.standNumber}
                Description: ${vendor.description}
                Vendor Number: ${vendor.vendorNumber}
                Menu Items: ${vendor.menuItems.size}
            """.trimIndent()
            )
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .create()
        detailDialog.show()
    }

    private fun editVendor(vendor: Vendor) {
        AlertDialog.Builder(context)
        Toast.makeText(context, "Edit ${vendor.name}", Toast.LENGTH_SHORT).show()
        // Actually make this into a new fragment to edit lolz
    }

    private fun deleteVendor(vendor: Vendor) {
        AlertDialog.Builder(context)
            .setTitle("Delete Vendor")
            .setMessage("Are you sure you want to delete ${vendor.name}?")
            .setPositiveButton("Delete") { _, _ ->
                // Implement Firebase deletion
                val vendorsRef = FirebaseDatabase.getInstance().reference.child("vendors")
                vendorsRef.orderByChild("name").equalTo(vendor.name)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (childSnapshot in snapshot.children) {
                                childSnapshot.ref.removeValue()
                                    .addOnSuccessListener {
                                        vendorListFragment.vendorCounter--
                                        vendorListFragment.loadVendors()
                                        Toast.makeText(
                                            context,
                                            "Vendor ${vendor.name} deleted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            context,
                                            "Failed to delete ${vendor.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun getItemCount(): Int = vendors.size
}

/*
 *
 * TODO: When a card is clicked, a popup dialog will show up prompting to view, edit or delete a Vendor document.
 */