package id.librocanteen.adminapp

class Settings {
    val maxVendors : Int = 32
    val maxConsumables : Int = 16 // Set this to how many items that each vendor can sell. This is the hardcap, but you can change anytime.
    val appTitle: String ="Libro Canteen" // Feel free to change to something else.
    val appVersion: String = "0.0.1a"
    val secretPhrase: String = "ushiwakamaru" // Generate your own secret phase and replace this accordingly. The code on the left is used for encryption upon initially launching the app.
    val maxAdmins: Int = 1
}