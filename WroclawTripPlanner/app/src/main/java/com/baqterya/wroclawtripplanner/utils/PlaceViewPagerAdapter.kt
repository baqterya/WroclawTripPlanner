package com.baqterya.wroclawtripplanner.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.baqterya.wroclawtripplanner.view.fragment.wrappers.TagsBottomSheetWrapper
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PlaceViewPagerAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlaceViewPagerAdapter.PlaceViewPagerViewHolder>(){
    private val firestoreViewModel = FirestoreViewModel()

    class PlaceViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_pager_item_place, parent, false)
        return PlaceViewPagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewPagerViewHolder, position: Int) {
        val currentPlace = places[position]
        holder.binding.textViewPlaceName.text = currentPlace.placeName
        val ownerString = holder.itemView.context.getString(R.string.created_by_placeholder)
        holder.binding.textViewPlaceOwner.text = String.format(ownerString, currentPlace.placeOwnerName)

        firestoreViewModel.isPlaceFav(currentPlace, holder.binding.imageButtonAddPlaceToFav)
        holder.binding.imageButtonAddPlaceToFav.setOnClickListener {
            updatePlaceIsFav(currentPlace, it as ImageButton)
        }

        val categoryString = StringBuilder()
        for (category in currentPlace.placeCategories) {
            categoryString.append(category.plus("   "))
        }
        holder.binding.textViewPlaceCategory.text = categoryString.toString()

        val placeTags = findMaxUsedTags(currentPlace.placeTagList)
        fillTagChips(placeTags, holder.binding.chipGroupPlaceTopTags)

        holder.binding.buttonAddTagsToPlace.setOnClickListener {
            openTagsSelectorSheet(holder.itemView, currentPlace, holder.binding.chipGroupPlaceTopTags)
        }

        holder.binding.textViewPlaceDescription.text = currentPlace.placeDescription
    }

    override fun getItemCount(): Int {
        return places.size
    }

    private fun findMaxUsedTags(tags: ArrayList<Tag>): ArrayList<Tag> {
        val topTenTags = arrayListOf<Tag>()

        while (topTenTags.size < 10) {
            if (tags.isEmpty()) return topTenTags

            val maxTag = tags.maxWithOrNull(Comparator.comparingInt { it.tagCounter })
            tags.remove(maxTag)
            if (maxTag != null) {
                topTenTags.add(maxTag)
            }
        }
        return topTenTags
    }

    private fun fillTagChips(topTenTags: ArrayList<Tag>, chipGroup: ChipGroup) {
        for (tag in topTenTags) {
            val idx = topTenTags.indexOf(tag)
            val tagString = tag.tagName.plus(" ").plus(tag.tagCounter)
            (chipGroup[idx] as Chip).text = tagString
            (chipGroup[idx] as Chip).isVisible = true
        }
    }

    private fun updatePlaceIsFav(currentPlace: Place, imageButton: ImageButton) {
        firestoreViewModel.updatePlaceIsFav(currentPlace, imageButton)
    }

    private fun openTagsSelectorSheet(view: View, currentPlace: Place, chipGroup: ChipGroup) {
        val tagsBottomSheetWrapper = TagsBottomSheetWrapper(view)
        tagsBottomSheetWrapper.createTagBottomSheet(addingTags = true)
        tagsBottomSheetWrapper.checkUserTags(currentPlace)
        tagsBottomSheetWrapper.fabTagsProceed.setOnClickListener {
            val tags = tagsBottomSheetWrapper.selectedTags
            val tagsToRemove= tagsBottomSheetWrapper.tagsToRemove
            tagsBottomSheetWrapper.tagsBottomSheet.dismiss()
            if (tags.isEmpty() && tagsToRemove.isEmpty()) return@setOnClickListener

            firestoreViewModel.updatePlaceTags(currentPlace, tags, tagsToRemove, chipGroup)
        }
    }
}