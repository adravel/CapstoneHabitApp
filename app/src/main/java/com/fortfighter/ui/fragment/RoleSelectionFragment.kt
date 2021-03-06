package com.fortfighter.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.FragmentRoleSelectionBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Display role cards icon images
        Glide.with(this)
            .load(R.drawable.img_general_male)
            .into(binding.parentRoleCardIcon)
        Glide.with(this)
            .load(R.drawable.img_soldier_male)
            .into(binding.childRoleCardIcon)

        // Set Parent card onClickListener
        binding.parentRoleCard.setOnClickListener {
            // Save Parent as role preference
            editor.putBoolean(getString(R.string.role_pref_is_parent_key), true).apply()

            // Remove Child data preference if they exist
            val childIdKey = getString(R.string.role_pref_child_id_key)
            val childNameKey = getString(R.string.role_pref_child_name_key)
            if (sharedPref.contains(childIdKey) && sharedPref.contains(childNameKey)) {
                editor
                    .remove(childIdKey)
                    .remove(childNameKey)
                    .apply()
            }

            // Navigate to parent home page
            findNavController().navigate(R.id.parentHomeFragment)
        }

        // Set Child card onClickListener
        binding.childRoleCard.setOnClickListener {
            // Navigate to child account selection page
            // and show add child button in that page
            val action = RoleSelectionFragmentDirections
                .actionRoleSelectionFragmentToChildAccountSelectionFragment(false)
             findNavController().navigate(action)
        }

        // Set logout button onClickListener
        binding.logoutButton.setOnClickListener {
            // Logout from Firebase Auth
            Firebase.auth.signOut()

            // Remove all role preference values
            editor.clear().apply()

            // Navigate to welcome page
            findNavController().navigate(R.id.welcomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}