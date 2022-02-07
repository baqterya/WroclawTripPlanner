package com.baqterya.wroclawtripplanner.view.fragment.wrappers

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.iterator
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Tag
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class TagsBottomSheetWrapper(private val view: View) {
    private lateinit var fabTagsProceed: ExtendedFloatingActionButton
    private lateinit var chipGroups: ArrayList<ChipGroup>
    lateinit var tagsBottomSheet: BottomSheetDialog
    val selectedTags = arrayListOf<Tag>()

    fun createTagBottomSheet(addingTags: Boolean = false) {
        tagsBottomSheet = BottomSheetDialog(view.context)
        tagsBottomSheet.setContentView(R.layout.fragment_tags_bottom_sheet)
        tagsBottomSheet.behavior.peekHeight = view.height
        tagsBottomSheet.behavior.isDraggable = false

        fabTagsProceed = tagsBottomSheet.findViewById(R.id.fab_tags_proceed)!!
        fabTagsProceed.setOnClickListener {
            tagsBottomSheet.hide()
        }

        if (addingTags) {
            val addIcon = ContextCompat.getDrawable(view.context, R.drawable.ic_add)
            fabTagsProceed.icon = addIcon
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
                (chip as Chip).setOnCheckedChangeListener { _, checked ->
                    val tag = Tag(chip.text.toString(), 1)
                    if (checked) {
                        selectedTags.add(tag)
                    } else {
                        selectedTags.remove(tag)
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

    private fun disableChips() {
        for (chipGroup in chipGroups.iterator()) {
            for (chip in chipGroup) {
                if (!(chip as Chip).isChecked) {
                    chip.isEnabled = false
                }
            }
        }
    }

    private fun enableChips() {
        for (chipGroup in chipGroups.iterator()) {
            for (chip in chipGroup) {
                chip.isEnabled = true
            }
        }
    }
}