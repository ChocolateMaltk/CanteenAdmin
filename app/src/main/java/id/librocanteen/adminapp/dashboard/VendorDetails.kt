package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.Vendor

class VendorDetails : Fragment() {

    private lateinit var navController: NavController

    private lateinit var vendorNameTextView: TextView
    private lateinit var vendorVendorNumberView: TextView
    private lateinit var vendorDescriptionTextView: TextView
    private lateinit var vendorStandNumberTextView: TextView
    private lateinit var vendorBannerImageView: ImageView
    private lateinit var vendorProfilePictureView: ImageView
    private lateinit var vendorDetailsHeadline: TextView
    private lateinit var editButton: Button
    private lateinit var menuItemsButton: Button

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
        vendorDetailsHeadline = view.findViewById(R.id.vendorDetailsHeadline)

        // Button UI Elements
        editButton = view.findViewById(R.id.enterEditVendorDetails)
        menuItemsButton = view.findViewById(R.id.checkVendorMenu)

        editButton.setOnClickListener {
            redirectToEdit()
        }
        menuItemsButton.setOnClickListener {
            redirectToMenu()
        }

        // Retrieve the vendor data passed in the Bundle
        val vendor = arguments?.getParcelable<Vendor>("vendor")

        // Display the vendor data
        vendor?.let {
            vendorNameTextView.text = it.name
            vendorVendorNumberView.text = "# ${it.vendorNumber}"
            vendorStandNumberTextView.text = "Vendor Stand: ${it.standNumber}"
            vendorDescriptionTextView.text = it.description
            // Profile Picture
            if (it.profilePictureURL.isNotEmpty() && it.profilePictureURL.isNotBlank()) {
                Glide.with(requireContext())
                    .load(it.profilePictureURL)
                    .placeholder(R.drawable.placeholder_profile_picture)
                    .error(R.drawable.placeholder_profile_picture)
                    .into(vendorProfilePictureView)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.placeholder_profile_picture)
                    .into(vendorProfilePictureView)
            }
            // Banner Picture

            if (it.bannerPictureURL.isNotEmpty() && it.bannerPictureURL.isNotBlank()) {
                Glide.with(requireContext())
                    .load(it.bannerPictureURL) // Use the actual URL
                    .placeholder(R.drawable.placeholder_profile_banner) // Placeholder
                    .error(R.drawable.placeholder_profile_banner) // Error fallback
                    .into(vendorBannerImageView)
            } else {
                // If the URL is empty or blank, load the placeholder
                Glide.with(requireContext())
                    .load(R.drawable.placeholder_profile_banner)
                    .into(vendorBannerImageView)
            }


            vendorDetailsHeadline.text = getString(R.string.vendor_details_headline, it.name)


        }

        return view
    }

    fun redirectToEdit() {

        val vendor = arguments?.getParcelable<Vendor>("vendor")
        val bundle = Bundle().apply {
            putParcelable("vendor", vendor)
        }
        navController.navigate(
            R.id.action_vendorDetailsFragment_to_editVendorDetailsFragment,
            bundle
        )
    }

    fun redirectToMenu() {
        val vendor = arguments?.getParcelable<Vendor>("vendor")
        val menuItemsArray = vendor?.menuItems
            ?.map { it as Parcelable }
            ?.toTypedArray()

        val bundle = bundleOf(
            "vendor" to vendor,
            "vendorName" to vendor?.name,
            "vendorKey" to vendor?.nodeKey,
            "menuItems" to menuItemsArray
        )

        navController.navigate(
            R.id.action_checkVendorsMenu,
            bundle
        )
    }

}

