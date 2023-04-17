package com.example.happy

import java.util.*

class Orphanage {
    var id: String? = ""
    var name: String? = ""
    var about: String? = ""
    var whatsapp: String? = ""
    var visit: Visit? = Visit()
    var coords: Coords? = Coords()
    @field:JvmField
    var weekendsOn: Boolean? = false
    @field:JvmField
    var isFavorited: Boolean? = false
    var createdBy: User = User()

    constructor() {}

    constructor(
        id: String? = "",
        name: String,
        about: String,
        whatsapp: String,
        visit: Visit,
        coords: Coords,
        createdBy: User,
        weekendsOn: Boolean? = false,
        isFavorited: Boolean? = false,
    ) {
        if(id == null) this.id = UUID.randomUUID().toString()
        else this.id = id

        this.name = name
        this.about = about
        this.whatsapp = whatsapp
        this.visit = visit
        this.coords = coords
        this.createdBy = createdBy
        this.weekendsOn = weekendsOn
        this.isFavorited = isFavorited
    }

    data class Visit(val instructions: String? = "", val time: String? = "") {}

    data class Coords(val latitude: Double? = 0.0, val longitude: Double? = 0.0) {}
}