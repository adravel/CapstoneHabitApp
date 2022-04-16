package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentGradingFormBinding
import com.example.capstonehabitapp.viewmodel.GradingFormViewModel
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class GradingFormFragment: Fragment() {

    private var _binding: FragmentGradingFormBinding? = null
    private val binding get() = _binding!!

    private val taskDetailViewModel: TaskDetailViewModel by activityViewModels()
    private val gradingFormViewModel: GradingFormViewModel by viewModels()

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
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
            taskDetailViewModel.task.observe(viewLifecycleOwner) { task ->
                // Fetch child data from Firestore
                gradingFormViewModel.getChildFromFirebase(task.childId)

                // Display task data in finished state
                titleDataText.text = task.title
                areaDataText.text = task.area
                difficultyDataText.text = when (task.difficulty.toInt()) {
                    0 -> getString(R.string.task_difficulty_0)
                    1 -> getString(R.string.task_difficulty_1)
                    2 -> getString(R.string.task_difficulty_2)
                    else -> getString(R.string.task_difficulty_0)
                }
                // TODO: Implement task repetition data display in ViewModel
                repetitionDataText.text = "NOT_YET_IMPLEMENTED"
                timeLimitDataText.text = getString(
                    R.string.task_time_limit_placeholder,
                    task.startTimeLimit,
                    task.finishTimeLimit
                )
                durationDataText.text = taskDetailViewModel.getTaskDurationString(task)
                if (task.status.toInt() == 2) {
                    // When user chose to grade directly
                    statusDataText.text = getString(R.string.task_status_2_with_child_name, task.childName)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
                } else {
                    // When user chose to grade remotely
                    statusDataText.text = getString(R.string.task_status_3_for_parent_role_with_child_name, task.childName)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                }
                detailDataText.text = task.detail

                // Observe child LiveData in ViewModel
                gradingFormViewModel.child.observe(viewLifecycleOwner) { child ->
                    // Set grade task button OnClickListener
                    gradeTaskButton.setOnClickListener {
                        // Grade data in the form of integer
                        val grade = gradingFormViewModel.getGradeInt(gradeAutoCompleteTextView.text.toString())

                        // Points that the child will get after completing the task
                        val gradePoints = gradingFormViewModel.getGradePoints(task.difficulty.toInt(), grade)

                        val notes = notesEditText.text.toString()

                        // Update task data to Firestore
                        gradingFormViewModel.gradeTask(task.id, grade, notes)

                        // Update child data to Firestore
                        gradingFormViewModel.updateChildPointsAndLevel(child, gradePoints)

                        view.findNavController().popBackStack()
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