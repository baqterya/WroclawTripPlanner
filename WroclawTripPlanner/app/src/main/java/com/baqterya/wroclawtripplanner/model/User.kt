package com.baqterya.wroclawtripplanner.model

data class User(
    var userId: String? = null,
    var userName: String? = null,
    var userEmail: String? = null,
    var userFavTrips: ArrayList<String> = arrayListOf()
)