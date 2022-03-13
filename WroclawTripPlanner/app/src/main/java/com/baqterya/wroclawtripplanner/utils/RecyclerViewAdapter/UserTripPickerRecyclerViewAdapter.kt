package com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemTripPickerBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Trip
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

/**
 * Firestore Recycler View Adapter that lists all of user's Trips from the database.
 * It allows the user to add a place to one of their trips.
 */
class UserTripPickerRecyclerViewAdapter(
    options: FirestoreRecyclerOptions<Trip>,
    private val currentPlace: Place,
    private val dialog: MaterialDialog
) : FirestoreRecyclerAdapter<Trip, UserTripPickerRecyclerViewAdapter.TripPickerViewHolder>(options) {
    class TripPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemTripPickerBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripPickerViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_trip_picker,
            parent,
            false
        )
        return TripPickerViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: TripPickerViewHolder, position: Int, currentTrip: Trip) {
        val firestoreViewModel = FirestoreViewModel()
        holder.binding.textViewTripNamePicker.text = currentTrip.tripName
        holder.binding.imageButtonAddToTrip.setOnClickListener {
            firestoreViewModel.addPlaceToTrip(currentPlace, currentTrip)
            Toast.makeText(
                dialog.context,
                "Place added to ${currentTrip.tripName}",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }
        holder.binding.cardViewTripPicker.setOnClickListener {
            firestoreViewModel.addPlaceToTrip(currentPlace, currentTrip)
            Toast.makeText(
                dialog.context,
                "Place added to ${currentTrip.tripName}",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }
    }
}