package com.baqterya.wroclawtripplanner.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baqterya.wroclawtripplanner.databinding.FragmentSettingsBinding
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
        private val binding get() = _binding!!

    val firestoreViewModel = FirestoreViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {

    }
}