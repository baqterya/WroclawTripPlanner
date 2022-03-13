package com.baqterya.wroclawtripplanner.model

/**
 * Data class model of a Place object in the database
 *
 * @property placeFavUsersId: List of users that added the place to their favourites
 */
data class Place(
    var placeId: String? = null,
    var placeName: String? = null,
    var placeGeoHash: String? = null,
    var placeLatitude: Double? = null,
    var placeLongitude: Double? = null,
    var placeDescription: String? = null,
    var placeOwnerId: String? = null,
    var placeOwnerName: String? = null,
    var placeCategories: ArrayList<String> = arrayListOf(),
    var placeTagList: ArrayList<Tag> = arrayListOf(),
    var placeFavUsersId: ArrayList<String> = arrayListOf(),
    var placeLikes: Int = 0,
    var placeIsPrivate: Boolean = false,
)