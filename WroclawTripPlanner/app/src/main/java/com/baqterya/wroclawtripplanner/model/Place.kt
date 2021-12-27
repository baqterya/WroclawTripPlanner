package com.baqterya.wroclawtripplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Place(
    var placeId: String? = null,
    var placeName: String? = null,
    var placeDescription: String? = null,
    var placeOwnerId: String? = null,
    var placeOwnerName: String? = null,
    var placeTagList: ArrayList<String> = arrayListOf(),
    var placeLikes: Int = 0,
    var placeIsPrivate: Boolean = false,
) : Parcelable