package com.example.happy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrphanageDetailActivity: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    var orphanageId: String? = null
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orphanage_detail)
        orphanageId = intent.getStringExtra("orphanage_id")
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sendMessage()
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        val zoomlevel = 15f

        val orphanageRef = FirebaseDatabase.getInstance().getReference("orphanages")
            .child(orphanageId ?: "")
        orphanageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orphanage = snapshot.getValue(Orphanage::class.java)
                val position =
                    LatLng(orphanage?.coords?.latitude ?: 0.0, orphanage?.coords?.longitude ?: 0.0)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomlevel))
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(orphanage?.name)
                        .icon(bitmapDescriptorFromVector(baseContext, R.drawable.happy_marker))

                )
                populateData(orphanage)
                supportActionBar?.setTitle(orphanage?.name)

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun populateData(orphanage: Orphanage?) {
        if(orphanage == null) return

        val orphanageAboutTextView = findViewById<TextView>(R.id.orphanage_description)
        orphanageAboutTextView.text = orphanage.about

        val orphanageVisitInstructionsTextView = findViewById<TextView>(R.id.visit_instrutions)
        orphanageVisitInstructionsTextView.text = orphanage.visit?.instructions

        val orphanageVisitTimeBoxTextView = findViewById<TextView>(R.id.visit_time_box)
        orphanageVisitTimeBoxTextView.text = orphanage.visit?.time

        val orphanageWeeekendsOn = findViewById<TextView>(R.id.visit_weekend_open_box)
        if (orphanage.weekendsOn == true) {
            orphanageWeeekendsOn.text = "Atendemos aos fins de semana"
            orphanageWeeekendsOn.background = ContextCompat.getDrawable(this, R.drawable.green_gradient)
            orphanageWeeekendsOn.setTextColor(Color.BLACK)
        } else {
            orphanageWeeekendsOn.text = "Não atendemos aos fins de semana"
            orphanageWeeekendsOn.background = ContextCompat.getDrawable(this, R.drawable.red_gradient)
            orphanageWeeekendsOn.setTextColor(Color.RED)
            orphanageWeeekendsOn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.info_red, 0, 0)
        }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun setIcon(menuItem: MenuItem?, isFavorited: Boolean?) {
        if (menuItem == null) return
        menuItem.icon =
            if (isFavorited == true) ContextCompat.getDrawable(this, R.drawable.favorite_filled_red)
            else ContextCompat.getDrawable(this, R.drawable.favorite_outline)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.orphanage_detail_menu, menu)
        val layotuButton = menu?.findItem(R.id.action_favorite)

        val userId = getUserId()
        val orphanageRef = FirebaseDatabase.getInstance().getReference("orphanages_users")
            .child(userId)

        orphanageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orphanageUser = snapshot.child(orphanageId!!).getValue(OrphanageUser::class.java)
                setIcon(layotuButton, orphanageUser?.isFavorited)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                val userId = getUserId()
                val orphanageRef = FirebaseDatabase.getInstance().getReference("orphanages_users")
                    .child(userId)

                orphanageRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {

                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val orphanageUser = snapshot.child(orphanageId!!).getValue(OrphanageUser::class.java)

                        if(orphanageUser == null) toggleFavoriteOrphanage(true)
                        else {
                            setIcon(item, !orphanageUser.isFavorited!!)
                            toggleFavoriteOrphanage(!orphanageUser.isFavorited!!)
                        }
                    }
                })

                return true
            }
            R.id.action_comments -> {
                val intent = Intent(baseContext, CommentActivity::class.java)
                intent.putExtra("orphanage_id", orphanageId)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleFavoriteOrphanage(isFavorited: Boolean?) {
        val userId = getUserId()
        val orphanageUser = OrphanageUser(orphanageId!!, userId, isFavorited)
        try {
            database.child("orphanages_users").child(userId).child(orphanageId!!).setValue(orphanageUser)
            val toastText = if(isFavorited == true) "Você favoritou o orfanato!!" else "Você desfavoritou o orfanato"
            Toast.makeText(baseContext, toastText, Toast.LENGTH_LONG).show()
        } catch (exception: FirebaseException) {
            Toast.makeText(baseContext, exception.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getUserId(): String {
        val user = auth.currentUser ?: return ""
        return user.uid
    }

    private fun sendMessage() {
        val buttonSendWhatsapp = findViewById<Button>(R.id.whatsapp_button)
        buttonSendWhatsapp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra(Intent.EXTRA_TEXT, "Teste")

            if(intent.resolveActivity(packageManager) == null) {
                Toast.makeText(this,
                    "Por favor instale o whatsapp antes.",
                    Toast.LENGTH_SHORT).show()

            } else {
                startActivity(intent)
            }
        }
    }
}