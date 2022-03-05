package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentTripDetailsBinding
import com.baqterya.wroclawtripplanner.utils.TripPlacesRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel

class TripDetailsFragment : Fragment() {
    private var _binding: FragmentTripDetailsBinding? = null
        private val binding get() = _binding!!

    private val firestoreViewModel = FirestoreViewModel()
    private val args by navArgs<TripDetailsFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentTrip = args.currentTrip
        binding.textViewTripNameDetails.text = currentTrip.tripName
        val ownerString = requireContext().getString(R.string.created_by_placeholder)
        binding.textViewTripAuthorDetails.text = String.format(ownerString, currentTrip.tripOwnerName)
        binding.textViewTripDescriptionDetails.text = currentTrip.tripDescription

        val options = firestoreViewModel.getTripPlacesOptions(requireActivity(), currentTrip)!!
        val adapter = TripPlacesRecyclerViewAdapter(options)
        binding.recyclerViewTripPlaces.adapter = adapter
        binding.recyclerViewTripPlaces.layoutManager = WrapperLinearLayoutManager(requireContext())

        binding.fabShowTheWay.setOnClickListener {

        }
    }

}