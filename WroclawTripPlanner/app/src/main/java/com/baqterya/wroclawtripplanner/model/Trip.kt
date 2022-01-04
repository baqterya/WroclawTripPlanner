package com.baqterya.wroclawtripplanner.model

data class Trip(
    var tripId: String? = null,
    var tripName: String? = null,
    var tripDescription: String? = null,
    var tripOwnerId: String? = null,
    var tripOwnerName: String? = null,
    var tripPlaceIdList: ArrayList<String> = arrayListOf(),
    var tripTagList: ArrayList<String> = arrayListOf(),
    var tripLikes: Int = 0,
    var tripIsPrivate: Boolean = false,
)
