package com.example.happy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class FirstTutorial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_tutorial)
        supportActionBar?.hide()

        val buttonNextTutorial = findViewById<Button>(R.id.first_tutorial_next_btn)
        buttonNextTutorial.setOnClickListener {
            val intent = Intent(this, SecondTutorial::class.java)
            startActivity(intent)
        }
    }
}