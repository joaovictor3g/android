package com.example.happy

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class CommentActivity: AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setTitle("Comentários")
        setContentView(R.layout.activity_comments)

        database = FirebaseDatabase.getInstance().reference

        findViewById<Button>(R.id.add_new_comment_button)
            .setOnClickListener {
                createNewComment()
            }
    }

    private fun createNewComment() {
        val commentEditText = findViewById<TextView>(R.id.new_comment_edit_text)
        val commentText = commentEditText.text.toString()

        val childId = UUID.randomUUID().toString()
        val orphangeComment = Comment(commentText, "13bf030d-472b-489e-89c4-a364773538aa" ,childId)

        database.child("comments").child(childId).setValue(orphangeComment)
    }
}