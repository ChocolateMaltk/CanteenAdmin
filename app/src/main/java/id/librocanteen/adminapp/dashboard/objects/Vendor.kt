package id.librocanteen.adminapp.dashboard.objects

import android.os.Parcel
import android.os.Parcelable

data class Vendor(
    val vendorNumber: Int = 0,
    val name: String = "",
    val standNumber: Int = 0,
    val description: String = "",
    val profilePictureURL: String = "",
    val bannerPictureURL : String = "",
    val menuItems: MutableList<MenuItem> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(MenuItem.CREATOR) ?: mutableListOf()
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
