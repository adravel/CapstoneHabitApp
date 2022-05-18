package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentParentAccountVerificationBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.ParentAccountVerificationViewModel

class ParentAccountVerificationFragment: Fragment() {

    private var _binding: FragmentParentAccountVerificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParentAccountVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentParentAccountVerificationBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.parent_account_verification)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable register button by default
            gradeTaskButton.isEnabled = false

            // Set textChangedListener for the EditTexts
            passwordEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val passwordText = passwordEditText.text.toString().trim()

                    // Enable button if the EditText is not empty
                    gradeTaskButton.isEnabled = passwordText.isNotEmpty()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            // Set grade task button onCLickListener
            gradeTaskButton.setOnClickListener {
                val password = passwordEditText.text.toString()

                // Call the method to reauthenticate the user
                viewModel.reauthenticateUser(password)
            }
        }

        // Observe reauthenticationResponse LiveData in ViewModel
        // This value determines whether the reauthentication is successful or not
        viewModel.reauthenticationResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Build navigation options to pop this fragment before navigating
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.parentAccountVerificationFragment, true)
                        .build()

                    // Navigate to grading form page
                    val action = ParentAccountVerificationFragmentDirections
                        .actionParentAccountVerificationFragmentToGradingFormFragment()
                    findNavController().navigate(action, navOptions)
                }
                is Response.Failure -> {
                    Log.e("Reauthentication", response.message)
                    Toast.makeText(context, getString(R.string.reauthentication_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}