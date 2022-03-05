package com.baqterya.wroclawtripplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Trip(
    var tripId: String? = null,
    var tripName: String? = null,
    var tripDescription: String? = null,
    var tripOwnerId: String? = null,
    var tripOwnerName: String? = null,
    var tripIsPrivate: Boolean = false,
    var tripPlaceIdList: ArrayList<String> = arrayListOf(),
    var tripFavUsersId: ArrayList<String> = arrayListOf(),
    var tripLikes: Int = 0,
) : Parcelable
