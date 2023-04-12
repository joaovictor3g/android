package com.example.happy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OrphanageDetailActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_orphanage_detail)
    }
}