package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor

class VendorDetails : Fragment() {

    private lateinit var navController: NavController

    private lateinit var vendorNameTextView: TextView
    private lateinit var vendorVendorNumberView : TextView
    private lateinit var vendorDescriptionTextView: TextView
    private lateinit var vendorStandNumberTextView: TextView
    private lateinit var vendorBannerImageView: ImageView
    private lateinit var vendorProfilePictureView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vendor__details, container, false)
        navController = findNavController()

        // Initialize UI elements
        vendorNameTextView = view.findViewById(R.id.nameTextView)
        vendorVendorNumberView = view.findViewById(R.id.vendorNumberTextView)
        vendorStandNumberTextView = view.findViewById(R.id.standNumberTextView)
        vendorDescriptionTextView = view.findViewById(R.id.descriptionTextView)
        vendorBannerImageView = view.findViewById(R.id.bannerImageView)
        vendorProfilePictureView = view.findViewById(R.id.profileImageView)

        // Retrieve the vendor data passed in the Bundle
        val vendor = arguments?.getParcelable<Vendor>("vendor")

        // Display the vendor data
        vendor?.let {
            vendorNameTextView.text = it.name
            vendorVendorNumberView.text = "# ${it.vendorNumber}"
            vendorStandNumberTextView.text = "Vendor Stand: ${it.standNumber}"
            vendorDescriptionTextView.text = it.description
            Glide.with(requireContext()).load(it.profilePictureURL).into(vendorProfilePictureView)
            Glide.with(requireContext()).load(it.bannerPictureURL).into(vendorBannerImageView)
        }

        return view
    }
}
