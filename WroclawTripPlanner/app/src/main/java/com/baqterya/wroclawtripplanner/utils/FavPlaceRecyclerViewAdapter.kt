package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.baqterya.wroclawtripplanner.view.fragment.ListFavouritesFragmentDirections
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class FavPlaceRecyclerViewAdapter(options: FirestoreRecyclerOptions<Place>) : FirestoreRecyclerAdapter<Place, FavPlaceRecyclerViewAdapter.FavPlaceViewHolder>(options) {
    private val firestoreViewModel = FirestoreViewModel()

    class FavPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavPlaceViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_place,
            parent,
            false
        )
        return FavPlaceViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: FavPlaceViewHolder, position: Int, currentPlace: Place) {
        holder.binding.textViewPlaceNameFav.text = currentPlace.placeName
        val ownerString = holder.itemView.context.getString(R.string.created_by_placeholder)
        holder.binding.textViewPlaceAuthorFav.text = String.format(ownerString, currentPlace.placeOwnerName)

        holder.binding.textViewLikeCounterFav.text = currentPlace.placeLikes.toString()

        val imageButtonFav = holder.binding.imageButtonAddPlaceToFavFav
        imageButtonFav.scaleX = 1.1F
        imageButtonFav.scaleY = 1.1F
        if (firestoreViewModel.isPlaceFav(currentPlace)) {
            imageButtonFav.setImageResource(R.drawable.ic_favourite)
        } else {
            imageButtonFav.setImageResource(R.drawable.ic_favourite_border)
        }

        imageButtonFav.setOnClickListener {
            firestoreViewModel.updatePlaceIsFav(currentPlace)
        }

        holder.binding.cardViewFavPlace.setOnClickListener {
            (holder.itemView.context as MainActivity).imageViewCenterPin.visibility = View.VISIBLE
            val latLng = currentPlace.placeLatitude.toString() + ',' + currentPlace.placeLongitude.toString()
            val action = ListFavouritesFragmentDirections.actionListFavouritesFragmentToMapFragment(latLng, currentPlace.placeId)
            holder.itemView.findNavController().navigate(action)
        }
    }

}