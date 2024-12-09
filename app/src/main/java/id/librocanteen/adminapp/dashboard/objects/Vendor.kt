package id.librocanteen.adminapp.dashboard.objects

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

data class Vendor(
    var nodeKey: String? = null, // Firebase node key
    var vendorAccessKey: String? = null, // Access key to open vendor profile in customer/vendor application.
    val vendorNumber: Int = 0,
    val name: String = "",
    val standNumber: Int = 0,
    val description: String = "",
    val profilePictureURL: String = "",
    val bannerPictureURL: String = "",
    val menuItems: MutableList<MenuItem> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        vendorNumber = parcel.readInt(), // vendorNumber
        name = parcel.readString() ?: "", // name
        standNumber = parcel.readInt(), // standNumber
        description = parcel.readString() ?: "", // description
        profilePictureURL = parcel.readString() ?: "", // profilePictureURL
        bannerPictureURL = parcel.readString() ?: "", // bannerPictureURL
        menuItems = parcel.createTypedArrayList(MenuItem.CREATOR) ?: mutableListOf() // menuItems
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(vendorNumber)
        parcel.writeString(name)
        parcel.writeInt(standNumber)
        parcel.writeString(description)
        parcel.writeString(profilePictureURL)
        parcel.writeString(bannerPictureURL)
        parcel.writeTypedList(menuItems)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Vendor> {
        override fun createFromParcel(parcel: Parcel): Vendor {
            return Vendor(parcel)
        }

        override fun newArray(size: Int): Array<Vendor?> {
            return arrayOfNulls(size)
        }
    }
}

