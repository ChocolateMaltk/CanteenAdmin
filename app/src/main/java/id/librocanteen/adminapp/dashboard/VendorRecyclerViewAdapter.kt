package id.librocanteen.adminapp.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor

class VendorRecyclerViewAdapter(
    private val context: Context,
    private val vendors: List<Vendor>
) : RecyclerView.Adapter<VendorRecyclerViewAdapter.VendorViewHolder>() {

    inner class VendorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.vendorName)
        val description: TextView = view.findViewById(R.id.vendorDescription)
        val bannerPicture: ImageView = view.findViewById(R.id.vendorBannerPicture)
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
    }

    override fun getItemCount(): Int = vendors.size
}

/*
 *
 * TODO: ALSO FIX THE XML LAYOUTS.
 */