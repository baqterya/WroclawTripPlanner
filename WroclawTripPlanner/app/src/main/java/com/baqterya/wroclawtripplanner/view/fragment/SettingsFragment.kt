package com.baqterya.wroclawtripplanner.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.baqterya.wroclawtripplanner.databinding.FragmentSettingsBinding
import com.baqterya.wroclawtripplanner.utils.inputCheck
import com.baqterya.wroclawtripplanner.view.activity.LoginActivity
import com.baqterya.wroclawtripplanner.viewmodel.FirestoreViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
        private val binding get() = _binding!!

    private val firestoreViewModel = FirestoreViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestoreViewModel.fillUsername(binding.editTextEditUsername)

        binding.buttonChangeUsername.setOnClickListener {
            val newUserName = binding.editTextEditUsername.text.toString()
            if (inputCheck(newUserName)) {
                firestoreViewModel.changeUsername(newUserName)
                Toast.makeText(requireContext(), "Update successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonLogOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { dialog, _ ->
                    dialog.dismiss()
                    userLogout()
                }
                .show()
        }
    }

    private fun userLogout() {
        firestoreViewModel.userLogout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}