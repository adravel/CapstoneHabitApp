package com.example.capstonehabitapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationTemplateTaskBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.util.getTaskDifficultyImageResId
import com.example.capstonehabitapp.util.getTaskDifficultyString
import com.example.capstonehabitapp.viewmodel.TaskCreationTemplateViewModel

class TaskCreationTemplateTaskFragment : Fragment() {

    private var _binding: FragmentTaskCreationTemplateTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskCreationTemplateViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskCreationTemplateTaskBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.choose_task)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load template task data from SharedViewModel
        val templateTask = viewModel.getTemplateTask()

        // Display template task data
        binding.templateTaskCardLayout.apply {
            val difficultyInt = templateTask.difficulty.toInt()
            val difficultyString = getTaskDifficultyString(requireContext(), difficultyInt)

            titleText.text = templateTask.title
            infoText.text = "${templateTask.area} - $difficultyString"
            difficultyImage.setImageResource(getTaskDifficultyImageResId(difficultyInt))

            // Set template task card onCLickListener
            card.setOnClickListener {
                // Add the template task to Firestore
                viewModel.addTaskToFirebase(templateTask)
            }
        }

        // Observe task ID data in ViewModel
        viewModel.taskId.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.taskIdResponseHandled()

                    val taskId = response.data

                    Toast.makeText(context, getString(R.string.task_creation_success), Toast.LENGTH_SHORT).show()

                    // Build navigation options to pop into taskListFragment before navigating
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.taskListFragment, false)
                        .build()

                    // Navigate to task detail page
                    val action = TaskCreationTemplateTaskFragmentDirections
                        .actionTaskCreationTemplateTaskFragmentToTaskDetailFragment(taskId)
                    findNavController().navigate(action, navOptions)
                }
                is Response.Failure -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.taskIdResponseHandled()

                    Log.e("TaskCreation", response.message)
                    Toast.makeText(context, getString(R.string.task_creation_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set create task manual button onClickListener
        binding.createTaskManualButton.setOnClickListener {
            // Navigate to TaskCreationManualFragment with false argument
            val action = TaskCreationTemplateTaskFragmentDirections
                .actionTaskCreationTemplateTaskFragmentToTaskCreationManualFragment(false)
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