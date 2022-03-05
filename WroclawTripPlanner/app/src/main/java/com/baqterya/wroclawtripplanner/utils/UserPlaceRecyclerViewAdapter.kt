package com.baqterya.wroclawtripplanner.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.iterator
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.RecyclerViewItemUserPlaceBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.baqterya.wroclawtripplanner.view.fragment.ListPlacesTripsFragmentDirections
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial

class UserPlaceRecyclerViewAdapter(options: FirestoreRecyclerOptions<Place>) : FirestoreRecyclerAdapter<Place, UserPlaceRecyclerViewAdapter.UserPlaceViewHolder>(options) {
    private val firestoreViewModel = FirestoreViewModel()

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
            showEditPlaceDialog(currentPlace, holder.itemView.context)
        }

        holder.binding.cardViewUserPlace.setOnClickListener {
            (holder.itemView.context as MainActivity).imageViewCenterPin.visibility = View.VISIBLE
            val latLng = currentPlace.placeLatitude.toString() + ',' + currentPlace.placeLongitude.toString()
            val action = ListPlacesTripsFragmentDirections.actionListPlacesTripsFragmentToMapFragment(latLng, currentPlace.placeId)
            holder.itemView.findNavController().navigate(action)
        }
    }

    private fun showEditPlaceDialog(currentPlace: Place, context: Context) {
        val dialog = MaterialDialog(context)
            .noAutoDismiss()
            .customView(R.layout.dialog_edit_place)

        val textViewEditPrompt = dialog.findViewById<TextView>(R.id.text_view_edit_place_prompt)
        val editTextPlaceName = dialog.findViewById<EditText>(R.id.edit_text_edit_place_name)
        val editTextPlaceDescription = dialog.findViewById<EditText>(R.id.edit_text_edit_place_description)
        val switchIsPlacePrivate = dialog.findViewById<SwitchMaterial>(R.id.switch_edit_place_is_private)
        val dialogChips = dialog.findViewById<ChipGroup>(R.id.chip_group_place_edit_category_picker)

        val ownerString = context.getString(R.string.edit_prompt)
        textViewEditPrompt.text = String.format(ownerString, currentPlace.placeName)

        editTextPlaceName.setText(currentPlace.placeName)
        editTextPlaceDescription.setText(currentPlace.placeDescription)

        switchIsPlacePrivate.isChecked = currentPlace.placeIsPrivate

        firestoreViewModel.editPlaceSetCategoryChips(currentPlace, dialogChips)

        for (dialogChip in dialogChips.iterator()) {
            dialog.findViewById<Chip>(dialogChip.id).setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    currentPlace.placeCategories.add(chip.text.toString())
                } else {
                    currentPlace.placeCategories.remove(chip.text.toString())
                }
            }
        }

        dialog.findViewById<Button>(R.id.button_edit_place).setOnClickListener {
            val placeName = editTextPlaceName.text.toString()
            val placeDescription = editTextPlaceDescription.text.toString()
            val placeIsPrivate = switchIsPlacePrivate.isChecked

            val isCategoryPicked = dialogChips.checkedChipIds.isNotEmpty()

            if (inputCheck(placeName) && inputCheck(placeDescription) && isCategoryPicked) {

                currentPlace.placeName = placeName
                currentPlace.placeDescription = placeDescription
                currentPlace.placeIsPrivate = placeIsPrivate
                firestoreViewModel.editPlace(currentPlace)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}
