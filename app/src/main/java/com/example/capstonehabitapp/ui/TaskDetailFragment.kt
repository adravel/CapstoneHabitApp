package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskDetailBinding
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskId: String

    private val viewModel: TaskDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.task_detail)

        // Initialize task ID using Safe Args provided by navigation component
        val args: TaskDetailFragmentArgs by navArgs()
        taskId = args.taskId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user's role, child ID, and child name data from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref?.getBoolean(getString(R.string.role_pref_is_parent_key), true)
        val childId = sharedPref?.getString(getString(R.string.role_pref_child_id_key), "")
        val childName = sharedPref?.getString(getString(R.string.role_pref_child_name_key), "")

        // Fetch task detail data from Firestore
        viewModel.getTaskFromFirebase(taskId)

        // Observe task LiveData in SharedViewModel
        viewModel.task.observe(viewLifecycleOwner) { task ->

            // TODO: Fix bug in displaying task data when navigating from grading form page (task ID does not change so the solution below does not work)
            // Handle the case of observing task LiveData multiple times
            if (task.id != taskId) {
                // Reset task data and make all Views that might be hidden visible
                displayTaskData(Task(), isParent!!)
                binding.apply {
                    View.VISIBLE.let {
                        gradePointsText.visibility = it
                        gradePointsDataText.visibility = it
                        notesText.visibility = it
                        notesDataText.visibility = it
                        changeTaskStatusButton.visibility = it
                    }
                }
            } else {
                // Display task data correctly
                displayTaskData(task, isParent!!)
            }

            // Set button OnClickListener depending on the task status and user's role
            binding.changeTaskStatusButton.setOnClickListener {
                if (isParent == true) {
                    //Navigate to grading form page
                    view.findNavController().navigate(R.id.gradingFormFragment)
                } else {
                    when (task.status.toInt()) {
                        0 -> viewModel.startTask(taskId, childId!!, childName!!)
                        1 -> viewModel.finishTask(taskId)
                        2 -> view.findNavController().navigate(R.id.gradingMethodSelectionDialogFragment)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Display task data
    private fun displayTaskData(task: Task, isForParent: Boolean) {
        binding.apply {
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
            detailDataText.text = task.detail

            // Display the rest of the data according to status
            when (task.status.toInt()) {

                // State: Task default state
                0 -> {
                    durationDataText.text = "-"
                    statusDataText.text = getString(R.string.task_status_0)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                    if (isForParent) {
                        gradePointsDataText.text = "-"
                        notesDataText.text = "-"
                        changeTaskStatusButton.visibility = View.GONE
                    } else {
                        View.GONE.let {
                            gradePointsText.visibility = it
                            gradePointsDataText.visibility = it
                            notesText.visibility = it
                            notesDataText.visibility = it
                        }
                        changeTaskStatusButton.text = getString(R.string.button_label_start_task)
                    }
                }

                // State: Task is in progress
                1 -> {
                    durationDataText.text = "-"
                    statusDataText.text = getString(R.string.task_status_1_with_child_name, task.childName)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_warning_dark))
                    if (isForParent) {
                        gradePointsDataText.text = "-"
                        notesDataText.text = "-"
                        changeTaskStatusButton.visibility = View.GONE
                    } else {
                        View.GONE.let {
                            gradePointsText.visibility = it
                            gradePointsDataText.visibility = it
                            notesText.visibility = it
                            notesDataText.visibility = it
                        }
                        changeTaskStatusButton.text = getString(R.string.button_label_finish_task)
                    }
                }

                // State: Task is finished
                2 -> {
                    durationDataText.text = viewModel.getTaskDurationString(task)
                    statusDataText.text = getString(R.string.task_status_2_with_child_name, task.childName)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
                    if (isForParent) {
                        gradePointsDataText.text = "-"
                        notesDataText.text = "-"
                        changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                        changeTaskStatusButton.isEnabled = false
                    } else {
                        View.GONE.let {
                            gradePointsText.visibility = it
                            gradePointsDataText.visibility = it
                            notesText.visibility = it
                            notesDataText.visibility = it
                        }
                        changeTaskStatusButton.text = getString(R.string.button_label_ask_for_grading)
                    }
                }

                // State: Waiting for grading
                3 -> {
                    durationDataText.text = viewModel.getTaskDurationString(task)
                    if (isForParent) {
                        statusDataText.text = getString(R.string.task_status_3_for_parent_role_with_child_name, task.childName)
                        statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                        changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                    } else {
                        statusDataText.text = getString(R.string.task_status_3_for_child_role)
                        statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_info))
                        changeTaskStatusButton.visibility = View.GONE
                    }
                    gradePointsDataText.text = "-"
                    notesDataText.text = "-"
                }

                // State: Task has been graded
                4 -> {
                    durationDataText.text = viewModel.getTaskDurationString(task)
                    statusDataText.text = getString(R.string.task_status_4)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
                    gradePointsDataText.text = task.gradePoints.toString()
                    notesDataText.text = task.notes
                    changeTaskStatusButton.visibility = View.GONE
                }
            }
        }
    }
}