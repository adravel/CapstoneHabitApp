package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentLoginBinding

class LoginFragment: Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set login button onCLickListener
        binding.loginButton.setOnClickListener {
            // Navigate to role selection page
            findNavController().navigate(R.id.roleSelectionFragment)
        }

        // Set register option text onClickListener
        binding.registerInsteadText.setOnClickListener {
            // Build navigation options to pop this fragment before navigating
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build()

            // Navigate to register page
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}