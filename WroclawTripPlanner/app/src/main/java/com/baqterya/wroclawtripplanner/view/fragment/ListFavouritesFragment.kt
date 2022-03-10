package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.baqterya.wroclawtripplanner.databinding.FragmentListFavouritesBinding
import com.baqterya.wroclawtripplanner.utils.ViewPagerAdapter.FavouritesViewPagerAdapter
import com.baqterya.wroclawtripplanner.view.activity.MainActivity

class ListFavouritesFragment : Fragment() {
    private var _binding: FragmentListFavouritesBinding? = null
        private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager2 = binding.viewPager2Favourites
        val adapter = FavouritesViewPagerAdapter(requireActivity())
        viewPager2.adapter = adapter

        viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == 0) {
                    binding.fabToFavPlaces.visibility = View.INVISIBLE
                    binding.fabToFavTrips.visibility = View.VISIBLE
                } else {
                    binding.fabToFavPlaces.visibility = View.VISIBLE
                    binding.fabToFavTrips.visibility = View.INVISIBLE
                }
            }
        })

        binding.fabToFavTrips.setOnClickListener {
            viewPager2.currentItem = 1
        }

        binding.fabToFavPlaces.setOnClickListener {
            viewPager2.currentItem = 0
        }

        (requireActivity() as MainActivity).swapFabVisibility("disable")
    }

}