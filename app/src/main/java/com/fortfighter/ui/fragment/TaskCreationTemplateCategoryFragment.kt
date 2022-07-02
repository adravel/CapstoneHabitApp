package com.fortfighter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.FragmentTaskCreationTemplateCategoryBinding
import com.fortfighter.viewmodel.TaskCreationTemplateViewModel

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
        binding.toolbarLayout.toolbar.title = getString(R.string.select_category)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Observe timeLimit LiveData in ViewModel
            viewModel.timeLimit.observe(viewLifecycleOwner) {
                val (startTimeLimit, finishTimeLimit) = it

                // Change oneTimeTaskCard appearance and behavior
                // depending on whether time limit is set
                if (startTimeLimit.isNotEmpty() && finishTimeLimit.isNotEmpty()) {
                    // Time limit is set
                    // Change card background color to subtle green
                    oneTimeTaskCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.subtle_green)
                    )

                    // Change description text appearance
                    oneTimeTaskCardDescriptionText.text = "$startTimeLimit - $finishTimeLimit"
                    oneTimeTaskCardDescriptionText.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.green)
                    )

                    // Set the card card onCLickListener
                    oneTimeTaskCard.setOnClickListener {
                        // Unset the time limit
                        viewModel.setTimeLimit("", "")
                    }
                } else {
                    // Time limit is not set
                    // Change card background color to white
                    oneTimeTaskCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.white)
                    )

                    // Change description text appearance
                    oneTimeTaskCardDescriptionText.text = getString(R.string.select_time)
                    oneTimeTaskCardDescriptionText.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.state_error_dark)
                    )

                    // Set task repetition card onCLickListener
                    oneTimeTaskCard.setOnClickListener {
                        // Display time picker dialog
                        findNavController().navigate(R.id.timePickerDialogFragment)
                    }
                }
            }

            // Set all category cards onCLickListener
            val categoryCards = listOf(
                creativityCategoryCard,
                houseworkCategoryCard,
                healthCategoryCard,
                selfCareCategoryCard
            )
            for (categoryCard in categoryCards) {
                categoryCard.setOnClickListener {
                    // Set the selected template task category as the String obtained from CardView's Tag
                    viewModel.setTemplateTaskCategory(categoryCard.tag.toString())

                    // Navigate to taskCreationTemplateTask
                    findNavController().navigate(R.id.taskCreationTemplateTaskFragment)
                }
            }

            // Display cards icon image
            Glide.with(this@TaskCreationTemplateCategoryFragment).apply {
                this.load(R.drawable.img_template_task_one_time).into(oneTimeTaskCardImage)
                this.load(R.drawable.img_template_task_repeated).into(repeatedTaskCardImage)
                this.load(R.drawable.img_category_creativity).into(creativityCategoryCardImage)
                this.load(R.drawable.img_category_housework).into(houseworkCategoryCardImage)
                this.load(R.drawable.img_category_health).into(healthCategoryCardImage)
                this.load(R.drawable.img_category_self_care).into(selfCareCategoryCardImage)
            }

            // Set create task manual button onClickListener
            createTaskManualButton.setOnClickListener {
                // Navigate to TaskCreationManualFragment with false argument
                val action = TaskCreationTemplateCategoryFragmentDirections
                    .actionTaskCreationTemplateCategoryFragmentToTaskCreationManualFragment(false)
                findNavController().navigate(action)
            }

            // Set back button onClickListener
            toolbarLayout.toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}