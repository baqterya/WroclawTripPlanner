package com.baqterya.wroclawtripplanner.utils.ViewPagerAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemUserPlacesTripsBinding
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.UserPlaceRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.UserTripsRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel

/**
 * ViewPagerAdapter class for the view pager of user's own places and trips.
 * First page displays the places and second page displays the trips.
 *
 * @property firestoreViewModel: Firestore View Model that communicates with the database
 * @property options: Firestore Recycler View Options that gather the lists of user's trips and places
 */
class UsersPlacesTripsViewPagerAdapter(private val activity: FragmentActivity) :
    RecyclerView.Adapter<UsersPlacesTripsViewPagerAdapter.UsersPlacesTripsViewPagerViewHolder>() {
    private val firestoreViewModel = FirestoreViewModel()
    private val options = firestoreViewModel.getUsersPlacesTripsRecyclerOptions(activity)

    class UsersPlacesTripsViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemUserPlacesTripsBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersPlacesTripsViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_pager_item_user_places_trips, parent, false)
        return UsersPlacesTripsViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersPlacesTripsViewPagerViewHolder, position: Int) {
        var dataType = ""
        if (options != null)
            when (position) {
                0 -> {
                    /**
                     * create the recycler view of user's places on the first page
                     */
                    val currentOptions = options.first
                    dataType = "PLACES"
                    val adapter = UserPlaceRecyclerViewAdapter(currentOptions)
                    holder.binding.recyclerViewUserPlacesTrips.adapter = adapter
                    holder.binding.recyclerViewUserPlacesTrips.layoutManager =
                        WrapperLinearLayoutManager(activity)
                }
                1 -> {
                    /**
                     * create the recycler view of user's trips on the second page
                     */
                    val currentOptions = options.second
                    dataType = "TRIPS"
                    val adapter = UserTripsRecyclerViewAdapter(currentOptions)
                    holder.binding.recyclerViewUserPlacesTrips.adapter = adapter
                    holder.binding.recyclerViewUserPlacesTrips.layoutManager =
                        WrapperLinearLayoutManager(activity)
                }
            }
        val titleString = holder.itemView.context.getString(R.string.users_list_prompt)
        holder.binding.textViewUserPlacesTripsList.text = String.format(titleString, dataType)
    }

    override fun getItemCount(): Int = 2
}