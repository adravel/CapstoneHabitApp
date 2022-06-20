package com.fortfighter.ui.fragment

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
import com.fortfighter.R
import com.fortfighter.databinding.FragmentTaskCreationManualBinding
import com.fortfighter.model.Task
import com.fortfighter.util.Response
import com.fortfighter.util.getTaskDifficultyString
import com.fortfighter.util.validateTimeLimit
import com.fortfighter.util.validateTimeStringFormat
import com.fortfighter.viewmodel.TaskCreationManualViewModel
import com.fortfighter.viewmodel.TaskDetailViewModel

class TaskCreationManualFragment : Fragment() {

    private var _binding: FragmentTaskCreationManualBinding? = null
    private val binding get() = _binding!!

    private var isForEditing: Boolean? = null

    private val taskCreationManualViewModel: TaskCreationManualViewModel by viewModels()
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
                        taskCreationManualViewModel.setEditedTaskId(task.id)
                    }
                }
            }

            // Set create task button onClickListener
            createTaskButton.setOnClickListener {
                val title = titleEditText.text.toString()
                val category = categoryEditText.text.toString()
                val area = areaEditText.text.toString()
                val difficulty = taskCreationManualViewModel.getDifficultyInt(difficultyAutoCompleteTextView.text.toString())
                val startTimeLimit = startTimeLimitEditText.text.toString()
                val finishTimeLimit = finishTimeLimitEditText.text.toString()
                val detail = detailEditText.text.toString()

                val task = Task(
                    title = title,
                    category = category,
                    area = area,
                    difficulty = difficulty.toLong(),
                    startTimeLimit = startTimeLimit,
                    finishTimeLimit = finishTimeLimit,
                    detail = detail
                )

                // Process the task data depending on the state of time limit EditTexts
                if (startTimeLimit.isEmpty() && finishTimeLimit.isEmpty()) {
                    // Both time limit EditTexts are empty
                    // Process task that does not have time limit
                    writeTask(task)
                } else if (startTimeLimit.isNotEmpty() && finishTimeLimit.isNotEmpty()) {
                    // Both time limit EditTexts are not empty
                    // Check if both time limit String inputs are valid time format
                    if (validateTimeStringFormat(startTimeLimit) &&
                        validateTimeStringFormat(finishTimeLimit)
                    ) {
                        // Both time limit String inputs are valid time format
                        // Check if the finish time limit is after the start time limit
                        if (validateTimeLimit(startTimeLimit, finishTimeLimit)) {
                            // Finish time limit is after the start time limit
                            // Process task that has time limit
                            writeTask(task)
                        } else {
                            // Finish time limit is before the start time limit
                            Toast.makeText(context, getString(R.string.time_limit_invalid), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // At least one time limit is not a valid time format
                        Toast.makeText(context, getString(R.string.time_format_invalid), Toast.LENGTH_LONG).show()
                    }
                } else {
                    // One time limit EditText is empty but the other is not
                    Toast.makeText(context, getString(R.string.time_limit_state_error), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Process the data depending on
    // whether this fragment is used for
    // creating or editing task
    private fun writeTask(task: Task ) {
        if (isForEditing == false) {
            // Add new task data to Firestore
            taskCreationManualViewModel.addTaskToFirebase(task)

            // Observe task ID data in ViewModel
            taskCreationManualViewModel.taskId.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        val taskId = response.data

                        Toast.makeText(context, getString(R.string.task_creation_success), Toast.LENGTH_SHORT).show()

                        // Build navigation options to pop into taskListFragment before navigating
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.taskListFragment, false)
                            .build()

                        // Navigate to task detail page
                        val action = TaskCreationManualFragmentDirections
                            .actionTaskCreationManualFragmentToTaskDetailFragment(taskId)
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
            taskCreationManualViewModel.updateTask(task)

            // Observe task ID data in ViewModel
            taskCreationManualViewModel.taskId.observe(viewLifecycleOwner) { response ->
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
                            .actionTaskCreationManualFragmentToTaskDetailFragment(taskId)
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