package id.librocanteen.adminapp.dashboard.objects

data class MenuItem(
    val itemNumber: Int = 0,
    val itemName: String = "",
    val itemDescription: String = "",
    val itemPrice: Int = 0,
    val itemPictureURL: String = ""
)
