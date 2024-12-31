package id.librocanteen.adminapp.dashboard.objects

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isUserValidated = MutableLiveData<Boolean>()
}