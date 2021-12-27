package com.baqterya.wroclawtripplanner.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceTag(
    var placeTagId: String? = null,
    var placeTagName: String? = null,
    var placeTagList: ArrayList<String> = arrayListOf(),
    var placeTagPublicUses: Int = 0,
) : Parcelable
