package com.example.capstonehabitapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstonehabitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set Toolbar as the app bar
        setSupportActionBar(binding.mainActivityToolbar)
    }
}