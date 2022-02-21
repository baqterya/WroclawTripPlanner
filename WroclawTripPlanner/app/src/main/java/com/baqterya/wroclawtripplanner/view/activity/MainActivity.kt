package com.baqterya.wroclawtripplanner.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val user = Firebase.auth.currentUser

    lateinit var imageViewCenterPin: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageViewCenterPin = binding.imageViewCenterPin

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val navController = findNavController(R.id.main_fragment_container)
        binding.textViewSettings.setOnClickListener {
            navController.navigate(R.id.settingsFragment)
        }
        binding.textViewFavourites.setOnClickListener {
            navController.navigate(R.id.listFavouritesFragment)
        }
        binding.fabShowMap.setOnClickListener {
            navController.navigate(R.id.mapFragment)
        }
        binding.textViewUserPlacesAndTrips.setOnClickListener {
            navController.navigate(R.id.listPlacesTripsFragment)
        }
    }

    fun swapFabVisibility(mode: String) {
        when (mode) {
            "disable" -> {
                imageViewCenterPin.visibility = View.INVISIBLE
                binding.fabAddPin.visibility = View.INVISIBLE
                binding.fabShowMap.visibility = View.VISIBLE
            }
            "enable" -> {
                imageViewCenterPin.visibility = View.VISIBLE
                binding.fabAddPin.visibility = View.VISIBLE
                binding.fabShowMap.visibility = View.INVISIBLE
            }
        }
    }
}