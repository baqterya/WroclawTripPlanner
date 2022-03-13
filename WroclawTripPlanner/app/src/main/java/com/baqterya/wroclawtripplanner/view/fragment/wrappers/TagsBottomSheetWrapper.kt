package com.baqterya.wroclawtripplanner.view.fragment.wrappers

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * A wrapper for a bottom sheet dialog that displays tags to search a place with or to add to a place.
 */
class TagsBottomSheetWrapper(private val view: View) {
    lateinit var fabTagsProceed: ExtendedFloatingActionButton
    lateinit var tagsBottomSheet: BottomSheetDialog
    private lateinit var chipGroups: ArrayList<ChipGroup>
    val selectedTags = arrayListOf<Tag>()
    val tagsToRemove = arrayListOf<Tag>()

    /**
     * Creates and applies setting to a tagsBottomSheet.
     *
     * @param addingTags: specifies whether a sheet is created to find a place by tags
     * or to add tags to a place.
     */
    fun createTagBottomSheet(addingTags: Boolean = false) {
        tagsBottomSheet = BottomSheetDialog(view.context)
        tagsBottomSheet.setContentView(R.layout.fragment_tags_bottom_sheet)

        tagsBottomSheet.behavior.peekHeight = view.rootView.height
        tagsBottomSheet.behavior.isDraggable = false

        fabTagsProceed = tagsBottomSheet.findViewById(R.id.fab_tags_proceed)!!

        if (addingTags) {
            val addIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_save)
            fabTagsProceed.icon = addIcon
            fabTagsProceed.scaleX = 1F
        }

        chipGroups = arrayListOf(
            tagsBottomSheet.findViewById(R.id.chip_group_tags_culture)!!,
            tagsBottomSheet.findViewById(R.id.chip_group_tags_food_and_drink)!!,
            tagsBottomSheet.findViewById(R.id.chip_group_tags_leisure)!!,
            tagsBottomSheet.findViewById(R.id.chip_group_tags_shopping)!!,
            tagsBottomSheet.findViewById(R.id.chip_group_tags_services)!!,
        )
        for (chipGroup in chipGroups.iterator()) {
            for (chip in chipGroup) {
                (chip as Chip).setOnClickListener {
                    val checked = chip.isChecked
                    val tag = Tag(chip.text.toString(), 1)
                    if (checked) {
                        selectedTags.add(tag)
                        tagsToRemove.remove(tag)
                    } else {
                        selectedTags.remove(tag)
                        tagsToRemove.add(tag)
                        if (selectedTags.size < 10) {
                            enableChips()
                        }
                    }
                    if (selectedTags.size >= 10) {
                        disableChips()
                    }
                }
            }
        }
        tagsBottomSheet.show()
    }

    /**
     * Checks for tags already added by the user and checks the tag chips accordingly.
     */
    fun checkUserTags(currentPlace: Place) {
        val placeViewModel = FirestoreViewModel()
        placeViewModel.checkUserTags(currentPlace, chipGroups)
    }

    /**
     * Disables all non-checked chips.
     */
    private fun disableChips() {
        for (chipGroup in chipGroups.iterator()) {
            for (chip in chipGroup) {
                if (!(chip as Chip).isChecked) {
                    chip.isEnabled = false
                }
            }
        }
    }

    /**
     * Enables all chips.
     */
    private fun enableChips() {
        for (chipGroup in chipGroups.iterator()) {
            for (chip in chipGroup) {
                chip.isEnabled = true
            }
        }
    }
}