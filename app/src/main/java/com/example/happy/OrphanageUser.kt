package com.example.happy

class OrphanageUser {
    var orphanageId: String = ""
    var userId: String = ""
    @field:JvmField
    var isFavorited: Boolean? = false

    constructor(orphanageId: String, userId: String, isFavorited: Boolean? = false) {
        this.orphanageId = orphanageId
        this.userId = userId
        this.isFavorited = isFavorited
    }

    constructor() {}
}