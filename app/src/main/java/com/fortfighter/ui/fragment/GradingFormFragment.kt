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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.FragmentGradingFormBinding
import com.fortfighter.model.Task
import com.fortfighter.util.Response
import com.fortfighter.util.emptyTextWrapper
import com.fortfighter.util.timeLimitTextWrapper
import com.fortfighter.viewmodel.TaskDetailViewModel

class GradingFormFragment: Fragment() {

    private var _binding: FragmentGradingFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()

        // Set array adapter for difficulty drop-down menu
        val difficulties = resources.getStringArray(R.array.grades)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, difficulties)
        binding.gradeAutoCompleteTextView.setAdapter(arrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentGradingFormBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.grading_form)

        // Hide task photo pop up by default
        binding.popupCard.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Set close button onClickListener to hide task photo pop up
            closeButton.setOnClickListener {
                binding.popupCard.visibility = View.GONE
            }

            // Disable grade task button by default
            gradeTaskButton.isEnabled = false

            // Set TextChangedListener for task grade AutoCompleteTextView
            gradeAutoCompleteTextView.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val gradeText = gradeAutoCompleteTextView.text.toString()

                    // Enable button if the EditTexts is not empty
                    gradeTaskButton.isEnabled = gradeText.isNotEmpty()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            // Observe task LiveData in SharedViewModel
            viewModel.task.observe(viewLifecycleOwner) { taskResponse ->
                if (taskResponse is Response.Success) {
                    val task = taskResponse.data

                    // Display task data in finished state
                    displayTaskData(task)

                    // Set open photo button behavior depending on task grading method
                    if (task.timeAskForGrading == null) {
                        // Task is being graded directly
                        // Disable button
                        openPhotoButton.isEnabled = false
                    } else {
                        // Task is being graded remotely
                        // Set button onClickListener
                        openPhotoButton.isEnabled = true
                        openPhotoButton.setOnClickListener {
                            // Fetch task photo image data
                            viewModel.getTaskPhotoFromStorage(task.id)

                            // Display task photo pop up
                            popupCard.visibility = View.VISIBLE
                        }
                    }

                    // Set grade task button OnClickListener
                    gradeTaskButton.setOnClickListener {
                        // Get grade data in the form of integer
                        val grade = viewModel.getGradeInt(gradeAutoCompleteTextView.text.toString())

                        // Calculate points that the child will get after completing the task
                        val gradePoints = viewModel.getGradePoints(task.difficulty.toInt(), grade)

                        val notes = notesEditText.text.toString()

                        // Grade task by updating task and child documents in Firestore
                        viewModel.gradeTask(
                            task.id,
                            task.childId,
                            grade,
                            gradePoints,
                            notes
                        )
                    }
                }
            }

            // Observe taskPhoto LiveData in ViewModel
            viewModel.taskPhoto.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        val imageBitmap = response.data

                        // Load photo image data
                        Glide.with(this@GradingFormFragment)
                            .load(imageBitmap)
                            .placeholder(R.color.black)
                            .into(photoImage)
                    }
                    is Response.Failure -> {
                        Log.e("GradingForm", response.message)
                        Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Observe taskStatusChange LiveData in ViewModel
            // This value determines whether the grading transaction is successful or not
            viewModel.taskStatusChange.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        // Clear task photo LiveData in ViewModel
                        viewModel.clearTaskPhoto()

                        // Return to task detail page
                        findNavController().popBackStack()
                    }
                    is Response.Failure -> {
                        Log.e("GradingForm", response.message)
                        Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Set back button onClickListener
            toolbarLayout.toolbar.setNavigationOnClickListener {
                // Clear task photo LiveData in ViewModel
                viewModel.clearTaskPhoto()

                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Display task data in finished state
    private fun displayTaskData(task: Task) {
        binding.apply {
            titleDataText.text = task.title
            categoryDataText.text = emptyTextWrapper(task.category)
            areaDataText.text = task.area
            difficultyDataText.text = when (task.difficulty.toInt()) {
                0 -> getString(R.string.task_difficulty_0)
                1 -> getString(R.string.task_difficulty_1)
                2 -> getString(R.string.task_difficulty_2)
                else -> getString(R.string.task_difficulty_0)
            }
            // TODO: Implement task repetition data display in ViewModel
            repetitionDataText.text = "-"
            timeLimitDataText.text = timeLimitTextWrapper(
                requireContext(),
                task.startTimeLimit,
                task.finishTimeLimit
            )
            durationDataText.text = viewModel.getTaskDurationString(task)
            if (task.status.toInt() == 2) {
                // When user chose to grade directly
                statusDataText.text = getString(R.string.task_status_2_with_child_name, task.childName)
                statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
            } else {
                // When user chose to grade remotely
                statusDataText.text = getString(R.string.task_status_3_for_parent_with_child_name, task.childName)
                statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
            }
            detailDataText.text = emptyTextWrapper(task.detail)
        }
    }
}