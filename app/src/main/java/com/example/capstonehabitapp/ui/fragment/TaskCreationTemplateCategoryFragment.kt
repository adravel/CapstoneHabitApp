package com.example.capstonehabitapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationTemplateCategoryBinding
import com.example.capstonehabitapp.viewmodel.TaskCreationTemplateViewModel

class TaskCreationTemplateCategoryFragment : Fragment() {

    private var _binding: FragmentTaskCreationTemplateCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskCreationTemplateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskCreationTemplateCategoryBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.choose_category)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe timeLimit LiveData in ViewModel
        viewModel.timeLimit.observe(viewLifecycleOwner) {
            val (startTimeLimit, finishTimeLimit) = it

            // Change oneTimeTaskCard appearance and behavior
            // depending on whether time limit is set
            if (startTimeLimit.isNotEmpty() && finishTimeLimit.isNotEmpty()) {
                // Time limit is set
                // Change card background color to subtle green
                binding.oneTimeTaskCard.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.subtle_green)
                )

                // Change description text appearance
                binding.oneTimeTaskCardDescriptionText.text = "$startTimeLimit - $finishTimeLimit"
                binding.oneTimeTaskCardDescriptionText.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.green)
                )

                // Set the card card onCLickListener
                binding.oneTimeTaskCard.setOnClickListener {
                    // Unset the time limit
                    viewModel.setTimeLimit("", "")
                }
            } else {
                // Time limit is not set
                // Change card background color to white
                binding.oneTimeTaskCard.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                // Change description text appearance
                binding.oneTimeTaskCardDescriptionText.text = getString(R.string.choose_time)
                binding.oneTimeTaskCardDescriptionText.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.state_error_dark)
                )

                // Set task repetition card onCLickListener
                binding.oneTimeTaskCard.setOnClickListener {
                    // Display time picker dialog
                    findNavController().navigate(R.id.timePickerDialogFragment)
                }
            }
        }

        // Set all category cards onCLickListener
        val categoryCards = listOf(
            binding.creativityCategoryCard,
            binding.houseworkCategoryCard,
            binding.healthCategoryCard,
            binding.selfCareCategoryCard
        )
        for (categoryCard in categoryCards) {
            categoryCard.setOnClickListener {
                // Set the selected template task category as the String obtained from CardView's Tag
                viewModel.setTemplateTaskCategory(categoryCard.tag.toString())

                // Navigate to taskCreationTemplateTask
                findNavController().navigate(R.id.taskCreationTemplateTaskFragment)
            }
        }

        // Set create task manual button onClickListener
        binding.createTaskManualButton.setOnClickListener {
            // Navigate to TaskCreationManualFragment with false argument
            val action = TaskCreationTemplateCategoryFragmentDirections
                .actionTaskCreationTemplateCategoryFragmentToTaskCreationManualFragment(false)
            findNavController().navigate(action)
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