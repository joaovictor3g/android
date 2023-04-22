package com.example.happy

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.firebase.database.*
import java.util.UUID

class CommentActivity: AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setTitle("Comentários")
        setContentView(R.layout.activity_comments)

        database = FirebaseDatabase.getInstance().reference

        val orphanageId = intent.getStringExtra("orphanage_id") ?: ""
        findViewById<Button>(R.id.add_new_comment_button)
            .setOnClickListener {
                createNewComment(orphanageId)
            }

        getCommentsByOrphanageId(orphanageId)
    }

    private fun createNewComment(orphanageId: String?) {
        if(orphanageId == null) return

        val commentEditText = findViewById<TextView>(R.id.new_comment_edit_text)
        val commentText = commentEditText.text.toString()

        val childId = UUID.randomUUID().toString()
        val orphangeComment = Comment(commentText, orphanageId ,childId)

        database.child("comments").child(childId).setValue(orphangeComment)
    }

    private fun getCommentsByOrphanageId(orphanageId: String?) {
        if(orphanageId == null) return

        val orphanageRef = database.child("comments")

        orphanageRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val commentLayout = findViewById<LinearLayout>(R.id.comments_linear_layout)
                commentLayout.removeAllViews()

                for (child in snapshot.children) {
                    val comment = child.getValue(Comment::class.java)
                    if(comment?.orphanageId == orphanageId)
                        updateUI(comment, commentLayout)
                }
            }
        })
    }

    private fun updateUI(comment: Comment?, layout: LinearLayout) {
        if(comment == null) return

        val textView = TextView(this)
        textView.text = comment.content
        textView.background = ContextCompat.getDrawable(this, R.drawable.blue_gradient)
        textView.setPadding(10.dpToPx(), 10.dpToPx(), 10.dpToPx(), 10.dpToPx())
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 0, 10.dpToPx())
        textView.layoutParams = layoutParams
        layout.addView(textView)
        val commentEditText = findViewById<TextView>(R.id.new_comment_edit_text)
        commentEditText.text = ""
    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

}