package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskDetailBinding
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskId: String
    private lateinit var childId: String
    private lateinit var childName: String

    private val hide = View.GONE
    private val show = View.VISIBLE

    private val viewModel: TaskDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.task_detail)

        // Initialize task ID using Safe Args provided by navigation component
        val args: TaskDetailFragmentArgs by navArgs()
        taskId = args.taskId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user's role, child ID, and child name data from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)
        if (!isParent) {
            childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!
            childName = sharedPref.getString(getString(R.string.role_pref_child_name_key), "")!!
        }

        // Fetch task detail data from Firestore
        viewModel.getTaskFromFirebase(taskId)

        // Observe task LiveData in SharedViewModel
        viewModel.task.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> displayEmptyTask()
                is Response.Success -> {
                    val task = response.data

                    // Display task data correctly depending on status and user's role
                    displayTaskData(task, isParent)

                    // Set button OnClickListener depending on the task status and user's role
                    binding.changeTaskStatusButton.setOnClickListener {
                        if (isParent) {
                            //Navigate to grading form page
                            findNavController().navigate(R.id.gradingFormFragment)
                        } else {
                            when (task.status.toInt()) {
                                0 -> viewModel.startTask(taskId, childId, childName)
                                1 -> viewModel.finishTask(taskId)
                                2 -> findNavController().navigate(R.id.gradingMethodSelectionDialogFragment)
                            }
                        }
                    }
                }
                is Response.Failure -> {
                    Log.e("TaskDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe task status change LiveData in ViewModel
        viewModel.taskStatusChange.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.taskStatusChangeResponseHandled()

                    // Fetch task data to update the Views
                    viewModel.getTaskFromFirebase(taskId)

                    // Display a toast when task status changes
                    // i.e. when the button is clicked and the function is executed successfully
                    when (response.data) {
                        1 -> Toast.makeText(context, getString(R.string.task_start_success), Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(context, getString(R.string.task_finish_success), Toast.LENGTH_SHORT).show()
                        3 -> Toast.makeText(context, getString(R.string.ask_for_grading_success), Toast.LENGTH_SHORT).show()
                        4 -> findNavController().navigate(R.id.gradingSuccessDialogFragment)
                    }
                }
                is Response.Failure -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.taskStatusChangeResponseHandled()

                    Log.e("TaskDetail", response.message)
                    Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                }
            }
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

    // Display empty task for when data is still being loaded
    private fun displayEmptyTask() {
        binding.apply {
            titleDataText.text = ""
            areaDataText.text = ""
            difficultyDataText.text = ""
            repetitionDataText.text = ""
            timeLimitDataText.text = ""
            statusDataText.text = ""
            detailDataText.text = ""

            gradePointsText.visibility = hide
            gradePointsDataText.visibility = hide
            notesText.visibility = hide
            notesDataText.visibility = hide
            changeTaskStatusButton.visibility = hide
        }
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

                        changeTaskStatusButton.visibility = hide
                    } else {
                        gradePointsText.visibility = hide
                        gradePointsDataText.visibility = hide
                        notesText.visibility = hide
                        notesDataText.visibility = hide

                        changeTaskStatusButton.visibility = show
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

                        changeTaskStatusButton.visibility = hide
                    } else {
                        gradePointsText.visibility = hide
                        gradePointsDataText.visibility = hide
                        notesText.visibility = hide
                        notesDataText.visibility = hide

                        changeTaskStatusButton.visibility = if (task.childId == childId) show else hide
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

                        changeTaskStatusButton.visibility = show
                        changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                        changeTaskStatusButton.isEnabled = false
                    } else {
                        gradePointsText.visibility = hide
                        gradePointsDataText.visibility = hide
                        notesText.visibility = hide
                        notesDataText.visibility = hide

                        changeTaskStatusButton.visibility = if (task.childId == childId) show else hide
                        changeTaskStatusButton.text = getString(R.string.button_label_ask_for_grading)
                    }
                }

                // State: Waiting for grading
                3 -> {
                    durationDataText.text = viewModel.getTaskDurationString(task)
                    if (isForParent) {
                        statusDataText.text = getString(R.string.task_status_3_for_parent_with_child_name, task.childName)
                        statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))

                        changeTaskStatusButton.visibility = show
                        changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                        changeTaskStatusButton.isEnabled = true
                    } else {
                        statusDataText.text = getString(R.string.task_status_3_for_child)
                        statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_info))

                        changeTaskStatusButton.visibility = hide
                    }
                    gradePointsText.visibility = show
                    gradePointsDataText.visibility = show
                    notesText.visibility = show
                    notesDataText.visibility = show

                    gradePointsDataText.text = "-"
                    notesDataText.text = "-"
                }

                // State: Task has been graded
                4 -> {
                    durationDataText.text = viewModel.getTaskDurationString(task)
                    statusDataText.text = getString(R.string.task_status_4)
                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))

                    gradePointsText.visibility = show
                    gradePointsDataText.visibility = show
                    notesText.visibility = show
                    notesDataText.visibility = show

                    gradePointsDataText.text = when (task.grade.toInt()) {
                        1 -> getString(R.string.task_grade_1)
                        2 -> getString(R.string.task_grade_2)
                        3 -> getString(R.string.task_grade_3)
                        else -> "-"
                    }
                    notesDataText.text = task.notes

                    changeTaskStatusButton.visibility = hide
                }
            }
        }
    }
}