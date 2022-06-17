package com.example.capstonehabitapp.ui.dialog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.DialogTimePickerBinding
import com.example.capstonehabitapp.viewmodel.TaskCreationTemplateViewModel

class TimePickerDialogFragment: DialogFragment() {

    private var _binding: DialogTimePickerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskCreationTemplateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogTimePickerBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable choose button by default
            chooseButton.isEnabled = false

            // Set textChangedListener for both time limit editTexts
            val editTexts = listOf(startTimeLimitEditText, finishTimeLimitEditText)
            for (editText in editTexts) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val et1 = startTimeLimitEditText.text.toString().trim()
                        val et2 = finishTimeLimitEditText.text.toString().trim()

                        // Enable button if the EditTexts are not empty
                        chooseButton.isEnabled = et1.isNotEmpty() && et2.isNotEmpty()
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            // Set choose button onClickListener for setting the time limit
            chooseButton.setOnClickListener {
                val startTimeLimit = startTimeLimitEditText.text.toString()
                val finishTimeLimit = finishTimeLimitEditText.text.toString()

                // Store time limit data in SharedViewModel
                viewModel.setTimeLimit(startTimeLimit, finishTimeLimit)

                // Dismiss this dialog
                findNavController().popBackStack()
            }

            // Set cancel button onClickListener for canceling action
            cancelButton.setOnClickListener {
                // Dismiss this dialog
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}