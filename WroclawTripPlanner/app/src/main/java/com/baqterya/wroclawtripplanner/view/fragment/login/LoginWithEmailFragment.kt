package com.baqterya.wroclawtripplanner.view.fragment.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.baqterya.wroclawtripplanner.databinding.FragmentLoginWithEmailBinding
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * Fragment that allows the user to log in using their email.
 * It checks whether all field are filled and redirects the user to the main activity.
 */
class LoginWithEmailFragment : Fragment() {
    private var _binding: FragmentLoginWithEmailBinding? = null
        private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginWithEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogIn.setOnClickListener {
            if (TextUtils.isEmpty(binding.editTextEmailLogin.text.toString().trim { it <= ' ' })) {
                Toast.makeText(requireContext(), "Please enter a username.", Toast.LENGTH_SHORT)
                    .show()
                binding.editTextEmailLogin.requestFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextEmailLogin, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(
                    binding.editTextPasswordLogin.text.toString().trim { it <= ' ' })
            ) {
                Toast.makeText(requireContext(), "Please enter a password.", Toast.LENGTH_SHORT)
                    .show()
                binding.editTextPasswordLogin.requestFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextPasswordLogin, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            val email: String = binding.editTextEmailLogin.text.toString().trim { it <= ' ' }
            val password: String = binding.editTextPasswordLogin.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "You have logged in successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(activity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}