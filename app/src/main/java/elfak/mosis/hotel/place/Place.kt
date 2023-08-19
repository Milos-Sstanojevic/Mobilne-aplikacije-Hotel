package elfak.mosis.hotel.place

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import android.os.Parcelable


data class Place(
    val name: String,
    val latLng: LatLng,
    val address: String,
    val rating: Double,
    val comment: List<String>
):Serializable