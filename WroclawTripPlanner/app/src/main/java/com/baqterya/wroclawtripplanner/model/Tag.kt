package com.baqterya.wroclawtripplanner.model

/**
 * Data class of a tag that a user can designate for a Place.
 *
 * @property tagCounter: counts how many times a tag was added to a place
 * @property tagAddedByUserIds: list of IDs of users that added the same tag
 */
data class Tag(
    var tagName: String? = null,
    var tagCounter: Int = 0,
    var tagAddedByUserIds: ArrayList<String> = arrayListOf()
)
