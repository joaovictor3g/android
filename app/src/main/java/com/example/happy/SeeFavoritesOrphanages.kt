package com.example.happy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class SeeFavoritesOrphanages : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var layout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_favorites_orphanages)
        title = "Orfanatos favoritados"

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        layout = findViewById(R.id.container)

        updateInterface()
    }

    private fun updateInterface() {
        val currentUser = auth.currentUser ?: return

        database
            .child("orphanages_users")
            .child(currentUser.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val isFavorited = child.child("isFavorited").getValue(Boolean::class.java)

                            if (isFavorited == true) {
                                val orphanageId = child.child("orphanageId").getValue(String::class.java)
                                renderFavorites(orphanageId)

                            }
                        }
                    }
                }
            }

    }

    private fun renderFavorites(orphanageId: String?) {
        database
            .child("orphanages")
            .child(orphanageId!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        val orphanage = snapshot.getValue(Orphanage::class.java)

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            )

                        val boxLayout = LinearLayout(this)
                        boxLayout.background = ContextCompat.getDrawable(this, R.drawable.edit_text_border_outline)

                        val orphanageNameTextView = TextView(this)
                        orphanageNameTextView.text = orphanage?.name.toString()
                        orphanageNameTextView.textSize = 20f

                        boxLayout.addView(orphanageNameTextView)

                        boxLayout.setPadding(16, 16,16,16)
                        boxLayout.layoutParams = layoutParams
                        layoutParams.setMargins(24,24,24,24)

                        boxLayout.setOnClickListener {
                            val intent = Intent(this, OrphanageDetailActivity::class.java)
                            intent.putExtra("orphanage_id", orphanageId)
                            startActivity(intent)
                            finish()
                        }

                        layout.addView(boxLayout)
                    }
                }
            }
    }

}