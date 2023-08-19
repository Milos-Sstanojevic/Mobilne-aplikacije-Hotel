package elfak.mosis.hotel.place

import com.google.android.gms.maps.model.LatLng

data class PlaceResponse(
    var image: String,
    val geometry: Geometry,
    val name: String,
    val vicinity: String,
    val rating: Double,
    val comment:List<String>
) {

    data class Geometry(
        val location: GeometryLocation
    )

    data class GeometryLocation(
        val lat: Double,
        val lng: Double
    )
}

fun PlaceResponse.toPlace(): Place {
    return Place(
        name = name,
        latLng = LatLng(geometry.location.lat, geometry.location.lng),
        address = vicinity,
        rating = rating,
        comment=comment
    )
}
