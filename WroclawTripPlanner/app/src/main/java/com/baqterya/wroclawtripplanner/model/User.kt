package com.baqterya.wroclawtripplanner.model

/**
 * Data class model of a User object in the database
 *
 * @property userFavTrips: List of IDs of trips added by the user to their favourites
 */
data class User(
    var userId: String? = null,
    var userName: String? = null,
    var userEmail: String? = null,
    var userFavTrips: ArrayList<String> = arrayListOf()
)