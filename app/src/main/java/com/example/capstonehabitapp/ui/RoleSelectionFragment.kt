package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentRoleSelectionBinding

class RoleSelectionFragment: Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Define a shared preference for storing role selection
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()

        // Set Parent card onClickListener
        binding.parentRoleCard.setOnClickListener {
            // Save Parent as role preference
            editor?.putBoolean(getString(R.string.role_pref_is_parent_key), true)?.apply()

            // Navigate to parent home page
            findNavController().navigate(R.id.parentHomeFragment)
        }

        // Set Child card onClickListener
        binding.childRoleCard.setOnClickListener {
            // Save Child as role preference
            editor?.putBoolean(getString(R.string.role_pref_is_parent_key), false)?.apply()

            // Navigate to child home page
             findNavController().navigate(R.id.childAccountSelectionFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}