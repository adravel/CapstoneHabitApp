package com.example.capstonehabitapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.capstonehabitapp.databinding.FragmentRoleSelectionBinding

class RoleSelectionFragment: Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Hide action bar
        (activity as AppCompatActivity).supportActionBar?.hide()

        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.chooseParentRoleCard.setOnClickListener {
            it.findNavController().navigate(R.id.parentHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Show action bar again
        (activity as AppCompatActivity).supportActionBar?.show()

        _binding = null
    }
}