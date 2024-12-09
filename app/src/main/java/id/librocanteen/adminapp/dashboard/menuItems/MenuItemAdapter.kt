package id.librocanteen.adminapp.dashboard.menuItems

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.librocanteen.adminapp.R
import id.librocanteen.adminapp.dashboard.objects.MenuItem

class MenuItemAdapter(private var menuItems: List<MenuItem>) :
    RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNameTextView: TextView = itemView.findViewById(R.id.menuItemName)
        val itemPriceTextView: TextView = itemView.findViewById(R.id.menuItemPrice)
        val itemDescriptionTextView: TextView = itemView.findViewById(R.id.menuItemDescription)
        val itemImageView: ImageView = itemView.findViewById(R.id.menuItemImage)
        val menuItemCard : LinearLayout = itemView.findViewById(R.id.menuItemCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_menu_items_card, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]

        holder.itemNameTextView.text = menuItem.itemName
        holder.itemPriceTextView.text = "Rp. ${menuItem.itemPrice}"
        holder.itemDescriptionTextView.text = menuItem.itemDescription

        // Load image using Glide
        if (menuItem.itemPictureURL.isNotEmpty() && menuItem.itemPictureURL.isNotBlank()) {
            Glide.with(holder.itemImageView.context)
                .load(menuItem.itemPictureURL)
                .placeholder(R.drawable.placeholder_profile_picture)
                .error(R.drawable.placeholder_profile_picture)
                .into(holder.itemImageView)
        } else {
            // Load placeholder if no image
            Glide.with(holder.itemImageView.context)
                .load(R.drawable.placeholder_profile_picture)
                .into(holder.itemImageView)
        }

        holder.menuItemCard.setOnClickListener {
            val bundle = bundleOf("menuItem" to menuItem)
            it.findNavController().navigate(R.id.menuItemDetailFragment, bundle)
        }
    }

    fun fetchItems(newMenuItems : List<MenuItem>) {
        Log.d("MenuItemAdapter: ", "Received ${newMenuItems.size} items.")
        menuItems = newMenuItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = menuItems.size
}