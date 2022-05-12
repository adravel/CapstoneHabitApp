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
import com.example.capstonehabitapp.databinding.FragmentRegisterBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.RegisterViewModel

class RegisterFragment: Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable register button by default
            registerButton.isEnabled = false

            // Set textChangedListener for the EditTexts
            val editTexts = listOf(nameEditText, emailEditText, passwordEditText)
            for (editText in editTexts) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val et1 = nameEditText.text.toString().trim()
                        val et2 = emailEditText.text.toString().trim()
                        val et3 = passwordEditText.text.toString().trim()

                        // Enable button if the EditTexts are not empty
                        // and a RadioButton is checked
                        registerButton.isEnabled = et1.isNotEmpty()
                                && et2.isNotEmpty()
                                && et3.isNotEmpty()
                                && binding.parentalRoleRadioGroup.checkedRadioButtonId != -1
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            // Set parental role RadioGroup listener
            parentalRoleRadioGroup.setOnCheckedChangeListener { _, checkedButtonId ->
                val et1 = nameEditText.text.toString().trim()
                val et2 = emailEditText.text.toString().trim()
                val et3 = passwordEditText.text.toString().trim()

                // Enable button if the EditTexts are not empty
                // and a RadioButton is checked
                registerButton.isEnabled = et1.isNotEmpty()
                        && et2.isNotEmpty()
                        && et3.isNotEmpty()
                        && checkedButtonId != -1
            }

            // Set register button onCLickListener
            registerButton.setOnClickListener {
                val email = binding.emailEditText.text.toString().lowercase()
                val password = binding.passwordEditText.text.toString()

                // Call the method to register user
                viewModel.registerUser(email, password)
            }

            // Observe user LiveData in ViewModel
            // This value determines whether user registration is successful or not
            viewModel.user.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        Toast.makeText(context, getString(R.string.register_success), Toast.LENGTH_SHORT).show()

                        // Build navigation options to pop all auth fragments before navigating
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.welcomeFragment, true)
                            .build()

                        // Navigate to role selection page
                        val action = RegisterFragmentDirections.actionRegisterFragmentToRoleSelectionFragment()
                        findNavController().navigate(action, navOptions)
                    }
                    is Response.Failure -> {
                        Log.e("Register", response.message)
                        Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Set login option text onClickListener
            loginInsteadText.setOnClickListener {
                // Build navigation options to pop this fragment before navigating
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.registerFragment, true)
                    .build()

                // Navigate to login page
                val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
                findNavController().navigate(action, navOptions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}