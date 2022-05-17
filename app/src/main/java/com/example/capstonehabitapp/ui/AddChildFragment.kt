package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentAddChildBinding

class AddChildFragment: Fragment() {

    private var _binding: FragmentAddChildBinding? = null
    private val binding get() = _binding!!

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
                    val gradeText = childNameEditText.text.toString().trim()

                    // Enable button if the EditText are not empty
                    // and a RadioButton is checked
                    addChildButton.isEnabled = gradeText.isNotEmpty()
                            && childGenderRadioGroup.checkedRadioButtonId != -1
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            // Set child gender RadioGroup listener
            childGenderRadioGroup.setOnCheckedChangeListener { _, checkedButtonId ->
                val gradeText = childNameEditText.text.toString().trim()

                // Enable button if the EditText are not empty
                // and a RadioButton is checked
                addChildButton.isEnabled = gradeText.isNotEmpty() && checkedButtonId != -1
            }

            // Set add child button onCLickListener
            addChildButton.setOnClickListener {
                val name = childNameEditText.text.toString().lowercase()
                val isMale = childGenderRadioGroup.checkedRadioButtonId == R.id.male_radio_button

                Log.i("AddChild", "$name, $isMale")
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