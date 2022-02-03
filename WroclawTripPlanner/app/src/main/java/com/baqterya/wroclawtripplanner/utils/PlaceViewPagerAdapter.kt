package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place

class PlaceViewPagerAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlaceViewPagerAdapter.PlaceViewPagerViewHolder>(){
    class PlaceViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item_place, parent, false)
        return PlaceViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewPagerViewHolder, position: Int) {
        val currentPlace = places[position]
        holder.binding.textViewPlaceName.text = currentPlace.placeName
        holder.binding.textViewPlaceOwner.text = "created by: ${currentPlace.placeOwnerName}"
        holder.binding.textViewPlaceDescription.text = currentPlace.placeDescription
    }

    override fun getItemCount(): Int {
        return places.size
    }
}