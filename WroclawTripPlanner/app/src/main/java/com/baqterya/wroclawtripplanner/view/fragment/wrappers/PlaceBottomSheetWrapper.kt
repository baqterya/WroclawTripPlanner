package com.baqterya.wroclawtripplanner.view.fragment.wrappers

import android.view.View
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.PlaceViewPagerAdapter
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.Query

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
        viewPager2.apply {
            clipToPadding = false
            clipChildren = false
            offscreenPageLimit = 2
        }
        val offsetPx = 30
        viewPager2.setPadding(offsetPx, 0, offsetPx, 0)
        val marginPageTransformer = MarginPageTransformer(10)
        viewPager2.setPageTransformer(marginPageTransformer)
    }

}