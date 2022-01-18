package com.baqterya.wroclawtripplanner.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: MaterialToolbar

    private val user = Firebase.auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.topToolbar
        setSupportActionBar(toolbar)

        if (user == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}