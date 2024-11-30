package id.librocanteen.adminapp.dashboard.objects

data class Vendor(
    val vendorNumber: Int = 0,
    val name: String = "",
    val standNumber: Int = 0,
    val description: String = "",
    val profilePictureURL: String = "",
    val bannerPictureURL : String = "",
    val menuItems: MutableList<MenuItem> = mutableListOf()
)
