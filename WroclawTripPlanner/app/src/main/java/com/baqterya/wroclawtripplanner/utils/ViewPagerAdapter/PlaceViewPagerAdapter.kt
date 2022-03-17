package com.baqterya.wroclawtripplanner.utils.ViewPagerAdapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
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
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.UserTripPickerRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.utils.inputCheck
import com.baqterya.wroclawtripplanner.view.fragment.MapFragment
import com.baqterya.wroclawtripplanner.view.fragment.wrappers.TagsBottomSheetWrapper
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * ViewPagerAdapter class for the view pager of places scanned by the user on the map.
 *
 * @property firestoreViewModel: Firestore View Model that communicates with the database
 */
class PlaceViewPagerAdapter(
    private val places: List<Place>,
    private val activity: FragmentActivity,
    private val mapFragment: MapFragment
) : RecyclerView.Adapter<PlaceViewPagerAdapter.PlaceViewPagerViewHolder>() {
    private val firestoreViewModel = FirestoreViewModel()

    class PlaceViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ViewPagerItemPlaceBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewPagerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_pager_item_place, parent, false)
        return PlaceViewPagerViewHolder(view)
    }

    /**
     * Fills the page with Place's data acquired from the database
     */
    override fun onBindViewHolder(holder: PlaceViewPagerViewHolder, position: Int) {

        val currentPlace = places[position]
        holder.binding.textViewPlaceName.text = currentPlace.placeName
        val ownerString = holder.itemView.context.getString(R.string.created_by_placeholder)
        holder.binding.textViewPlaceOwner.text =
            String.format(ownerString, currentPlace.placeOwnerName)

        val buttonFavPlace = holder.binding.imageButtonAddPlaceToFav
        updatePlaceFavUI(currentPlace, buttonFavPlace)
        val likeCounter = holder.binding.textViewLikeCounter
        likeCounter.text = currentPlace.placeLikes.toString()

        buttonFavPlace.setOnClickListener {
            firestoreViewModel.updatePlaceIsFav(currentPlace, likeCounter)
            updatePlaceFavUI(currentPlace, buttonFavPlace)
        }

        val categoryString = StringBuilder()
        for (category in currentPlace.placeCategories) {
            categoryString.append(category.plus("   "))
        }
        holder.binding.textViewPlaceCategory.text = categoryString.toString()

        val placeTags = findMaxUsedTags(currentPlace.placeTagList)
        fillTagChips(placeTags, holder.binding.chipGroupPlaceTopTags)

        holder.binding.buttonAddTagsToPlace.setOnClickListener {
            openTagsSelectorSheet(
                holder.itemView,
                currentPlace,
                holder.binding.chipGroupPlaceTopTags
            )
        }

        holder.binding.textViewPlaceDescription.text = currentPlace.placeDescription

        holder.binding.buttonAddPlaceToTrip.setOnClickListener {
            showAddPlaceToTripDialog(currentPlace, holder.itemView.context)
        }

        holder.binding.fabNavigateToPlace.setOnClickListener {
            mapFragment.drawPathToPlace(currentPlace.placeLatitude!!, currentPlace.placeLongitude!!, currentPlace.placeId!!)
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }

    /**
     * Updates the like button according to user's action
     */
    private fun updatePlaceFavUI(currentPlace: Place, buttonFavPlace: ImageButton) {
        if (firestoreViewModel.isPlaceFav(currentPlace)) {
            buttonFavPlace.setImageResource(R.drawable.ic_favourite)
        } else {
            buttonFavPlace.setImageResource(R.drawable.ic_favourite_border)
        }
    }

    /**
     * Finds ten most popular tags of the place.
     */
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

    /**
     * Displays top ten tags of the place on chips.
     */
    private fun fillTagChips(topTenTags: ArrayList<Tag>, chipGroup: ChipGroup) {
        for (tag in topTenTags) {
            val idx = topTenTags.indexOf(tag)
            val tagString = tag.tagName.plus(" ").plus(tag.tagCounter)
            (chipGroup[idx] as Chip).text = tagString
            (chipGroup[idx] as Chip).isVisible = true
        }
    }

    /**
     * Opens bottom sheet used for adding and removing tags from the place.
     */
    private fun openTagsSelectorSheet(view: View, currentPlace: Place, chipGroup: ChipGroup) {
        val tagsBottomSheetWrapper = TagsBottomSheetWrapper(view)
        tagsBottomSheetWrapper.createTagBottomSheet(addingTags = true)
        tagsBottomSheetWrapper.checkUserTags(currentPlace)
        tagsBottomSheetWrapper.fabTagsProceed.setOnClickListener {
            val tags = tagsBottomSheetWrapper.selectedTags
            val tagsToRemove = tagsBottomSheetWrapper.tagsToRemove
            tagsBottomSheetWrapper.tagsBottomSheet.dismiss()
            if (tags.isEmpty() && tagsToRemove.isEmpty()) return@setOnClickListener

            firestoreViewModel.updatePlaceTags(currentPlace, tags, tagsToRemove, chipGroup)
        }
    }

    /**
     * Shows the dialog that lets user pick whether they want to add the place
     * to an existing trip or create a new trip with the place already in it.
     */
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

    /**
     * Creates a dialog that lets the user create a new trip.
     */
    private fun showAddNewTripDialog(currentPlace: Place, context: Context) {
        val dialog = MaterialDialog(context)
            .noAutoDismiss()
            .customView(R.layout.dialog_add_new_trip)

        val newTrip = Trip()

        dialog.findViewById<Button>(R.id.button_add_trip).setOnClickListener {
            val tripName =
                dialog.findViewById<EditText>(R.id.edit_text_add_trip_name).text.toString()
            val tripDescription =
                dialog.findViewById<EditText>(R.id.edit_text_add_trip_description).text.toString()
            val tripIsPrivate = dialog.findViewById<SwitchMaterial>(R.id.switch_is_trip_private)

            if (inputCheck(tripName) && inputCheck(tripDescription)) {
                newTrip.tripName = tripName
                newTrip.tripDescription = tripDescription
                newTrip.tripIsPrivate = tripIsPrivate.isChecked

                firestoreViewModel.addTripToFirestore(currentPlace.placeId!!, newTrip)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    /**
     * Shows a dialog containing a list all of user's trips to add the place to.
     */
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

        dialog.show()
    }
}