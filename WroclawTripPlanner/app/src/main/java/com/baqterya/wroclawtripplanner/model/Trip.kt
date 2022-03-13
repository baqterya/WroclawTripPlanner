package com.baqterya.wroclawtripplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class model of a Trip object in the database.
 * It is parcelable so it can be used as a fragment argument in the navigation component.
 *
 * @property tripFavUsersId: list of IDs of users that added the trip to their favourites
 */
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
