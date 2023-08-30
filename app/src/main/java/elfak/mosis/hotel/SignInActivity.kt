package elfak.mosis.hotel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import elfak.mosis.hotel.databinding.ActivitySignInBinding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySignInBinding
    private lateinit var firebaseAuth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()

        binding.textView.setOnClickListener{
            val intent=Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener{
            val email=binding.emailEt.text.toString()
            val pass=binding.passET.text.toString()

            if(email.isNotEmpty() && email.isNotBlank() && pass.isNotEmpty() && pass.isNotBlank()){
                firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if(it.isSuccessful){
                        val intent=Intent(this,MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this,"Error with email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Empty fields are not allowed",Toast.LENGTH_SHORT).show()
            }
        }
    }
}