package com.example.happy

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.TextView
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
import com.google.firebase.database.*

class OrphanageDetailActivity: AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    var orphanageId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_orphanage_detail)

        orphanageId = intent.getStringExtra("orphanage_id")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        val zoomlevel = 15f
        Log.i("Orphanage id", orphanageId!!)
        val orphanageRef = FirebaseDatabase.getInstance().getReference("orphanages")
            .child(orphanageId ?: "")
        orphanageRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orphanage = snapshot.getValue(CreateOrphanageActivity.Orphanage::class.java)
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
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun populateData(orphanage: CreateOrphanageActivity.Orphanage?) {
        if(orphanage == null) return

        val orphanageNameTextView = findViewById<TextView>(R.id.orphanage_name)
        orphanageNameTextView.text = orphanage.name

        val orphanageAboutTextView = findViewById<TextView>(R.id.orphanage_description)
        orphanageAboutTextView.text = orphanage.about

        val orphanageVisitInstructionsTextView = findViewById<TextView>(R.id.visit_instrutions)
        orphanageVisitInstructionsTextView.text = orphanage.visit?.instructions
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}