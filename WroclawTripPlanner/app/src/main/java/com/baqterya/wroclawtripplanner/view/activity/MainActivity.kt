package com.baqterya.wroclawtripplanner.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ActivityMainBinding
import com.baqterya.wroclawtripplanner.view.fragment.MapFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val navController = findNavController(R.id.main_fragment_container)
        binding.textViewSettings.setOnClickListener {
            navController.navigate(R.id.settingsFragment)
            binding.fabAddPin.visibility = View.INVISIBLE
            binding.fabShowMap.visibility = View.VISIBLE
        }
        binding.textViewFavourites.setOnClickListener {
        }
        binding.fabShowMap.setOnClickListener {
            navController.navigate(R.id.mapFragment)
            it.visibility = View.INVISIBLE
            binding.fabAddPin.visibility = View.VISIBLE
        }
    }

}