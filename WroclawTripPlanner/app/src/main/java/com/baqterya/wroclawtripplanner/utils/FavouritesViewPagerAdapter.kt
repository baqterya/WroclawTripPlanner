package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemListFavouritesBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions

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
        val currentOptions = options[position]
        var favType = ""

        when (position) {
            0 -> {
                favType = "PLACES"
                val adapter = FavPlaceRecyclerViewAdapter(currentOptions as FirestoreRecyclerOptions<Place>)
                holder.binding.recyclerViewFavourites.adapter = adapter
                holder.binding.recyclerViewFavourites.layoutManager = WrapperLinearLayoutManager(activity)
            }
            1 -> {
                favType = "TRIPS"
                val adapter = null
                holder.binding.recyclerViewFavourites.adapter = adapter
                holder.binding.recyclerViewFavourites.layoutManager = WrapperLinearLayoutManager(activity)
            }
        }
        val titleString = holder.itemView.context.getString(R.string.fav_list_prompt)
        holder.binding.textViewFavList.text = String.format(titleString, favType)


    }

    override fun getItemCount(): Int = 2
}