package elfak.mosis.hotel

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import elfak.mosis.hotel.ui.theme.HotelTheme

class ReserveActivity : ComponentActivity() {

    private lateinit var database:FirebaseDatabase
    private lateinit var auth:FirebaseAuth
    private lateinit var numOfGuests:EditText
    private lateinit var reserve:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_reserve)

        database=FirebaseDatabase.getInstance()
        auth=FirebaseAuth.getInstance()

        numOfGuests=findViewById(R.id.number_reserve)
        reserve=findViewById(R.id.reserve)


        val currentUser=auth.currentUser

        val currentPlaceName=intent.getSerializableExtra("selectedPlace")

        var databaseRefHotel=database.reference.child("hotels").child(currentPlaceName.toString())


        var databaseRefReservations=database.reference.child("reservations").child(currentUser!!.uid)

        reserve.setOnClickListener {

            val userRef=database.reference.child("users").child(currentUser.uid)
            userRef.child("userName").get().addOnSuccessListener { snapshot ->
                var userName=snapshot.value

                val reservationData=mapOf("userName" to userName, "hotel" to currentPlaceName.toString())

                databaseRefReservations.setValue(reservationData).addOnSuccessListener {
                    databaseRefHotel.child("capacity").get().addOnSuccessListener {dataSnapshot->

                        var capacity=dataSnapshot.value

                        databaseRefHotel.child("currentGuests").get().addOnSuccessListener { dataSnapshot ->

                            var current=dataSnapshot.value

                            if(current.toString().toInt()+numOfGuests.text.toString().toInt()<=capacity.toString().toInt()){
                                databaseRefHotel.child("currentGuests").setValue(current.toString().toInt()+numOfGuests.text.toString().toInt()).addOnSuccessListener {
                                    Toast.makeText(this,"Successfully reserved a hotel",Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                    .addOnFailureListener{
                                        Log.d("ERRORR","Error with setting value of new guests")
                                    }
                            }
                            else{
                                Toast.makeText(this,"There is not enough space in this hotel",Toast.LENGTH_LONG).show()
                            }
                        }.addOnFailureListener{
                            Log.d("ERRORR","Error with getting value of current guests")
                        }

                    }
                        .addOnFailureListener{
                            Log.d("ERRORR","Error with getting value of capacity")
                        }
                    }
                }


            }


    }
}
