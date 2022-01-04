package com.baqterya.wroclawtripplanner.model

data class Place(
    var placeId: String? = null,
    var placeName: String? = null,
    var placeGeoHash: String? = null,
    var placeLatitude: Double? = null,
    var placeLongitude: Double? = null,
    var placeDescription: String? = null,
    var placeOwnerId: String? = null,
    var placeOwnerName: String? = null,
    var placeTagList: ArrayList<String> = arrayListOf(),
    var placeLikes: Int = 0,
    var placeIsPrivate: Boolean = false,
)