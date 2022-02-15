package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
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
        holder.binding.imageButtonAddPlaceToFavFav.setOnClickListener {
            firestoreViewModel.updatePlaceIsFav(
                currentPlace,
                holder.binding.imageButtonAddPlaceToFavFav,
                holder.binding.textViewLikeCounterFav
            )
        }

        holder.binding.cardViewFavPlace.setOnClickListener {
//            val action =
//            holder.itemView.findNavController().navigate(action)
        }
    }

}