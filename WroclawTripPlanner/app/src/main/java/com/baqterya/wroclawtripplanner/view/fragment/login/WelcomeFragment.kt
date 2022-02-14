package com.baqterya.wroclawtripplanner.view.fragment.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentWelcomeBinding
import com.baqterya.wroclawtripplanner.model.User
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
        private val binding get() = _binding!!

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonEmailSignUp.setOnClickListener {
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToRegisterWithEmailFragment()
            findNavController().navigate(action)
        }

        binding.linearLayoutLoginPrompt.setOnClickListener {
            val action = WelcomeFragmentDirections.actionWelcomeFragmentToLoginWithEmailFragment()
            findNavController().navigate(action)
        }

        binding.buttonTwitterSignIn.setOnClickListener {
            val provider = OAuthProvider.newBuilder("twitter.com")

            val pendingTaskResult = auth.pendingAuthResult
            if (pendingTaskResult == null) {
                auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
                    .addOnSuccessListener { authResult ->
                        Log.d(TWITTER_TAG, "onViewCreated: finish the twitter sign in")
                        val newUser = User(
                            userId = authResult.user!!.uid,
                            userName = authResult.user!!.displayName,
                            userEmail = authResult.user!!.email
                        )

                        checkIfNameAvailable(newUser)
                    }
                    .addOnFailureListener { error ->
                        Log.e(TWITTER_TAG, "onViewCreated: error occurred: ", error)
                    }
            } else {
                pendingTaskResult.addOnSuccessListener { authResult ->
                    Log.d(TWITTER_TAG, "onViewCreated: finish the twitter sign in")
                    val newUser = User(
                        userId = authResult.user!!.uid,
                        userName = authResult.user!!.displayName,
                        userEmail = authResult.user!!.email
                    )

                    checkIfNameAvailable(newUser)
                }.addOnFailureListener { error ->
                    Log.e(TWITTER_TAG, "onViewCreated: error occurred: ", error)
                }
            }
        }
    }

    private fun checkIfNameAvailable(newUser: User) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                var isUsernameFree = true
                for (document in result) {
                    if ((document.data["userName"] as String).lowercase() == newUser.userName!!.lowercase())
                        isUsernameFree = false
                }

                if (isUsernameFree) {
                    proceedToMain(newUser)
                } else {
                    val dialog = MaterialDialog(requireContext())
                        .noAutoDismiss()
                        .customView(R.layout.dialog_twitter_username_taken)

                    dialog.findViewById<Button>(R.id.button_retry_username).setOnClickListener {
                        val newUsernameEditText = dialog.findViewById<EditText>(R.id.edit_text_retry_username)

                        if (TextUtils.isEmpty(newUsernameEditText.text.toString().trim { it <= ' ' })) {
                            Toast.makeText(requireContext(), "Please enter a new username.", Toast.LENGTH_SHORT).show()
                            newUsernameEditText.requestFocus()
                            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showSoftInput(newUsernameEditText, InputMethodManager.SHOW_IMPLICIT)
                            return@setOnClickListener
                        }
                        newUser.userName = newUsernameEditText.text.toString()
                        checkIfNameAvailable(newUser)
                    }
                    dialog.show()
                }
            }
    }


    private fun proceedToMain(newUser: User) {
        db.collection("users").document(newUser.userId!!).set(newUser)

        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    companion object {
        private const val TWITTER_TAG = "TWITTER_SIGN_IN_TAG"
    }
}