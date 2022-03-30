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
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.databinding.FragmentTaskDetailBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class TaskDetailFragment : Fragment() {

    private val TAG = "TaskDetailFragment"

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskId: String

    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private lateinit var parentDocRef: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_detail)

        // Initialize Firebase reference
        parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

        // Initialize task ID using Safe Args provided by navigation component
        val args: TaskDetailFragmentArgs by navArgs()
        taskId = args.taskId

        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check the user's role from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_preference_key), Context.MODE_PRIVATE)
        val isParent = sharedPref?.getBoolean("isParent", true)

        // Get task detail data from Firestore
        getTaskDetail(taskId, isParent!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTaskDetail(taskId: String, isForParent: Boolean) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocRef
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            // Convert the document into Task object
            val task = querySnapshot.toObject<Task>()

            withContext(Dispatchers.Main) {
                if (task != null) {
                    val status = task.status.toInt()

                    // Display task details
                    binding.apply {

                        // Display data that is unaffected by status
                        titleDataText.text = task.title
                        areaDataText.text = task.area
                        difficultyDataText.text = when (task.difficulty.toInt()) {
                            0 -> getString(R.string.task_difficulty_0)
                            1 -> getString(R.string.task_difficulty_1)
                            2 -> getString(R.string.task_difficulty_2)
                            else -> ""
                        }
                        // TODO: Implement task repetition data display
                        repetitionDataText.text = "NOT_YET_IMPLEMENTED"
                        timeLimitDataText.text = getString(
                            R.string.task_time_limit_placeholder,
                            task.startTimeLimit,
                            task.finishTimeLimit
                        )
                        detailDataText.text = task.detail

                        // Display the rest of the data according to status
                        when (status) {

                            // State: Task default state
                            0 -> {
                                durationDataText.text = "-"
                                statusDataText.text = getString(R.string.task_status_0)
                                statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                                if (isForParent) {
                                    gradePointsDataText.text = "-"
                                    notesDataText.text = "-"
                                    changeTaskStatusButton.visibility = View.GONE
                                }
                                else {
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
                                }
                                else {
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
                                // TODO: Implement task duration data display
                                durationDataText.text = "NOT_YET_IMPLEMENTED"
                                statusDataText.text = getString(R.string.task_status_2_with_child_name, task.childName)
                                statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_success))
                                if (isForParent) {
                                    gradePointsDataText.text = "-"
                                    notesDataText.text = "-"
                                    changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                                    changeTaskStatusButton.isEnabled = false
                                }
                                else {
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
                                // TODO: Implement task duration data display
                                durationDataText.text = "NOT_YET_IMPLEMENTED"
                                if (isForParent) {
                                    statusDataText.text = getString(R.string.task_status_3_for_parent_role_with_child_name, task.childName)
                                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_error))
                                    changeTaskStatusButton.text = getString(R.string.button_label_grade_task)
                                }
                                else {
                                    statusDataText.text = getString(R.string.task_status_3_for_child_role)
                                    statusDataText.setTextColor(ContextCompat.getColor(requireContext(), R.color.state_info))
                                    changeTaskStatusButton.visibility = View.GONE
                                }
                                gradePointsDataText.text = "-"
                                notesDataText.text = "-"
                            }

                            // State: Task has been graded
                            4 -> {
                                // TODO: Implement task duration data display
                                durationDataText.text = "NOT_YET_IMPLEMENTED"
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

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}