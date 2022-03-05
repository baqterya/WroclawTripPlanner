package com.baqterya.wroclawtripplanner.utils

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemTripBinding
import com.baqterya.wroclawtripplanner.model.Trip
import com.baqterya.wroclawtripplanner.view.fragment.ListFavouritesFragmentDirections
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class FavTripsRecyclerViewAdapter(options: FirestoreRecyclerOptions<Trip>) : FirestoreRecyclerAdapter<Trip, FavTripsRecyclerViewAdapter.FavTripViewHolder>(options) {
    private val firestoreViewModel = FirestoreViewModel()

    class FavTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemTripBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavTripViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_trip,
            parent,
            false
        )
        return FavTripViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: FavTripViewHolder, position: Int, currentTrip: Trip) {
        holder.binding.textViewTripName.text = currentTrip.tripName
        holder.binding.textViewTripDescription.text = currentTrip.tripDescription
        holder.binding.textViewLikeCounterTrip.text = currentTrip.tripLikes.toString()
        val ownerString = holder.itemView.context.getString(R.string.created_by_placeholder)
        holder.binding.textViewTripAuthor.text = String.format(ownerString, currentTrip.tripOwnerName)

        holder.binding.imageButtonAddTripToFav.setOnClickListener {
            firestoreViewModel.updateTripIsFav(
                currentTrip,
                holder.binding.imageButtonAddTripToFav,
                holder.binding.textViewLikeCounterTrip
            )
        }

        val expandableView = holder.binding.expandableView
        val cardView = holder.binding.cardViewTrip
        val buttonExpand = holder.binding.imageButtonExpandDescriptionTrip

        holder.binding.imageButtonExpandDescriptionTrip.setOnClickListener {
            if (expandableView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                expandableView.visibility = View.VISIBLE
                buttonExpand.setImageResource(R.drawable.ic_expand_less)
            } else {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                expandableView.visibility = View.GONE
                buttonExpand.setImageResource(R.drawable.ic_expand_more)
            }
        }

        cardView.setOnClickListener {
            val action = ListFavouritesFragmentDirections
                .actionListFavouritesFragmentToTripDetailsFragment(currentTrip)
            holder.itemView.findNavController().navigate(action)
        }
    }

}
