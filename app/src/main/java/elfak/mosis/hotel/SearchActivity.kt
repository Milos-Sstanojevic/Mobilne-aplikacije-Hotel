package elfak.mosis.hotel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore.Audio.Radio
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.fitness.data.Value
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.ktx.widget.PlaceSelectionError
import com.google.android.libraries.places.ktx.widget.PlaceSelectionSuccess
import com.google.android.libraries.places.ktx.widget.placeSelectionEvents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.place.StringUtil
import java.io.Serializable

class SearchActivity : AppCompatActivity() {

    private lateinit var autocompleteContainer: View
    private lateinit var radioRegular: RadioButton
    private lateinit var radioName: RadioButton
    private lateinit var radioRadius: RadioButton
    private lateinit var nameInput: EditText
    private lateinit var radioG: RadioGroup
    private lateinit var searchB: Button
    private lateinit var auth:FirebaseAuth

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var database: FirebaseDatabase



    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        Places.initialize(applicationContext, "AIzaSyDp5KJGyaPWzwlton_QL_1JK0uA-Vfipro")

        autocompleteContainer = findViewById(R.id.autocomplete_fragment)
        nameInput = findViewById(R.id.hotelName)
        database = FirebaseDatabase.getInstance()
        auth=FirebaseAuth.getInstance()

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)

        var currentUser=auth.currentUser

        radioG = findViewById(R.id.radioGroup)
        radioRegular = findViewById(R.id.radioRegular)
        radioName = findViewById(R.id.radioName)
        radioRadius = findViewById(R.id.radioRadius)
        searchB=findViewById(R.id.search_button)



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

        radioRegular.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                Log.d("EEEE", "ALOOOO")

                nameInput.setVisibility(View.INVISIBLE)
                autocompleteContainer.setVisibility(View.VISIBLE)
                searchB.setVisibility(View.INVISIBLE)


            }
        }
        radioName.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                nameInput.setVisibility(View.VISIBLE)
                autocompleteContainer.setVisibility(View.INVISIBLE)
                searchB.setVisibility(View.VISIBLE)
                nameInput.inputType=InputType.TYPE_CLASS_TEXT

                Log.d("EEEE", "ALOOOO222 ${nameInput.visibility}")

                searchB.setOnClickListener {
                    var hotelName = nameInput.text.toString()


                    if(hotelName.isNullOrBlank()){
                        Toast.makeText(this,"You have to insert a name of hotel",Toast.LENGTH_SHORT).show()
                    }else{

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

                            if (matchingHotels != null) {

                                val resultIntent = Intent()
                                resultIntent.putExtra("matchingHotels", matchingHotels)
                                setResult(Activity.RESULT_OK, resultIntent)
                            } else {
                                Toast.makeText(this@SearchActivity,"There is no hotels with inserted name",Toast.LENGTH_LONG).show()
                                setResult(Activity.RESULT_CANCELED)
                            }
                            finish()
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.d("ERROR","$databaseError")
                        }
                    })
                }
                }
            }
        }

        radioRadius.setOnCheckedChangeListener{_,isChecked ->
            if(isChecked) {
                nameInput.setVisibility(View.VISIBLE)
                autocompleteContainer.setVisibility(View.INVISIBLE)
                searchB.setVisibility(View.VISIBLE)
                nameInput.inputType = InputType.TYPE_CLASS_NUMBER
                nameInput.hint="Radius"
                var radius:Int=0

                val radiusTextWatcher=object: TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        val radiusInput = s.toString()
                        if (radiusInput.isNotBlank() && radiusInput.matches("\\d+".toRegex())) {
                            radius = radiusInput.toInt()
                            searchB.isEnabled = true
                        } else {
                            radius = 0
                            searchB.isEnabled = false
                        }
                    }
                }

                nameInput.addTextChangedListener(radiusTextWatcher)

                    searchB.setOnClickListener {
                        if(radius!=0){
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if(location!=null){
                                val userLocation=LatLng(location.latitude,location.longitude)

                                filterHotels(userLocation,radius){filteredHotels ->
                                    if(filteredHotels!=null){
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("radiusHotels", ArrayList(filteredHotels))
                                        setResult(Activity.RESULT_OK, resultIntent)
                                    } else {
                                        setResult(Activity.RESULT_CANCELED)
                                    }
                                    finish()
                                }
                            }
                        }.addOnFailureListener{
                            Log.d("LOCATION ERROR", "DID NOT GET LOCATION")
                        }
                    }
                }

            }
        }
    }

    private fun filterHotels(userLocation:LatLng,radius:Int, callback:(List<Hotel>)->Unit){

        val hotelRef=database.reference.child("hotels")
        val filteredHotels: MutableList<Hotel> = mutableListOf()

        hotelRef.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                for(hotelSnapshot in snapshot.children){
                    val hotel=hotelSnapshot.getValue(Hotel::class.java)

                    if(hotel!=null){
                        val hotelLatLng = LatLng(hotel.latitude, hotel.longitude)
                        val distance = calculateDistance(userLocation, hotelLatLng)

                        if(distance<=radius){
                            filteredHotels.add(hotel)
                        }
                    }
                }

                callback(filteredHotels)
            }

            override fun onCancelled(databaseError: DatabaseError){
                Log.d("ERROR","Greska sa bazom")
            }
        })
    }

    private fun calculateDistance(loc1:LatLng,loc2:LatLng):Float{
        val results=FloatArray(1)
        Location.distanceBetween(
            loc1.latitude,loc1.longitude,loc2.latitude,loc2.longitude,results
        )

        return results[0]
    }
}

