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
import androidx.navigation.fragment.findNavController
import com.fortfighter.R
import com.fortfighter.databinding.FragmentAddChildBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.AddChildViewModel

class AddChildFragment: Fragment() {

    private var _binding: FragmentAddChildBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddChildViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentAddChildBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.add_child)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable add child button by default
            addChildButton.isEnabled = false

            // Set TextChangedListener for child name EditText
            childNameEditText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val childNameText = childNameEditText.text.toString().trim()

                    // Enable button if the EditText are not empty
                    // and a RadioButton is checked
                    addChildButton.isEnabled = childNameText.isNotEmpty()
                            && childGenderRadioGroup.checkedRadioButtonId != -1
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            // Set child gender RadioGroup listener
            childGenderRadioGroup.setOnCheckedChangeListener { _, checkedButtonId ->
                val childNameText = childNameEditText.text.toString().trim()

                // Enable button if the EditText are not empty
                // and a RadioButton is checked
                addChildButton.isEnabled = childNameText.isNotEmpty() && checkedButtonId != -1
            }

            // Set add child button onCLickListener
            addChildButton.setOnClickListener {
                val name = childNameEditText.text.toString()
                val isMale = childGenderRadioGroup.checkedRadioButtonId == R.id.male_radio_button

                // Create new child document in Firebase
                viewModel.addChildToFirebase(name, isMale)
            }
        }

        // Observe addChildResponse LiveData in ViewModel
        // to determine whether child addition query is successful or not
        viewModel.addChildResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    Toast.makeText(context, getString(R.string.child_addition_success), Toast.LENGTH_SHORT).show()

                    // Return to child account selection page
                    findNavController().popBackStack()
                }
                is Response.Failure -> {
                    Log.e("AddChild", response.message)
                    Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
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