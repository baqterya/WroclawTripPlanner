package com.baqterya.wroclawtripplanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


fun inputCheck(name: String): Boolean {
    /**
     * Checks whether a string is not empty.
     */

    return !(TextUtils.isEmpty(name))
}

fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
    /**
     * Allows overriding map pin's icon with a drawable image.
     */

    return ContextCompat.getDrawable(context, vectorResId)?.run {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun createLocationRequest(): LocationRequest {
    /**
     * Reusable function for creating a location request.
     */

    val locationRequest = LocationRequest.create()
    locationRequest.interval = 10000
    locationRequest.fastestInterval = 5000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    return locationRequest
}
