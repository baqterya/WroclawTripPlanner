package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.baqterya.wroclawtripplanner.databinding.FragmentListAllTripsBinding
import com.baqterya.wroclawtripplanner.utils.AllTripsListRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.utils.WrapperLinearLayoutManager
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel


class ListAllTripsFragment : Fragment() {
    private var _binding: FragmentListAllTripsBinding? = null
        private val binding get() = _binding!!

    val firestoreViewModel = FirestoreViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListAllTripsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val options = firestoreViewModel.getAllTripsOptions(requireActivity())
        val recyclerView = binding.recyclerViewFavourites
        if (options != null) {
            val adapter = AllTripsListRecyclerViewAdapter(options)
            recyclerView.adapter = adapter
        }
        recyclerView.layoutManager = WrapperLinearLayoutManager(requireContext())

        (requireActivity() as MainActivity).swapFabVisibility("disable")
    }

}