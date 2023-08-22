package elfak.mosis.hotel

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.Place

data class Comment(val userName: String, val commentText: String)




class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {

    private lateinit var view:View
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private var comments:String?=""
    override fun getInfoContents(marker: Marker): View? {


        database = FirebaseDatabase.getInstance()

        auth= FirebaseAuth.getInstance()


        view = LayoutInflater.from(context).inflate(
            R.layout.marker_info_contents, null
        )



        if(marker.tag is Place) {
            val place = marker.tag as Place

            view.findViewById<TextView>(
                R.id.text_view_title
            ).text = place.name
            view.findViewById<TextView>(
                R.id.text_view_address
            ).text = place.address
            view.findViewById<TextView>(
                R.id.text_view_rating
            ).text = "Rating: %.2f".format(place.rating)

            view.findViewById<TextView>(R.id.comments).text ="Comments: \n" + place.comment
            Log.d("EEJJ","${view.findViewById<TextView>(R.id.comments).text}")
        }

        if(marker.tag is Hotel){
            val place = marker.tag as Hotel

            view.findViewById<TextView>(
                R.id.text_view_title
            ).text = place.name
            view.findViewById<TextView>(
                R.id.text_view_address
            ).text = place.address
            view.findViewById<TextView>(
                R.id.text_view_rating
            ).text = "Rating: "+place.rating
            view.findViewById<TextView>(
                R.id.current_guests
            ).text = "Current number of guests: "+place.currentGuests.toString()
            view.findViewById<TextView>(
                R.id.guest_limit
            ).text="Guest capacity: "+place.capacity.toString()

            view.findViewById<TextView>(R.id.comments).text ="Comments: \n"+place.comments
            Log.d("EEJJ","${view.findViewById<TextView>(R.id.comments).text}")
        }


        return view

    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }


}