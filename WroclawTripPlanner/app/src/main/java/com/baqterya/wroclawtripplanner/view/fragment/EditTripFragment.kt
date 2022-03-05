package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baqterya.wroclawtripplanner.databinding.FragmentEditTripBinding
import com.baqterya.wroclawtripplanner.utils.EditTripPlacesRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.utils.inputCheck
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel

class EditTripFragment : Fragment() {
    private var _binding: FragmentEditTripBinding? = null
        private val binding get() = _binding!!

    private val args by navArgs<EditTripFragmentArgs>()
    private val firestoreViewModel = FirestoreViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentTrip = args.currentTrip

        val editTextTripName = binding.editTextEditTripName
        val editTextTripDescription = binding.editTextEditTripDescription
        val switchIsPrivate = binding.switchIsTripPrivate
        val buttonSave = binding.fabEditTrip
        val recyclerView = binding.recyclerViewEditTripPlaces

        editTextTripName.setText(currentTrip.tripName)
        editTextTripDescription.setText(currentTrip.tripDescription)
        switchIsPrivate.isChecked = currentTrip.tripIsPrivate

        val options = firestoreViewModel.getTripPlacesOptions(requireActivity(), currentTrip)
        if (options != null) {
            val adapter = EditTripPlacesRecyclerViewAdapter(this, currentTrip, options)
            recyclerView.adapter = adapter
        }
        recyclerView.layoutManager = WrapperLinearLayoutManager(requireContext())

        buttonSave.setOnClickListener {
            val newName = editTextTripName.text.toString()
            val newDescription = editTextTripDescription.text.toString()
            if (inputCheck(newName) && inputCheck(newDescription)) {
                currentTrip.tripName = newName
                currentTrip.tripDescription = newDescription
                currentTrip.tripIsPrivate = switchIsPrivate.isChecked
            }
        }
    }

    fun updateAdapter(placeId: String) {
        val currentTrip = args.currentTrip
        currentTrip.tripPlaceIdList.remove(placeId)
        val recyclerView = binding.recyclerViewEditTripPlaces
        val options = firestoreViewModel.getTripPlacesOptions(requireActivity(), currentTrip)
        if (options != null) {
            val adapter = EditTripPlacesRecyclerViewAdapter(this, currentTrip, options)
            recyclerView.adapter = adapter
        }
        recyclerView.layoutManager = WrapperLinearLayoutManager(requireContext())
    }
}