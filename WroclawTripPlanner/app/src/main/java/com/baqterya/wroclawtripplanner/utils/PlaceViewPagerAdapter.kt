package com.baqterya.wroclawtripplanner.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ViewPagerItemPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.baqterya.wroclawtripplanner.model.Trip
import com.baqterya.wroclawtripplanner.view.fragment.wrappers.TagsBottomSheetWrapper
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PlaceViewPagerAdapter(private val places: List<Place>, private val activity: FragmentActivity) : RecyclerView.Adapter<PlaceViewPagerAdapter.PlaceViewPagerViewHolder>(){
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
        firestoreViewModel.updatePlaceLikesCounter(currentPlace, holder.binding.textViewLikeCounter)
        holder.binding.imageButtonAddPlaceToFav.setOnClickListener {
            updatePlaceIsFav(currentPlace, it as ImageButton, holder.binding.textViewLikeCounter)
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

        holder.binding.buttonAddPlaceToTrip.setOnClickListener {
            showAddPlaceToTripDialog(currentPlace, holder.itemView.context)
        }
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

    private fun updatePlaceIsFav(currentPlace: Place, imageButton: ImageButton, textView: TextView) {
        firestoreViewModel.updatePlaceIsFav(currentPlace, imageButton, textView)
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

    private fun showAddPlaceToTripDialog(currentPlace: Place, context: Context) {
        val dialog = MaterialDialog(context)
            .noAutoDismiss()
            .customView(R.layout.dialog_trip_action_picker)

        dialog.findViewById<Button>(R.id.button_add_to_new_trip).setOnClickListener {
            showAddNewTripDialog(currentPlace, context)
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.button_add_to_existing_trip).setOnClickListener {
            showTripListDialog(currentPlace, context)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddNewTripDialog(currentPlace: Place, context: Context) {
        val dialog = MaterialDialog(context)
            .noAutoDismiss()
            .customView(R.layout.dialog_add_new_trip)

        val newTrip = Trip()

        dialog.findViewById<Button>(R.id.button_add_trip).setOnClickListener {
            val tripName = dialog.findViewById<EditText>(R.id.edit_text_add_trip_name).text.toString()
            val tripDescription = dialog.findViewById<EditText>(R.id.edit_text_add_trip_description).text.toString()

            if (inputCheck(tripName) && inputCheck(tripDescription)) {

                newTrip.tripName = tripName
                newTrip.tripDescription = tripDescription

                firestoreViewModel.addTripToFirestore(currentPlace.placeId!!, newTrip)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showTripListDialog(currentPlace: Place, context: Context) {
        val dialog = MaterialDialog(context)
            .noAutoDismiss()
            .customView(R.layout.dialog_list_user_trips)

        val dialogRecyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view_choose_a_trip)

        val options = firestoreViewModel.getUserTripOptions(activity)
        if (options != null) {
            val adapter = UserTripPickerRecyclerViewAdapter(options, currentPlace, dialog)

            dialogRecyclerView.adapter = adapter
            dialogRecyclerView.layoutManager = WrapperLinearLayoutManager(activity)
        }
        dialog.findViewById<ImageButton>(R.id.image_button_close_trip_picker).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}