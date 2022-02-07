package com.baqterya.wroclawtripplanner.view.fragment.wrappers

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.PlaceViewPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlaceBottomSheetWrapper(private val view: View, private val places: ArrayList<Place>, private val clickedItemIndex: Int) {
    lateinit var viewPager2: ViewPager2
    lateinit var placeBottomSheet: BottomSheetDialog

    fun createPlaceBottomSheet() {
        placeBottomSheet = BottomSheetDialog(view.context)
        placeBottomSheet.setContentView(R.layout.fragment_place_bottom_sheet)
        placeBottomSheet.behavior.peekHeight = view.height

        val adapter = PlaceViewPagerAdapter(places)
        viewPager2 = placeBottomSheet.findViewById(R.id.view_pager_2_places)!!
        viewPager2.adapter = adapter
        viewPager2.currentItem = clickedItemIndex
    }

}