package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemUserPlacesTripsBinding
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel

class UsersPlacesTripsViewPagerAdapter(private val activity: FragmentActivity) : RecyclerView.Adapter<UsersPlacesTripsViewPagerAdapter.UsersPlacesTripsViewPagerViewHolder>() {
    private val firestoreViewModel = FirestoreViewModel()
    private val options = firestoreViewModel.getUsersPlacesTripsRecyclerOptions(activity)

    class UsersPlacesTripsViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemUserPlacesTripsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersPlacesTripsViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item_user_places_trips, parent, false)
        return UsersPlacesTripsViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersPlacesTripsViewPagerViewHolder, position: Int) {
        var dataType = ""
        if (options != null)
            when (position) {
                0 -> {
                    val currentOptions = options.first
                    dataType = "PLACES"
                    val adapter = UserPlaceRecyclerViewAdapter(currentOptions)
                    holder.binding.recyclerViewUserPlacesTrips.adapter = adapter
                    holder.binding.recyclerViewUserPlacesTrips.layoutManager = WrapperLinearLayoutManager(activity)
                }
                1 -> {
                    val currentOptions = options.second
                    dataType = "TRIPS"
                    //                val adapter = UserTripsRecyclerViewAdapter(currentOptions)
                    //                holder.binding.recyclerViewFavourites.adapter = adapter
                    //                holder.binding.recyclerViewFavourites.layoutManager = WrapperLinearLayoutManager(activity)
                }
            }
        val titleString = holder.itemView.context.getString(R.string.users_list_prompt)
        holder.binding.textViewUserPlacesTripsList.text = String.format(titleString, dataType)
    }

    override fun getItemCount(): Int = 2
}