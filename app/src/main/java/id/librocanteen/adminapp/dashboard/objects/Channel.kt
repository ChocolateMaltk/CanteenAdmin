package id.librocanteen.adminapp.dashboard.objects

data class Channel(
    val channelName: String = "",
    val users: MutableList<String> = mutableListOf<String>()
)
{

}

