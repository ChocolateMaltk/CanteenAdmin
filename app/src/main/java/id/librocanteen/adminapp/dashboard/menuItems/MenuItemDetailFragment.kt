package id.librocanteen.adminapp.dashboard.menuItems

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.MenuItem

class MenuItemDetailFragment : Fragment() {
    private lateinit var itemImage_imageView: ImageView
    private lateinit var itemNumber_textView: TextView
    private lateinit var itemName_textView: TextView
    private lateinit var itemDescription_textView: TextView
    private lateinit var itemStock_textView: TextView
    private lateinit var itemPrice_textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_menu_item_detail, container, false)

        // Initialize views (while in viewing mode).
        itemImage_imageView = view.findViewById(R.id.ivItemImage)
        itemNumber_textView = view.findViewById(R.id.etItemNumber)
        itemName_textView = view.findViewById(R.id.etItemName)
        itemDescription_textView = view.findViewById(R.id.etItemDescription)
        itemStock_textView = view.findViewById(R.id.etItemStock)
        itemPrice_textView = view.findViewById(R.id.etItemPrice)

        // Retrieve the passed menu item
        val menuItem = arguments?.getParcelable<MenuItem>("menuItem")

        // Populate the views with menu item details
        menuItem?.let {
            itemName_textView.text = it.itemName
            itemNumber_textView.text = it.itemNumber.toString()
            itemDescription_textView.text = it.itemDescription
            itemStock_textView.text = it.itemStock.toString()
            itemPrice_textView.text = it.itemPrice.toString()
        }

        return view
    }
}

/*
 * TODO: Add a toggle for edit mode.
 */