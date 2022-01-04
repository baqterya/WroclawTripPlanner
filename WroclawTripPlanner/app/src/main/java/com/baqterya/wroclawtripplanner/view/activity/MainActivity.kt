package com.baqterya.wroclawtripplanner.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.topToolbar
        setSupportActionBar(toolbar)
    }
}