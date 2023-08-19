package elfak.mosis.hotel

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.hotel.hotel.Hotel
import elfak.mosis.hotel.ui.theme.HotelTheme

class AddHotelActivity : ComponentActivity() {


    private lateinit var name:EditText
    private  lateinit var rating:NumberPicker
    private lateinit var capacity: EditText
    private lateinit var currentGuests: EditText
    private lateinit var submitButton: Button


    private lateinit var fusedLocationClient:FusedLocationProviderClient
    private lateinit var database:FirebaseDatabase
    private lateinit var auth:FirebaseAuth
    private lateinit var databaseRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_hotel)


        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
        database= FirebaseDatabase.getInstance()
        databaseRef=database.reference
        auth=FirebaseAuth.getInstance()


        name=findViewById(R.id.editTextHotelName)
        rating=findViewById(R.id.editTextHotelRating)
        rating.minValue=1
        rating.maxValue=5
        capacity=findViewById(R.id.editTextHotelCapacity)
        currentGuests=findViewById(R.id.editTextHotelCurrentGuests)
        submitButton=findViewById(R.id.submit_button)

        submitButton.setOnClickListener{
            val Name=name.text.toString()
            val rate=rating.value
            val cap=capacity.text.toString()
            val curr=currentGuests.text.toString()

            if(Name.isNullOrEmpty()){
                Toast.makeText(this,"You have to input a name of the hotel",Toast.LENGTH_LONG).show()
            }else if(cap.toInt() <=0){
                Toast.makeText(this,"You need to be able to take in at least 1 guest",Toast.LENGTH_LONG).show()
            }else if(curr.toInt() > cap.toInt()){
                Toast.makeText(this,"You can't have more guests than the capacity is!",Toast.LENGTH_LONG).show()
            }
            else{
                addHotel(Name,rate,cap.toInt(),curr.toInt())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun addHotel(name:String, rating:Int, capacity:Int, currentGuests:Int){
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if(location!=null){
                val user=auth.currentUser
                val userId=user?.uid

                if(userId!=null){
                    val geocoder=Geocoder(this)
                    val addresses=geocoder.getFromLocation(location.latitude,location.longitude,1)

                    if(addresses.isNullOrEmpty()){
                        Toast.makeText(this,"Error getting address!",Toast.LENGTH_SHORT).show()
                    }else{
                        val address=addresses[0].getAddressLine(0)

                        val hotel=Hotel(userId,name,address,location.latitude,location.longitude, rating, emptyList(),capacity,currentGuests)


                        val hotelRef=databaseRef.child("hotels").child(userId)

                        hotelRef.get().addOnSuccessListener { snapshot ->
                            var hotelExist=false

                            for(hotelSnap in snapshot.children){
                                Log.d("CAOOO","CAOOOO")
                                val existingHotel=hotelSnap.getValue(Hotel::class.java)
                                Log.d("CAOOO","$existingHotel")

                                if (existingHotel != null &&
                                    existingHotel.latitude == location.latitude &&
                                    existingHotel.longitude == location.longitude
                                ) {
                                    hotelExist = true
                                    break
                                }
                            }

                            if(hotelExist){
                                Toast.makeText(this, "A hotel already exists at this location", Toast.LENGTH_SHORT).show()
                            }
                            else{
                                hotelRef.push().setValue(hotel).addOnSuccessListener {
                                    val userRef=databaseRef.child("users").child(userId)
                                    userRef.child("points").get().addOnSuccessListener { dataSnapshot ->
                                        val currScore=dataSnapshot.value
                                        Log.d("POENIII","${dataSnapshot.value}")
                                        userRef.child("points").setValue(currScore.toString().toInt()+100).addOnSuccessListener {
                                            Toast.makeText(this,"Hotel added successfully!",Toast.LENGTH_LONG).show()
                                            finish()
                                        }
                                            .addOnFailureListener{
                                                Toast.makeText(this,"Failed to add hotel!",Toast.LENGTH_LONG).show()
                                            }
                                    }
                                        .addOnFailureListener{
                                            Toast.makeText(this,"Failed for get score of the user",Toast.LENGTH_LONG).show()
                                        }
                                }
                                    .addOnFailureListener{
                                        Toast.makeText(this,"Failed to add hotel!",Toast.LENGTH_LONG).show()
                                    }
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener{
            Log.d("LOCATION ERROR", "DID NOT GET LOCATION")
        }
    }
}