package elfak.mosis.hotel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.ktx.widget.PlaceSelectionError
import com.google.android.libraries.places.ktx.widget.PlaceSelectionSuccess
import com.google.android.libraries.places.ktx.widget.placeSelectionEvents
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.StringUtil

class SearchActivity : AppCompatActivity() {

    private lateinit var autocompleteContainer: View
    private lateinit var radioRegular: RadioButton
    private lateinit var radioName: RadioButton
    private lateinit var radioRadius: RadioButton
    private lateinit var nameInput: EditText
    private lateinit var radioG: RadioGroup
    private lateinit var searchB: Button

    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        Places.initialize(applicationContext, "AIzaSyDp5KJGyaPWzwlton_QL_1JK0uA-Vfipro")

        autocompleteContainer = findViewById(R.id.autocomplete_fragment)
        nameInput = findViewById(R.id.hotelName)
        database = FirebaseDatabase.getInstance()


        radioG = findViewById(R.id.radioGroup)
        radioRegular = findViewById(R.id.radioRegular)
        radioName = findViewById(R.id.radioName)
        radioRadius = findViewById(R.id.radioRadius)
        searchB=findViewById(R.id.search_button)

        radioRegular.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                Log.d("EEEE", "ALOOOO")

                nameInput.setVisibility(View.INVISIBLE)
                autocompleteContainer.setVisibility(View.VISIBLE)

                val autocompleteFragment =
                    supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                            as AutocompleteSupportFragment

                autocompleteFragment.setPlaceFields(
                    listOf(
                        Place.Field.NAME,
                        Place.Field.ID,
                        Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                )

                lifecycleScope.launchWhenCreated {
                    autocompleteFragment.placeSelectionEvents().collect { event ->
                        when (event) {
                            is PlaceSelectionSuccess -> {
                                val place = event.place
                                val resultIntent = Intent()
                                resultIntent.putExtra("selectedPlace", place)
                                setResult(Activity.RESULT_OK, resultIntent)
                                Toast.makeText(
                                    this@SearchActivity,
                                    "${StringUtil.stringifyAutocompleteWidget(place, false)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            is PlaceSelectionError -> Toast.makeText(
                                this@SearchActivity,
                                "Failed to get place '${event.status.statusMessage}'",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        radioName.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                nameInput.setVisibility(View.VISIBLE)
                autocompleteContainer.setVisibility(View.INVISIBLE)
                searchB.setVisibility(View.VISIBLE)


                Log.d("EEEE", "ALOOOO222 ${nameInput.visibility}")

                searchB.setOnClickListener {
                    var hotelName = nameInput.text.toString()

                    val hotelRef = database.reference.child("hotels")
                    val query = hotelRef.orderByChild("name").equalTo(hotelName)

                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var matchingHotels: Hotel? = null

                            for (hotelSnap in dataSnapshot.children) {
                                val hotel = hotelSnap.getValue(Hotel::class.java)
                                if (hotel != null) {
                                    matchingHotels = hotel
                                    break
                                }
                            }
                            Log.d("HOTEEELLL1111111","${matchingHotels}")
                            if (matchingHotels != null) {


                                val resultIntent = Intent()
                                resultIntent.putExtra("matchingHotels", matchingHotels)
                                setResult(Activity.RESULT_OK, resultIntent)
                            } else {
                                setResult(Activity.RESULT_CANCELED)
                            }
                            finish()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
            }
        }
    }
}
//        else if(radioRadius.isChecked()){
//            TODO("OVDE CES PRETRAGU SVIH OBJEKATA U RADIUSU ALI SAMO ONIH KOJE SU KORISNICI NAPRAVILI. ZNACI SVI U RADIUSU DOBIJAJU MARKER")
//        }
//    }
