package com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemTripPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.baqterya.wroclawtripplanner.view.fragment.TripDetailsFragmentDirections
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

/**
 * Firestore Recycler View Adapter that lists all of trip's places.
 */
class TripPlacesRecyclerViewAdapter(options: FirestoreRecyclerOptions<Place>) : FirestoreRecyclerAdapter<Place, TripPlacesRecyclerViewAdapter.TripPlaceViewHolder>(options)  {
    class TripPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemTripPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripPlaceViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_trip_place,
            parent,
            false
        )
        return TripPlaceViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: TripPlaceViewHolder, position: Int, currentPlace: Place) {
        holder.binding.textViewPlaceNameTrip.text = currentPlace.placeName
        holder.binding.cardViewTripPlace.setOnClickListener {
            (holder.itemView.context as MainActivity).imageViewCenterPin.visibility = View.VISIBLE
            val latLng = currentPlace.placeLatitude.toString() + ',' + currentPlace.placeLongitude.toString()
            val action = TripDetailsFragmentDirections.actionTripDetailsFragmentToMapFragment(latLng, currentPlace.placeId)
            holder.itemView.findNavController().navigate(action)
        }
    }

}