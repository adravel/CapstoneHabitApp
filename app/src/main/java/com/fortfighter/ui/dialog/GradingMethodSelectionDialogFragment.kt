package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.fortfighter.R
import com.fortfighter.databinding.DialogTwoButtonsBinding

class GradingMethodSelectionDialogFragment: DialogFragment() {

    private var _binding: DialogTwoButtonsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogTwoButtonsBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.grading_method_selection_message)
        binding.positiveButton.text = getString(R.string.button_label_yes)
        binding.negativeButton.text = getString(R.string.button_label_no)

        // Set button onClickListener for selecting direct grading method
        binding.positiveButton.setOnClickListener {
            // Navigate to parent account verification page
            val action = GradingMethodSelectionDialogFragmentDirections
                .actionGradingMethodSelectionDialogFragmentToParentAccountVerificationFragment(
                    true
                )
            findNavController().navigate(action)
        }

        // Set button onClickListener for selecting remote grading method
        binding.negativeButton.setOnClickListener {
            // Navigate to uploadPhotoFragment
            findNavController().navigate(R.id.uploadPhotoFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}