package com.example.happy

import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.util.UUID

class Comment {
    var id: String? = null
    var content: String = ""
    var createdAt: Date? = null
    var author: User = User()
    var orphanageId: String = ""
    private lateinit var auth: FirebaseAuth


    constructor(content: String, orphanageId: String, id: String? = "") {
        this.id = id ?: UUID.randomUUID().toString()
        this.content = content
        this.orphanageId = orphanageId
        this.createdAt = Date()

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null)
            this.author = User(user.email ?: "", user.displayName)
    }

    constructor() {}

    override fun toString(): String {
        return "Comment(id=$id, content='$content', createdAt=$createdAt, author=$author, orphanageId='$orphanageId')"
    }


}