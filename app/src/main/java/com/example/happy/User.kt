package com.example.happy


class User {
    var email: String = ""
    var name: String? = ""

    constructor(email: String, name: String? = "") {
        this.email = email
        this.name = name
    }

    constructor() {}
}