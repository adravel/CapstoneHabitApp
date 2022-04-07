package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskDetailBinding
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskId: String

    private lateinit var viewModel: TaskDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_detail)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[TaskDetailViewModel::class.java]

        // Initialize task ID using Safe Args provided by navigation component
        val args: TaskDetailFragmentArgs by navArgs()
        taskId = args.taskId

        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
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

        // Observe task LiveData in ViewModel
        viewModel.getTask().observe(viewLifecycleOwner) { task ->
            Log.i("TaskDetail", task.toString())

            // Bind the data to Views
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
                        if (isParent == true) {
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
                        if (isParent == true) {
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
                        if (isParent == true) {
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
                        if (isParent == true) {
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

            // Set button OnClickListener depending on task status and user's role
            binding.changeTaskStatusButton.setOnClickListener {
                viewModel.changeTaskStatus(task.id, task.status.toInt(), isParent!!, childId!!, childName!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}