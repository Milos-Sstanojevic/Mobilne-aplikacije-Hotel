package elfak.mosis.hotel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.service.autofill.UserData
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import elfak.mosis.hotel.databinding.ActivitySignUpBinding
import elfak.mosis.hotel.ui.theme.HotelTheme

class SignUpActivity : AppCompatActivity() {

    private var pickedImageUri: Uri? = null

    private val PROFILE_IMG_STORAGE = "profile_images"

    private lateinit var imageView: ImageView
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var firebaseStorage: FirebaseStorage

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val selectedImageUri: Uri? = intent.data

                    pickedImageUri = selectedImageUri
                    // Do something with the selectedImageUri, like display it in the ImageView
                    selectedImageUri?.let {
                        imageView.setImageURI(selectedImageUri)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        imageView = findViewById(R.id.imageView)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        binding.imageView.setOnClickListener {
            openGallery()
        }


        binding.button.setOnClickListener {
            val name = binding.nameEt.text.toString()
            val lastName = binding.lastNameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            val number = binding.numberEt.text.toString()
            val userName = binding.userNameEt.text.toString()
            val points=0

            if (number.isNotBlank() && number.isNotEmpty() && userName.isNotBlank() && name.isNotEmpty() && lastName.isNotBlank() && lastName.isNotEmpty() && email.isNotBlank() && pass.isNotEmpty() && pass.isNotBlank() && confirmPass.isNotBlank() && confirmPass.isNotEmpty()) {
                if (number.length != 10) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Mobile phone number must be in length of 10",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (pass == confirmPass) {
                        checkIfUsernameExists(userName) { isTaken ->
                            if (!isTaken) {
                                checkIfNumberExists(number) { exists ->
                                    if (!exists) {
                                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                    val user = firebaseAuth.currentUser
                                                    val request = UserProfileChangeRequest.Builder()
                                                        .setDisplayName(userName).build()
                                                    user?.updateProfile(request)

                                                    if (pickedImageUri != null) {
                                                        val photoRef =
                                                            firebaseStorage.reference.child(
                                                                PROFILE_IMG_STORAGE
                                                            ).child("$userName.jpg")
                                                        photoRef.putFile(pickedImageUri!!)
                                                            .addOnCompleteListener {
                                                                Toast.makeText(
                                                                    this@SignUpActivity,
                                                                    "Photo added",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }.addOnFailureListener {
                                                                Toast.makeText(
                                                                    this@SignUpActivity,
                                                                    "Failed photo upload",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                    }

                                                    saveUserDataToDatabase(
                                                        name,
                                                        lastName,
                                                        userName,
                                                        number,
                                                        points
                                                    )
                                                    Toast.makeText(
                                                        this@SignUpActivity,
                                                        "Account created",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    val intent =
                                                        Intent(this, SignInActivity::class.java)
                                                    startActivity(intent)
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        task.exception.toString(),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "User with that mobile number already exists",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            } else {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "User name is already taken",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    } else {
                        Toast.makeText(this, "Passwords not matching", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun saveUserDataToDatabase(
        name: String,
        lastName: String,
        userName: String,
        number: String,
        points: Int
    ) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val databaseReference = firebaseDatabase.reference.child("users").child(uid)
            val usernameReference = firebaseDatabase.reference.child("userNames")
            val numberRef = firebaseDatabase.reference.child("numbers")
            val pointsRef= firebaseDatabase.reference.child("points")

            val userData =
                elfak.mosis.hotel.model.UserData(name, lastName, userName, number,points)

            databaseReference.setValue(userData)
                .addOnSuccessListener {
                    usernameReference.child(userName).setValue(true).addOnSuccessListener {

                        numberRef.child(number).setValue(true).addOnSuccessListener {
                            val intent = Intent(this, SignInActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "User with that mobile number already exists",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, "Username is already taken!", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun checkIfNumberExists(number: String, onResult: (Boolean) -> Unit) {
        val databaseRef = firebaseDatabase.reference.child("numbers").child(number)
        databaseRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val dataSnapshot = task.result
                onResult(dataSnapshot.exists())
            } else {
                onResult(false)
            }
        }
    }

    private fun checkIfUsernameExists(userName: String, onResult: (Boolean) -> Unit) {
        val databaseReference = firebaseDatabase.reference.child("userNames").child(userName)
        databaseReference.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result
                    onResult(dataSnapshot.exists())
                } else {
                    onResult(false)
                }
            }
    }
}