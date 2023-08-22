package elfak.mosis.hotel.hotel

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class Hotel (
    val userId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val rating: Int = 0,
    var comments: String="",
    val capacity: Int = 0,
    val currentGuests: Int = 0
):Serializable