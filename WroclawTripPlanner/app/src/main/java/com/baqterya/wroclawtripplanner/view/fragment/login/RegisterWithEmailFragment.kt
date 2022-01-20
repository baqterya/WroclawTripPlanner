package com.baqterya.wroclawtripplanner.view.fragment.login

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.baqterya.wroclawtripplanner.databinding.FragmentRegisterWithEmailBinding
import com.baqterya.wroclawtripplanner.model.User
import com.baqterya.wroclawtripplanner.utils.PasswordStrength
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class RegisterWithEmailFragment : Fragment() {
    private var _binding: FragmentRegisterWithEmailBinding? = null
        private val binding get() = _binding!!

    private val db = Firebase.firestore

    private var safePassword: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterWithEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editTextPasswordRegister.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePasswordStrengthView(s.toString())
            }

        })

        binding.buttonRegister.setOnClickListener {
            if (TextUtils.isEmpty(binding.editTextEmailRegister.text.toString().trim { it <= ' ' })) {
                Toast.makeText(requireContext(), "Please enter an email.", Toast.LENGTH_SHORT).show()
                binding.editTextEmailRegister.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextEmailRegister, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(binding.editTextEmailRegister.text.toString()).matches()) {
                Toast.makeText(requireContext(), "Please enter a valid email.", Toast.LENGTH_SHORT).show()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextEmailRegister, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(binding.editTextUsernameRegister.text.toString().trim { it <= ' ' })) {
                Toast.makeText(requireContext(), "Please enter a username.", Toast.LENGTH_SHORT).show()
                binding.editTextUsernameRegister.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextUsernameRegister, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            if (!safePassword) {
                Toast.makeText(requireContext(), "Please create a safe password.", Toast.LENGTH_SHORT).show()
                binding.editTextPasswordRegister.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editTextPasswordRegister, InputMethodManager.SHOW_IMPLICIT)
                return@setOnClickListener
            }

            val email: String = binding.editTextEmailRegister.text.toString().trim {it <= ' '}
            val password: String = binding.editTextPasswordRegister.text.toString().trim {it <= ' '}
            val username: String = binding.editTextUsernameRegister.text.toString().trim {it <= ' '}

            db.collection("users").get()
                .addOnSuccessListener { result ->
                    var isUsernameFree = true
                    for (document in result) {
                        if (document.data["userName"] == username)
                            isUsernameFree = false
                    }

                    if (isUsernameFree) {
                        createUser(email, password, username)
                    } else {
                        Toast.makeText(requireContext(), "This username is taken", Toast.LENGTH_SHORT).show()
                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(binding.editTextUsernameRegister, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
        }
    }

    private fun updatePasswordStrengthView(password: String) {
        val progressBar = binding.progressBarPasswordStrength
        val strengthView = binding.textViewPasswordStrength
        if (TextUtils.isEmpty(password)) {
            strengthView.text = ""
            progressBar.progress = 0
            return
        }

        val strength = PasswordStrength.calculateStrength(password)
        strengthView.text = strength.getText(requireContext())
        strengthView.setTextColor(strength.color)

        progressBar.setIndicatorColor(strength.color)

        when (strength.getText(requireContext())) {
            "Weak" -> {
                progressBar.progress = 25
                safePassword = false
            }
            "Medium" -> {
                progressBar.progress = 50
                safePassword = false
            }
            "Strong" -> {
                progressBar.progress = 75
                safePassword = true
            }
            "Very Strong" -> {
                progressBar.progress = 100
                safePassword = true
            }
        }
    }

    private fun createUser(email: String, password: String, username: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result!!.user!!

                    Toast.makeText(requireContext(), "You have registered successfully", Toast.LENGTH_SHORT).show()
                    val newUser = User(firebaseUser.uid, username, email)
                    addUserToFirestore(newUser)

                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                } else  {
                    Log.e(TAG, "createUser: ", task.exception)
                }
            }
    }

    private fun addUserToFirestore(newUser: User) {
        db.collection("users").document(newUser.userId!!).set(newUser)
    }

    companion object {
        private const val TAG = "REGISTER_WITH_EMAIL"
    }
}