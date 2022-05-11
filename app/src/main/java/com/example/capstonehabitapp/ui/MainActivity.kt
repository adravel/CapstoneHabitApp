package com.example.capstonehabitapp.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Do actual auth check
        // Check whether the user is logged in or not from shared preference
        val authPref = getSharedPreferences("authPref", Context.MODE_PRIVATE)
        val isLoggedIn = authPref.getBoolean("isLoggedIn", false)

        // Check the user's role from shared preference
        val rolePref = getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = if (rolePref.contains(getString(R.string.role_pref_is_parent_key))) {
            rolePref.getBoolean(getString(R.string.role_pref_is_parent_key), true)
        } else {
            null
        }

        // Configure navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        // Set the start destination fragment
        val newStartDestination = if (isLoggedIn) {
            when (isParent) {
                true -> R.id.parentHomeFragment
                false -> R.id.childHomeFragment
                null -> R.id.roleSelectionFragment
            }
        } else {
            R.id.welcomeFragment
        }
        navGraph.setStartDestination(newStartDestination)
        navController.graph = navGraph
    }
}