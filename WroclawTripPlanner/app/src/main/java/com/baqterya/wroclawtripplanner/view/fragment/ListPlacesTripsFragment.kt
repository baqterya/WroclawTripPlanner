package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.baqterya.wroclawtripplanner.databinding.FragmentListPlacesTripsBinding
import com.baqterya.wroclawtripplanner.utils.UsersPlacesTripsViewPagerAdapter
import com.baqterya.wroclawtripplanner.view.activity.MainActivity

class ListPlacesTripsFragment : Fragment() {
    private var _binding: FragmentListPlacesTripsBinding? = null
        private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListPlacesTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager2 = binding.viewPager2UserPlacesTrips
        val adapter = UsersPlacesTripsViewPagerAdapter(requireActivity())
        viewPager2.adapter = adapter

        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == 0) {
                    binding.fabToUserPlaces.visibility = View.INVISIBLE
                    binding.fabToUserTrips.visibility = View.VISIBLE
                } else {
                    binding.fabToUserPlaces.visibility = View.VISIBLE
                    binding.fabToUserTrips.visibility = View.INVISIBLE
                }
            }
        })

        binding.fabToUserTrips.setOnClickListener {
            viewPager2.currentItem = 1
        }

        binding.fabToUserPlaces.setOnClickListener {
            viewPager2.currentItem = 0
        }

        (requireActivity() as MainActivity).swapFabVisibility("disable")
    }

}