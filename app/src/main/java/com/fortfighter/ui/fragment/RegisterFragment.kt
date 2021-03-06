package com.fortfighter.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.fortfighter.R
import com.fortfighter.databinding.FragmentRegisterBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.RegisterViewModel

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
                                && et3.length >= 6
                                && binding.parentalRoleRadioGroup.checkedRadioButtonId != -1
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            // Show error message if password length is less than 6 characters
            // which is the required minimum length for Firebase Auth
            passwordEditText.doOnTextChanged { text, _, _, _ ->
                if (text!!.length < 6) {
                    passwordTextInputLayout.error = getString(R.string.minimum_password_length_error)
                    passwordTextInputLayout.setErrorIconDrawable(0)
                } else {
                    passwordTextInputLayout.error = null
                }
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
                val email = emailEditText.text.toString().lowercase()
                val password = passwordEditText.text.toString()
                val name = nameEditText.text.toString()
                val isMale = parentalRoleRadioGroup.checkedRadioButtonId == R.id.father_radio_button

                // Call the method to register user
                viewModel.registerUser(email, password, name, isMale)
            }

            // Observe registerResponse LiveData in ViewModel
            // to determine whether user registration is successful or not
            viewModel.registerResponse.observe(viewLifecycleOwner) { response ->
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