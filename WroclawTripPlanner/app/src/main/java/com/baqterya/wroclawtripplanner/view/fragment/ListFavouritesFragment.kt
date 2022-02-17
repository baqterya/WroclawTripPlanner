package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.baqterya.wroclawtripplanner.databinding.FragmentListFavouritesBinding
import com.baqterya.wroclawtripplanner.utils.FavouritesViewPagerAdapter

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
    }

    companion object {

    }
}