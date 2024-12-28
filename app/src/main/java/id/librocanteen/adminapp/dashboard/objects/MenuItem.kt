package id.librocanteen.adminapp.dashboard.objects

import android.os.Parcel
import android.os.Parcelable


data class MenuItem(
    var nodeKey: String = "",
    var itemNumber: Int = 0,
    var itemName: String = "",
    var itemDescription: String = "",
    var itemStock: Int = 0,
    var itemPrice: Int = 0,
    var itemPictureURL: String = ""
) : Parcelable {
    constructor(parcel: Parcel): this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(nodeKey)
        dest.writeInt(itemNumber)
        dest.writeString(itemName)
        dest.writeString(itemDescription)
        dest.writeInt(itemPrice)
        dest.writeInt(itemStock)
        dest.writeString(itemPictureURL)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MenuItem> = object : Parcelable.Creator<MenuItem> {
            override fun createFromParcel(parcel: Parcel): MenuItem = MenuItem(parcel)
            override fun newArray(size: Int): Array<MenuItem?> = arrayOfNulls(size)
        }
    }
}
