package elfak.mosis.hotel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentPlaceActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var userName:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_place)


        database = FirebaseDatabase.getInstance()

        auth=FirebaseAuth.getInstance()

        var currentUser=auth.currentUser

        var databaseRef=database.reference.child("users").child(currentUser!!.uid)

        val commentEditText = findViewById<EditText>(R.id.commentEditText)
        val addCommentButton = findViewById<Button>(R.id.addCommentButton)



        val currentPlaceName=intent.getSerializableExtra("selectedPlace")


        databaseRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    userName = snapshot.child("userName").getValue(String::class.java).toString()

                    addCommentButton.setOnClickListener{
                        val commentText=commentEditText.text.toString().trim()

                        if(commentText.isNotEmpty()){
                            val commentsRef = database.reference.child("places")
                                .child(currentPlaceName.toString())
                                .child("comments")
                                .child(currentUser.uid)


                            commentsRef.addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot){
                                    if(!snapshot.exists()){

                                        val commentData=mapOf("userName" to userName, "comment" to commentText)

                                        commentsRef.setValue(commentData)
                                        commentEditText.setText("")

                                        val userRef=database.reference.child("users").child(currentUser.uid)

                                        userRef.child("points").get().addOnSuccessListener { dataSnapshot ->
                                            val currScore=dataSnapshot.value

                                            userRef.child("points").setValue(currScore.toString().toInt()+10).addOnSuccessListener {
                                                Toast.makeText(this@CommentPlaceActivity,"Successfully commented place",Toast.LENGTH_SHORT).show()
                                                finish()
                                            }
                                        }
                                            .addOnFailureListener{
                                                Toast.makeText(this@CommentPlaceActivity,"Failed to add comment",Toast.LENGTH_SHORT).show()
                                            }

                                    }else{
                                        Toast.makeText(this@CommentPlaceActivity,"You can't comment same place twice!",Toast.LENGTH_LONG).show()                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    error.toException().printStackTrace()
                                }
                            })
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                databaseError.toException().printStackTrace()
            }
        })


    }

}
