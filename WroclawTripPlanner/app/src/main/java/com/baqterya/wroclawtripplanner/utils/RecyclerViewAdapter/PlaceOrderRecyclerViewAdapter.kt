package com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemTripReorderBinding
import com.baqterya.wroclawtripplanner.model.Place

class PlaceOrderRecyclerViewAdapter (private val places: ArrayList<Place>) : RecyclerView.Adapter<PlaceOrderRecyclerViewAdapter.PlaceOrderViewHolder>(){
    class PlaceOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemTripReorderBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceOrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item_trip_reorder, parent, false)
        return PlaceOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceOrderViewHolder, position: Int) {
        val currentPlace = places[position]
        holder.binding.textViewPlaceNameTrip.text = currentPlace.placeName
    }

    override fun getItemCount(): Int = places.size
}