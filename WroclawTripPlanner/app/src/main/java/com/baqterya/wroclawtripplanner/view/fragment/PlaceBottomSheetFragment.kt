package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baqterya.wroclawtripplanner.databinding.FragmentPlaceBottomSheetBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.PlaceViewPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class PlaceBottomSheetFragment(private val places: List<Place>) : BottomSheetDialogFragment() {
    private var _binding: FragmentPlaceBottomSheetBinding? = null
        private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlaceViewPagerAdapter(places)
        binding.viewPager2Places.adapter = adapter
    }

    companion object {
        const val TAG: String = "BOTTOM_SHEET"
    }
}