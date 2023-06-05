package com.example.happy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class CreateOrphanageActivity: AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0

    private lateinit var imageView: ImageView
    private var imageUri: Uri? = null
    private lateinit var uploadButton: Button
    private lateinit var buttonSubmit: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_new_orphanage)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        buttonSubmit = findViewById(R.id.button_next)
        buttonSubmit.setOnClickListener {
            handleCreateOrphanage()
        }
        val coordsTextView: TextView = findViewById(R.id.coords_text_view)
        coordsTextView.text = "Você está com as seguintes coordenadas: lat:${latitude}, long${longitude}"

        imageView = findViewById<ImageView>(R.id.image_view)
        uploadButton = findViewById<Button>(R.id.img_pick_btn)
        uploadButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, IMAGE_PICK_CODE)
        }
    }

    private fun createNewOrphanage(imageUrl: String) {
        val name: EditText = findViewById(R.id.edit_text_name)
        val about: EditText = findViewById(R.id.edit_text_about)
        val whatsapp: EditText = findViewById(R.id.edit_text_whatsapp)
        val visitInstructions: EditText = findViewById(R.id.edit_text_instructions)
        val visitTime: EditText = findViewById(R.id.edit_text_visit_time)
        val weekendsOn: Switch = findViewById(R.id.switch_weekends_on)

        val coords = Orphanage.Coords(latitude, longitude)
        val visit = Orphanage.Visit(visitInstructions.text.toString(), visitTime.text.toString())
        val id = UUID.randomUUID().toString()
        val isOpenOnWeekends = weekendsOn.isChecked

        val email = auth.currentUser?.email!!
        val user = User(email)

        val orphanage = Orphanage(
            id,
            name.text.toString(),
            about.text.toString(),
            whatsapp.text.toString(),
            visit, coords,
            user,
            isOpenOnWeekends,
            imageUrl = imageUrl
        )

        try {
            database.child("orphanages").child(id).setValue(orphanage)
            Toast.makeText(this, "Orfanato criado com sucesso", Toast.LENGTH_SHORT).show()
            goToMap()
        } catch (exception: FirebaseException) {
            Toast.makeText(this, "Erro ao criar orfanato", Toast.LENGTH_SHORT).show()

        }
    }

    private fun goToMap() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun validateUser (): Boolean {
        val currentUser = auth.currentUser
        var hasAdminRole = false

        if (currentUser != null) {
            database
                .child("users")
                .child(currentUser.uid)
                .child("role")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result

                        if (snapshot.exists()) {
                            val role = snapshot.getValue(String::class.java)
                            if (role == "admin") hasAdminRole = true
                        }
                    }
                }

        }

        return hasAdminRole
    }


    private fun handleCreateOrphanage() {
        buttonSubmit.text = "Aguarde..."
        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val timestamp = System.currentTimeMillis()
        val imageName = "$timestamp.jpg"
        val storageRef = storage.getReference(imageName)
        val uploadTask = storageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                createNewOrphanage(imageUrl)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Falha ao criar o orfanato", Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        private const val IMAGE_PICK_CODE = 100
    }
}