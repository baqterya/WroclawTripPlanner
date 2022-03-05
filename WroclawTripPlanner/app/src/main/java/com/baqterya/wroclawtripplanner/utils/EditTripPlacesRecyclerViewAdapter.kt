package com.baqterya.wroclawtripplanner.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemEditTripPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Trip
import com.baqterya.wroclawtripplanner.view.fragment.EditTripFragment
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class EditTripPlacesRecyclerViewAdapter(private val hostFragment: EditTripFragment, private val currentTrip: Trip, options: FirestoreRecyclerOptions<Place>) : FirestoreRecyclerAdapter<Place, EditTripPlacesRecyclerViewAdapter.EditTripPlaceViewHolder>(options) {
    private val firestoreViewModel = FirestoreViewModel()

    class EditTripPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemEditTripPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditTripPlaceViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_edit_trip_place,
            parent,
            false
        )
        return EditTripPlaceViewHolder(objectView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: EditTripPlaceViewHolder, position: Int, currentPlace: Place) {
        holder.binding.textViewPlaceNameTrip.text = currentPlace.placeName

        holder.binding.imageButtonRemovePlaceFromTrip.setOnClickListener {
            firestoreViewModel.removePlaceFromTrip(currentPlace, currentTrip)
            hostFragment.updateAdapter(currentPlace.placeId!!)
        }
    }
}