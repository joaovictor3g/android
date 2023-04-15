package com.example.happy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CreateOrphanageActivity: AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_new_orphanage)

        database = FirebaseDatabase.getInstance().reference

        val extras = intent.extras
        Toast.makeText(baseContext, "${extras?.getString("latitude")} ${extras?.getString("longitude")}", Toast.LENGTH_SHORT).show()

        val buttonNext: Button = findViewById(R.id.button_next)
        buttonNext.setOnClickListener {
            createNewOrphanage()
        }

    }

    private fun createNewOrphanage() {
        val name: EditText = findViewById(R.id.edit_text_name)
        val about: EditText = findViewById(R.id.edit_text_about)
        val whatsapp: EditText = findViewById(R.id.edit_text_whatsapp)
        val visitInstructions: EditText = findViewById(R.id.edit_text_instructions)
        val visitTime: EditText = findViewById(R.id.edit_text_visit_time)
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        val coords = Coords(latitude, longitude)
        val visit = Visit(visitInstructions.text.toString(), visitTime.text.toString())

        val orphanage = Orphanage(name.text.toString(), about.text.toString(), whatsapp.text.toString(), visit, coords)
        try {
            database.child("orphanages").child(UUID.randomUUID().toString()).setValue(orphanage)
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


    data class Orphanage(val name: String? = "", val about: String? = "", val whatsapp: String? = "", val visit: Visit? = Visit(), val coords: Coords? = Coords()) {
    }

    data class Visit(val instructions: String? = "", val time: String? = "") {}

    data class Coords(val latitude: Double? = 0.0, val longitude: Double? = 0.0) {}
}