package com.baqterya.wroclawtripplanner.model

data class Tag(
    var tagName: String? = null,
    var tagCounter: Int = 0,
    var tagAddedByUserIds: ArrayList<String> = arrayListOf()
)
