package id.librocanteen.adminapp.dashboard.menuItems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.MenuItem

class MenuItemsCardFragment : Fragment() {

    private var menuItems: List<MenuItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            menuItems = it.getParcelableArrayList("menuItems") ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu_items_card, container, false)
    }
}