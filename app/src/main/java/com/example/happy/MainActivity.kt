package com.example.happy


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

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
                    createUserIfNotExists()
                } else {
                    Toast.makeText(baseContext, "Erro de autenticação com google", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun createUserIfNotExists() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val databaseUserIdRef = database
            .child("users")
            .child(userId)

        databaseUserIdRef
               .child("role")
               .get()
               .addOnCompleteListener { task ->
                   if (task.isSuccessful) {
                       val snapshot = task.result

                       if (snapshot.exists()) {
                           openMain()
                       } else {
                           val email = currentUser.email!!
                           val name = currentUser.displayName!!
                           val user = User(email, name)

                           databaseUserIdRef
                               .get()
                               .addOnCompleteListener { task ->
                                   if (task.isSuccessful) {
                                       val snapshot = task.result

                                       if (!snapshot.exists()) {
                                           databaseUserIdRef.setValue(user)
                                       }

                                       openMain()
                                   }
                               }
                       }
                   }
               }
    }

    override fun onStart() {
        super.onStart()
        createUserIfNotExists()
    }

    private fun openMain() {
        val intent = Intent(this, FirstTutorial::class.java)
        startActivity(intent)
        finish()
    }
}

