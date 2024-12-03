package id.librocanteen.adminapp.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import id.librocanteen.adminapp.R

class VendorDetails : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vendor__details, container, false)
        navController = findNavController()


        return view
    }

    fun getVendorData() {
        // When the "view details" option is selected, it will redirect to a detail fragment with the passed data from the database.
    }
}