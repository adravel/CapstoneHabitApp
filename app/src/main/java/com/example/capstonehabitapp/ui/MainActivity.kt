package com.example.capstonehabitapp.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavGraph
import androidx.navigation.fragment.NavHostFragment
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var navGraph: NavGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check the user's role from shared preference
        val sharedPref = getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = if (sharedPref.contains(getString(R.string.role_pref_is_parent_key))) {
            sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)
        } else {
            null
        }

        // Configure navigation and set the start destination fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        when (isParent) {
            true -> setNewStartDestination(R.id.parentHomeFragment)
            false -> setNewStartDestination(R.id.childHomeFragment)
            null -> setNewStartDestination(R.id.roleSelectionFragment)
        }
        navController.graph = navGraph
    }

    // Public method for setting a new start destination fragment
    fun setNewStartDestination(@IdRes id: Int) {
        navGraph.setStartDestination(id)
    }
}