package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemUserPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UserPlaceRecyclerViewAdapter(options: FirestoreRecyclerOptions<Place>) : FirestoreRecyclerAdapter<Place, UserPlaceRecyclerViewAdapter.UserPlaceViewHolder>(options) {
    class UserPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RecyclerViewItemUserPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPlaceViewHolder {
        val objectView = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_view_item_user_place,
            parent,
            false
        )
        return UserPlaceViewHolder(objectView)
    }

    override fun onBindViewHolder(holder: UserPlaceViewHolder, position: Int, currentPlace: Place) {
        holder.binding.textViewPlaceNameUser.text = currentPlace.placeName
        val likeString = holder.itemView.context.getString(R.string.place_likes_prompt)
        holder.binding.textViewLikeCounterUser.text = String.format(likeString, currentPlace.placeLikes)

        holder.binding.imageButtonEditPlace.setOnClickListener {
            showEditPlaceDialog()
        }

        holder.binding.cardViewUserPlace.setOnClickListener {

        }
    }

    private fun showEditPlaceDialog() {

    }
}
