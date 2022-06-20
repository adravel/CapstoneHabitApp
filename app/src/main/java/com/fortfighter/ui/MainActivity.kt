package com.fortfighter.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.fortfighter.R
import com.fortfighter.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Check whether the user is logged in or not
        val isLoggedIn = auth.currentUser != null

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