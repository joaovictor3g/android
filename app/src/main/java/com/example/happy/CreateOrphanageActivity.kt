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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_new_orphanage)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        val buttonNext: Button = findViewById(R.id.button_next)
        buttonNext.setOnClickListener {
            createNewOrphanage()
        }
        val coordsTextView: TextView = findViewById(R.id.coords_text_view)
        coordsTextView.text = "Você está com as seguintes coordenadas: lat:${latitude}, long${longitude}"

        val imgPickerBtn = findViewById<Button>(R.id.img_pick_btn)
        imgPickerBtn.setOnClickListener {
            //check runtime permission
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
//                    PackageManager.PERMISSION_DENIED){
//                    //permission denied
//                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
//                    //show popup to request runtime permission
//                    requestPermissions(permissions, PERMISSION_CODE);
//                }
//                else{
//                    //permission already granted
//                    pickImageFromGallery()
//                }
//            }
//            else{
//                //system OS is < Marshmallow
//                pickImageFromGallery()
//            }
//        }
            pickImageFromGallery()
        }
    }

    private fun createNewOrphanage() {
        val name: EditText = findViewById(R.id.edit_text_name)
        val about: EditText = findViewById(R.id.edit_text_about)
        val whatsapp: EditText = findViewById(R.id.edit_text_whatsapp)
        val visitInstructions: EditText = findViewById(R.id.edit_text_instructions)
        val visitTime: EditText = findViewById(R.id.edit_text_visit_time)
        val weekendsOn: Switch = findViewById(R.id.switch_weekends_on)
        val image: ImageView = findViewById(R.id.image_view)

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
        )

        try {
//            var profileImage = PreferenceManager.getDefaultSharedPreferences(this).getString(MediaStore.EXTRA_OUTPUT, null)
//            val file = Uri.fromFile(File(Uri.parse(profileImage)))
//            val uploadRef = storage.reference.child("images/${file.lastPathSegment}")
//            val uploadTask = uploadRef.putFile(file)
//
//            uploadTask.addOnFailureListener {
//                Log.i("Error to upload", it.toString())
//            }.addOnSuccessListener {
//                Log.i("Success to upload", it.toString())
//            }

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


    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickImageFromGallery()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //handle result of picked image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            val imageView = findViewById<ImageView>(R.id.image_view)
            imageView.setImageURI(data?.data)
        }
    }

    fun setupImage(imageView: ImageView): ByteArray {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return data
    }
}