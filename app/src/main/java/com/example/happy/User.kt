package com.example.happy


class User {
    var email: String = ""
    var name: String? = ""
    var role: String? = ""

    constructor(email: String, name: String? = "", role: String? = "personal") {
        this.email = email
        this.name = name
        this.role = role
    }
    constructor() {}
}