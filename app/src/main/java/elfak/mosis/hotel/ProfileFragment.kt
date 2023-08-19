package elfak.mosis.hotel

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.hotel.databinding.ActivityMainBinding
import elfak.mosis.hotel.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class ProfileFragment : Fragment() {


    private lateinit var userNameTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var nameLastNameTextView: TextView
    private lateinit var numberTextView: TextView

    private lateinit var profileImageView:ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        auth=FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance()
        storage=FirebaseStorage.getInstance()

        var currentUser=auth.currentUser

        var databaseRef=database.reference.child("users").child(currentUser!!.uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userName = dataSnapshot.child("userName").getValue(String::class.java)
                    val email = currentUser.email
                    val number = dataSnapshot.child("number").getValue(String::class.java)
                    val name=dataSnapshot.child("name").getValue(String::class.java)
                    val lastName=dataSnapshot.child("lastName").getValue(String::class.java)
                    val points=dataSnapshot.child("points").getValue(String::class.java)

                    userNameTextView.text = userName
                    pointsTextView.text=points
                    emailTextView.text = email
                    nameLastNameTextView.text = name + " " + lastName
                    numberTextView.text = number

                    val storageRef=storage.reference.child("profile_images/$userName.jpg")

                    val localFile= File.createTempFile("tempProfile","jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        val bitmap=BitmapFactory.decodeFile(localFile.absolutePath)
                        profileImageView.setImageBitmap(bitmap)
                    }
                        .addOnFailureListener{
                            Toast.makeText(requireContext(),"Failed to fetch image",Toast.LENGTH_SHORT).show()
                        }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().printStackTrace()
            }
        })


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view=inflater.inflate(R.layout.fragment_profile,container,false)
        userNameTextView = view.findViewById(R.id.userName)
        emailTextView = view.findViewById(R.id.email)
        nameLastNameTextView = view.findViewById(R.id.nameLastName)
        numberTextView = view.findViewById(R.id.number)
        profileImageView=view.findViewById(R.id.profileImage)
        pointsTextView=view.findViewById(R.id.points)
        return view
    }

}