package id.librocanteen.adminapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import id.librocanteen.adminapp.dashboard.objects.Vendor

class SharedViewModel : ViewModel() {
    private val _selectedVendor = MutableLiveData<Vendor>()
    val selectedVendor: MutableLiveData<Vendor> = _selectedVendor

    fun selectVendor(vendor: Vendor) {
        _selectedVendor.value = vendor
    }
}