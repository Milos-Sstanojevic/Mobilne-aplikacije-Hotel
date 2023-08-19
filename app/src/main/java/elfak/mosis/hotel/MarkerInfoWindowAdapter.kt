package elfak.mosis.hotel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.Place

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    private lateinit var view:View

    override fun getInfoContents(marker: Marker): View? {

        if(marker.tag is Place) {
            //val place = marker?.tag as? Place ?: return null
            val place = marker.tag as Place
            view = LayoutInflater.from(context).inflate(
                R.layout.marker_info_contents, null
            )
            view.findViewById<TextView>(
                R.id.text_view_title
            ).text = place.name
            view.findViewById<TextView>(
                R.id.text_view_address
            ).text = place.address
            view.findViewById<TextView>(
                R.id.text_view_rating
            ).text = "Rating: %.2f".format(place.rating)
        }

        if(marker.tag is Hotel){
            val place = marker.tag as Hotel
            view = LayoutInflater.from(context).inflate(
                R.layout.marker_info_contents, null
            )
            view.findViewById<TextView>(
                R.id.text_view_title
            ).text = place.name
            view.findViewById<TextView>(
                R.id.text_view_address
            ).text = place.address
            view.findViewById<TextView>(
                R.id.text_view_rating
            ).text = "Rating: "+place.rating
        }

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }


}