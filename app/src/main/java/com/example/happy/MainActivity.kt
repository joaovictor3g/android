package com.example.happy


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("82696240037-tk0v9rhjbjpvggbjuvm94geb9s67ov1v.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val button: Button = findViewById(R.id.signin_button)
            button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val intent = googleSignInClient.signInIntent
        openActivity.launch(intent)
    }

    private var openActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)
                    loginWithGoogle(account.idToken!!)

                } catch (exception: ApiException) {
                    Toast.makeText(baseContext, exception.message, Toast.LENGTH_SHORT).show()
                }

            }
    }

    private fun loginWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
            task: Task<AuthResult> ->
                if(task.isSuccessful) {
                    Toast.makeText(baseContext, "Autenticação efetuada com google", Toast.LENGTH_SHORT).show()
                    openMain()
                } else {
                    Toast.makeText(baseContext, "Erro de autenticação com google", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openMain() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }


}

