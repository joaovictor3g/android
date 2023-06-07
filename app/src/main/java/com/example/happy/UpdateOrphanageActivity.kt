package com.example.happy

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.widget.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.util.*

class UpdateOrphanageActivity : AppCompatActivity() {
    private lateinit var orphanageId: String

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    private lateinit var coords: Orphanage.Coords

    private lateinit var orphanageImageView: ImageView
    private var imageUri: Uri? = null
    private lateinit var uploadButton: Button
    private lateinit var buttonSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_orphanage)
        orphanageId = intent.getStringExtra("orphanage_id").toString()

        database =  FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        orphanageImageView = findViewById<ImageView>(R.id.image_view)
        uploadButton = findViewById<Button>(R.id.img_pick_btn)
        uploadButton.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, IMAGE_PICK_CODE)
        }

        buttonSubmit = findViewById(R.id.button_next)
        buttonSubmit.setOnClickListener {
            handleCreateOrphanage()
            val intent = Intent(this, OrphanageDetailActivity::class.java)
            intent.putExtra("orphanage_id", orphanageId)
            startActivity(intent)
            finish()
        }

        updateInterface()
    }

    private fun updateInterface() {
        val orphangeRef = database
            .child("orphanages")
            .child(orphanageId)

        orphangeRef
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        val orphanage = snapshot.getValue(Orphanage::class.java)
                        val coordsTextView = findViewById<TextView>(R.id.coords_text_view)
                        val nameTextEditText = findViewById<EditText>(R.id.edit_text_name)
                        val aboutEditText = findViewById<EditText>(R.id.edit_text_about)
                        val wppEditText = findViewById<EditText>(R.id.edit_text_whatsapp)
                        val imageView = findViewById<ImageView>(R.id.image_view)
                        val visitInstructionsTextView = findViewById<TextView>(R.id.edit_text_instructions)
                        val visitTimeTextView = findViewById<TextView>(R.id.edit_text_visit_time)
                        val switch = findViewById<Switch>(R.id.switch_weekends_on)

                        title = orphanage?.name

                        coords = orphanage?.coords!!

                        if (orphanage != null) {
                            coordsTextView.text = "Você está com as seguintes coordenadas: ${orphanage.coords?.latitude.toString()},${orphanage.coords?.longitude.toString()}"

                            val nameEditable = makeEditable(orphanage.name)
                            nameTextEditText.text = nameEditable

                            val aboutEditable = makeEditable(orphanage.about)
                            aboutEditText.text = aboutEditable

                            val wppEditable = makeEditable(orphanage.whatsapp)
                            wppEditText.text = wppEditable

                            Picasso
                                .get()
                                .load(orphanage.imageUrl)
                                .fit()
                                .centerCrop()
                                .placeholder(R.drawable.image_default)
                                .into(imageView)

                            val visitInstructionsEditable = makeEditable(orphanage.visit?.instructions)
                            visitInstructionsTextView.text = visitInstructionsEditable

                            val visitTimeEditable = makeEditable(orphanage.visit?.time)
                            visitTimeTextView.text = visitTimeEditable

                            switch.isChecked = orphanage.weekendsOn!!
                        }
                    }
                }
            }

    }

    private fun createNewOrphanage(imageUrl: String) {
        val name: EditText = findViewById(R.id.edit_text_name)
        val about: EditText = findViewById(R.id.edit_text_about)
        val whatsapp: EditText = findViewById(R.id.edit_text_whatsapp)
        val visitInstructions: EditText = findViewById(R.id.edit_text_instructions)
        val visitTime: EditText = findViewById(R.id.edit_text_visit_time)
        val weekendsOn: Switch = findViewById(R.id.switch_weekends_on)

        val visit = Orphanage.Visit(visitInstructions.text.toString(), visitTime.text.toString())
        val isOpenOnWeekends = weekendsOn.isChecked

        val email = auth.currentUser?.email!!
        val user = User(email)

        val orphanage = Orphanage(
            orphanageId,
            name.text.toString(),
            about.text.toString(),
            whatsapp.text.toString(),
            visit,
            coords,
            user,
            isOpenOnWeekends,
            imageUrl = imageUrl
        )

        try {
            database.child("orphanages").child(orphanageId).setValue(orphanage)
            Toast.makeText(this, "Orfanato atualizado com sucesso", Toast.LENGTH_SHORT).show()
        } catch (exception: FirebaseException) {
            Toast.makeText(this, "Erro ao atualizar orfanato", Toast.LENGTH_SHORT).show()

        }
    }

    private fun handleCreateOrphanage() {
        buttonSubmit.text = "Aguarde..."
        val drawable = orphanageImageView.drawable as BitmapDrawable
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

    private fun makeEditable(text: String?): Editable {
        return Editable.Factory.getInstance().newEditable(text)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            imageUri = data?.data
            orphanageImageView.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 100
    }
}