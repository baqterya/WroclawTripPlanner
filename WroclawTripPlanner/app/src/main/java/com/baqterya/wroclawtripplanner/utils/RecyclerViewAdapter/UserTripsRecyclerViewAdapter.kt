package com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemUserTripBinding
import com.baqterya.wroclawtripplanner.model.Trip
import com.baqterya.wroclawtripplanner.view.fragment.ListPlacesTripsFragmentDirections
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

/**
 * Firestore Recycler View Adapter that lists all of user's own Trips from the database.
 */
class UserTripsRecyclerViewAdapter(options: FirestoreRecyclerOptions<Trip>) : FirestoreRecyclerAdapter<Trip, UserTripsRecyclerViewAdapter.UserTripViewHolder>(options) {
    class UserTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemUserTripBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTripViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_user_trip,
            parent,
            false
        )
        return UserTripViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: UserTripViewHolder, position: Int, currentTrip: Trip) {
        holder.binding.textViewUserTripName.text = currentTrip.tripName
        val likeString = holder.itemView.context.getString(R.string.place_likes_prompt)
        holder.binding.textViewLikeCounterUserTrip.text = String.format(likeString, currentTrip.tripLikes)
        holder.binding.cardViewUserTrip.setOnClickListener {
            val action = ListPlacesTripsFragmentDirections
                .actionListPlacesTripsFragmentToEditTripFragment(currentTrip)
            holder.itemView.findNavController().navigate(action)
        }
        holder.binding.imageButtonEditTrip.setOnClickListener {
            val action = ListPlacesTripsFragmentDirections
                .actionListPlacesTripsFragmentToEditTripFragment(currentTrip)
            holder.itemView.findNavController().navigate(action)
        }
    }

}
