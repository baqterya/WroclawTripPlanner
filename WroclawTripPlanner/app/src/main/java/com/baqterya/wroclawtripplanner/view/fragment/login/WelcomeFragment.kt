package com.baqterya.wroclawtripplanner.view.fragment.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.baqterya.wroclawtripplanner.databinding.FragmentWelcomeBinding
import com.baqterya.wroclawtripplanner.model.User
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.google.firebase.auth.FirebaseUser
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

        binding.buttonTwitterSignIn.setOnClickListener {
            val provider = OAuthProvider.newBuilder("twitter.com")

            val pendingTaskResult = auth.pendingAuthResult
            if (pendingTaskResult == null) {
                auth.startActivityForSignInWithProvider(requireActivity(), provider.build())
                    .addOnSuccessListener { authResult ->
                        Log.d(TWITTER_TAG, "onViewCreated: finish the twitter sign in")
                        proceedToMain(authResult.user!!)
                    }
                    .addOnFailureListener { error ->
                        Log.e(TWITTER_TAG, "onViewCreated: error occurred: ", error)
                    }
            } else {
                pendingTaskResult.addOnSuccessListener { authResult ->
                    Log.d(TWITTER_TAG, "onViewCreated: finish the twitter sign in")
                    proceedToMain(authResult.user!!)
                }.addOnFailureListener { error ->
                    Log.e(TWITTER_TAG, "onViewCreated: error occurred: ", error)
                }
            }
        }
    }

    private fun proceedToMain(resultUser: FirebaseUser) {
        val newUser = User(
            userId = resultUser.uid,
            userName = resultUser.displayName,
            userEmail = resultUser.email
        )
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