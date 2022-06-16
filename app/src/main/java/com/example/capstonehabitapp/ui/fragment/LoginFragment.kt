package com.example.capstonehabitapp.ui.fragment

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
import com.example.capstonehabitapp.databinding.FragmentLoginBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.LoginViewModel

class LoginFragment: Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

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

        binding.apply {
            // Disable register button by default
            loginButton.isEnabled = false

            // Set textChangedListener for the EditTexts
            val editTexts = listOf(emailEditText, passwordEditText)
            for (editText in editTexts) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val et1 = emailEditText.text.toString().trim()
                        val et2 = passwordEditText.text.toString().trim()

                        // Enable button if the EditTexts is not empty
                        loginButton.isEnabled = et1.isNotEmpty() && et2.isNotEmpty()
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            // Set login button onCLickListener
            loginButton.setOnClickListener {
                val email = binding.emailEditText.text.toString().lowercase()
                val password = binding.passwordEditText.text.toString()

                // Call the method to register user
                viewModel.loginUser(email, password)
            }

            // Observe loginResponse LiveData in ViewModel
            // to determine whether user login is successful or not
            viewModel.loginResponse.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        Toast.makeText(context, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

                        // Build navigation options to pop all auth fragments before navigating
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.welcomeFragment, true)
                            .build()

                        // Navigate to role selection page
                        val action = LoginFragmentDirections.actionLoginFragmentToRoleSelectionFragment()
                        findNavController().navigate(action, navOptions)
                    }
                    is Response.Failure -> {
                        Log.e("Login", response.message)
                        Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Set register option text onClickListener
            registerInsteadText.setOnClickListener {
                // Build navigation options to pop this fragment before navigating
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()

                // Navigate to register page
                val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                findNavController().navigate(action, navOptions)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}