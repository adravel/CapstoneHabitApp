package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentGradingMethodSelectionDialogBinding
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class GradingMethodSelectionDialogFragment: DialogFragment() {

    private var _binding: FragmentGradingMethodSelectionDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentGradingMethodSelectionDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.directGradingButton.setOnClickListener {
            // Navigate to grading form page
            findNavController().navigate(R.id.gradingFormFragment)
        }

        binding.remoteGradingButton.setOnClickListener {
            // Ask parent for grading
            viewModel.task.observe(viewLifecycleOwner) { task ->
                viewModel.askForGrading(task.id)
            }

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}