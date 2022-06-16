package com.example.capstonehabitapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationManualBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.util.getTaskDifficultyString
import com.example.capstonehabitapp.viewmodel.TaskCreationViewModel
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class TaskCreationManualFragment : Fragment() {

    private var _binding: FragmentTaskCreationManualBinding? = null
    private val binding get() = _binding!!

    private var isForEditing: Boolean? = null

    private val taskCreationViewModel: TaskCreationViewModel by viewModels()
    private val taskDetailViewModel: TaskDetailViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()

        // Set array adapter for difficulty drop-down menu
        val difficulties = resources.getStringArray(R.array.difficulties)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, difficulties)
        binding.difficultyAutoCompleteTextView.setAdapter(arrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskCreationManualBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.create_task)

        // Initialize isForEditing variable using Safe Args provided by navigation component
        val args: TaskCreationManualFragmentArgs by navArgs()
        isForEditing = args.isForEditing

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable create task button by default
            createTaskButton.isEnabled = false

            // Set textChangedListener for task title, area, and difficulty EditTexts
            val editTexts = listOf(titleEditText, areaEditText, difficultyAutoCompleteTextView)
            for (editText in editTexts) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val et1 = titleEditText.text.toString().trim()
                        val et2 = areaEditText.text.toString().trim()
                        val et3 = difficultyAutoCompleteTextView.text.toString().trim()

                        // Enable button if the EditTexts is not empty
                        createTaskButton.isEnabled = et1.isNotEmpty()
                                && et2.isNotEmpty()
                                && et3.isNotEmpty()
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            // Set back button onClickListener
            toolbarLayout.toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            // Listen to task LiveData in SharedViewModel if this fragment is used for editing
            if (isForEditing == true) {
                taskDetailViewModel.task.observe(viewLifecycleOwner) { response ->
                    if (response is Response.Success) {
                        val task = response.data

                        // Populate the EditTexts with task data from SharedViewModel
                        titleEditText.setText(task.title)
                        categoryEditText.setText(task.category)
                        areaEditText.setText(task.area)
                        difficultyAutoCompleteTextView.setText(
                            getTaskDifficultyString(requireContext(), task.difficulty.toInt())
                        )
                        startTimeLimitEditText.setText(task.startTimeLimit)
                        finishTimeLimitEditText.setText(task.finishTimeLimit)
                        detailEditText.setText(task.detail)

                        // Set the task ID value in ViewModel
                        taskCreationViewModel.setEditedTaskId(task.id)
                    }
                }
            }

            // Set create task button onClickListener
            createTaskButton.setOnClickListener {
                val title = titleEditText.text.toString()
                val category = categoryEditText.text.toString()
                val area = areaEditText.text.toString()
                val difficulty = taskCreationViewModel.getDifficultyInt(difficultyAutoCompleteTextView.text.toString())
                val startTimeLimit = startTimeLimitEditText.text.toString()
                val finishTimeLimit = finishTimeLimitEditText.text.toString()
                val detail = detailEditText.text.toString()

                // Call the appropriate method depending on
                // whether this fragment is used for
                // creating or editing task
                if (isForEditing == false) {
                    // Add new task data to Firestore
                    taskCreationViewModel.addTaskToFirebase(
                        title,
                        category,
                        area,
                        difficulty,
                        startTimeLimit,
                        finishTimeLimit,
                        detail
                    )

                    // Observe task ID data in ViewModel
                    taskCreationViewModel.taskId.observe(viewLifecycleOwner) { response ->
                        when (response) {
                            is Response.Loading -> {}
                            is Response.Success -> {
                                val taskId = response.data

                                Toast.makeText(context, getString(R.string.task_creation_success), Toast.LENGTH_SHORT).show()

                                // Build navigation options to pop this fragment before navigating
                                val navOptions = NavOptions.Builder()
                                    .setPopUpTo(R.id.taskCreationFragment, true)
                                    .build()

                                // Navigate to task detail page
                                val action = TaskCreationManualFragmentDirections
                                    .actionTaskCreationFragmentToTaskDetailFragment(taskId)
                                findNavController().navigate(action, navOptions)
                            }
                            is Response.Failure -> {
                                Log.e("TaskCreation", response.message)
                                Toast.makeText(context, getString(R.string.task_creation_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Update task data in Firestore
                    taskCreationViewModel.updateTask(title, area, difficulty, startTimeLimit, finishTimeLimit, detail)

                    // Observe task ID data in ViewModel
                    taskCreationViewModel.taskId.observe(viewLifecycleOwner) { response ->
                        when (response) {
                            is Response.Loading -> {}
                            is Response.Success -> {
                                val taskId = response.data

                                // Build navigation options to pop this fragment and
                                // TaskDetailFragment before navigating
                                val navOptions = NavOptions.Builder()
                                    .setPopUpTo(R.id.taskDetailFragment, true)
                                    .build()

                                // Navigate to task detail page
                                val action = TaskCreationManualFragmentDirections
                                    .actionTaskCreationFragmentToTaskDetailFragment(taskId)
                                findNavController().navigate(action, navOptions)
                            }
                            is Response.Failure -> {
                                Log.e("TaskCreation", response.message)
                                Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}