package com.baqterya.wroclawtripplanner.utils.ViewPagerAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemListFavouritesBinding
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.FavPlaceRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.FavTripsRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel

/**
 * ViewPagerAdapter class for the view pager of user's favourite places and trips.
 * First page displays the places and second page displays the trips.
 *
 * @property firestoreViewModel: Firestore View Model that communicates with the database
 * @property options: Firestore Recycler View Options that gather the lists of favourites
 */
class FavouritesViewPagerAdapter(private val activity: FragmentActivity) : RecyclerView.Adapter<FavouritesViewPagerAdapter.FavouritesViewPagerViewHolder>(){
    private val firestoreViewModel = FirestoreViewModel()
    private val options = firestoreViewModel.getFavouritesRecyclerOptions(activity)

    class FavouritesViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemListFavouritesBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item_list_favourites, parent, false)
        return FavouritesViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavouritesViewPagerViewHolder, position: Int) {
        var favType = ""
        if (options != null)
            when (position) {
                0 -> {
                    /**
                     * create the recycler view of favourite places on the first page
                      */
                    val currentOptions = options.first
                    favType = "PLACES"
                    val adapter = FavPlaceRecyclerViewAdapter(currentOptions)
                    holder.binding.recyclerViewFavourites.adapter = adapter
                    holder.binding.recyclerViewFavourites.layoutManager = WrapperLinearLayoutManager(activity)
                }
                1 -> {
                    /**
                     * create the recycler view of favourite places on the second page
                     */
                    val currentOptions = options.second
                    favType = "TRIPS"
                    val adapter = FavTripsRecyclerViewAdapter(currentOptions)
                    holder.binding.recyclerViewFavourites.adapter = adapter
                    holder.binding.recyclerViewFavourites.layoutManager = WrapperLinearLayoutManager(activity)
                }
            }
        val titleString = holder.itemView.context.getString(R.string.fav_list_prompt)
        holder.binding.textViewFavList.text = String.format(titleString, favType)
    }

    override fun getItemCount(): Int = 2
}