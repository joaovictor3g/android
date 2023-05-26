package com.example.happy

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Display
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var locationManager : LocationManager? = null
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        supportActionBar?.setTitle("Orfanatos")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("82696240037-tk0v9rhjbjpvggbjuvm94geb9s67ov1v.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        handleGoToAddNewOrphanage()
        toggleShowButtonByRole()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        val quixada = LatLng(-4.9783253, -39.0256111)

        val zoomlevel = 15f

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quixada, zoomlevel))

        database.child("orphanages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orphanagesList = mutableListOf<Orphanage>()
                for (child in snapshot.children) {
                    val orphanage = child.getValue(Orphanage::class.java)!!

                    mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    orphanage.coords?.latitude ?: 0.0 ,
                                    orphanage.coords?.longitude ?: 0.0
                                )
                            )
                            .title(orphanage.id)
                            .icon(bitmapDescriptorFromVector(baseContext, R.drawable.happy_marker))

                    )
                    orphanagesList.add(orphanage)
                }
                setAmountFoundOrphanages(orphanagesList.size)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        mMap.setOnMarkerClickListener {marker ->
            val orphanageId = marker.title.toString()
            val intent = Intent(baseContext, OrphanageDetailActivity::class.java)
            intent.putExtra("orphanage_id", orphanageId)
            startActivity(intent)
            true
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

    private fun handleGoToAddNewOrphanage() {
        val button: Button = findViewById(R.id.add_new_orphanage_button)
        button.setOnClickListener {
            mMap?.let {
                val latLng = it.cameraPosition.target
                val latitude = latLng.latitude
                val longitude = latLng.longitude

                val intent = Intent(this, CreateOrphanageActivity::class.java)
                intent.putExtra("latitude", latitude)
                intent.putExtra("longitude", longitude)
                startActivity(intent)
            }

        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut()
    }

    private fun setAmountFoundOrphanages(amount: Int) {
        val amountOrphanages = findViewById<TextView>(R.id.amount_orphanages_found)
        amountOrphanages.text = "${amount} orfanatos encontrados"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                goToLogin()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleShowButtonByRole() {
        val currentUser = auth.currentUser ?: return
        val button = findViewById<Button>(R.id.add_new_orphanage_button)
        database
            .child("users")
            .child(currentUser.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        val role = snapshot.child("role").getValue(String::class.java)
                        if (role == "admin") button.visibility = View.VISIBLE
                        else {
                            val orphanageAmountTextView = findViewById<TextView>(R.id.amount_orphanages_found)
                            orphanageAmountTextView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                            button.visibility = View.GONE
                        }
                    }
                }
            }

    }


    private fun goToLogin() {
        val intent = Intent(baseContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}

