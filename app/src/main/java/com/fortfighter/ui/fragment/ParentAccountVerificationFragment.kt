package com.fortfighter.ui.fragment

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
import androidx.navigation.fragment.navArgs
import com.fortfighter.R
import com.fortfighter.databinding.FragmentParentAccountVerificationBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.ParentAccountVerificationViewModel

class ParentAccountVerificationFragment: Fragment() {

    private var _binding: FragmentParentAccountVerificationBinding? = null
    private val binding get() = _binding!!

    private var isForGrading: Boolean? = null

    private val viewModel: ParentAccountVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentParentAccountVerificationBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.parent_account_verification)

        // Initialize isForGrading variable using Safe Args provided by navigation component
        val args: ParentAccountVerificationFragmentArgs by navArgs()
        isForGrading = args.isForGrading

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable register button by default
            button.isEnabled = false

            // Set textChangedListener for the EditTexts
            passwordEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val passwordText = passwordEditText.text.toString().trim()

                    // Enable button if the EditText is not empty
                    button.isEnabled = passwordText.isNotEmpty()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            // Set button onCLickListener
            button.setOnClickListener {
                val password = passwordEditText.text.toString()

                // Call the method to reauthenticate the user
                viewModel.reauthenticateUser(password)
            }

            // Set button text
            button.text = if (isForGrading!!) {
                getString(R.string.button_label_grade_task)
            } else {
                getString(R.string.button_label_change_role)
            }
        }

        // Observe reauthenticationResponse LiveData in ViewModel
        // to determine whether the reauthentication is successful or not
        viewModel.reauthenticationResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Choose navigation direction depending on isForGrading value
                    if (isForGrading!!) {
                        // Build navigation options to pop this fragment before navigating
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.parentAccountVerificationFragment, true)
                            .build()

                        // Navigate to grading form page
                        val action = ParentAccountVerificationFragmentDirections
                            .actionParentAccountVerificationFragmentToGradingFormFragment()
                        findNavController().navigate(action, navOptions)
                    } else {
                        // Navigate to role selection page
                        findNavController().navigate(R.id.roleSelectionFragment)
                    }

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