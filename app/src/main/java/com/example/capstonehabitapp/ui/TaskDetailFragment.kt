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
import com.google.firebase.firestore.FieldValue
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
    private var taskStatus = 0

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

        // Get user's role, child ID, and child name data from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_preference_key), Context.MODE_PRIVATE)
        val isParent = sharedPref?.getBoolean("isParent", true)
        val childId = sharedPref?.getString("selectedChildId", "")
        val childName = sharedPref?.getString("selectedChildName", "")

        // Get task detail data from Firestore
        getTaskDetail(taskId, isParent!!)

        // Set button OnClickListener depending on task status and user's role
        binding.changeTaskStatusButton.setOnClickListener {
            if (isParent) {
                // TODO: Implement on button click function to grade task
                Toast.makeText(requireContext(), "NOT_YET_IMPLEMENTED", Toast.LENGTH_SHORT).show()
            } else {
                when (taskStatus) {
                    0 -> startTask(childId!!, childName!!)
                    1 -> finishTask()
                    2 -> askForGrading()
                }
            }
        }
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

            if (task != null) {
                taskStatus = task.status.toInt()

                // Calculate the duration
                var duration = 0
                if (task.timeStartWorking != null && task.timeFinishWorking != null) {
                    val start = task.timeStartWorking!!
                    val finish = task.timeFinishWorking!!
                    val diffInMinutes = (finish.seconds - start.seconds) / 60
                    duration = diffInMinutes.toInt()
                }

                withContext(Dispatchers.Main) {
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
                        when (taskStatus) {

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
                                durationDataText.text = getString(R.string.task_duration_placeholder, duration)
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
                                durationDataText.text = getString(R.string.task_duration_placeholder, duration)
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
                                durationDataText.text = getString(R.string.task_duration_placeholder, duration)
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
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_SHORT).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    private fun startTask(childId: String, childName: String) {
        val updates = hashMapOf(
            "status" to 1,
            "childId" to childId,
            "childName" to childName,
            "timeStartWorking" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                // Call getTaskDetail method again to update the UI
                getTaskDetail(taskId, false)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Pekerjaan telah dimulai!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Update gagal", Toast.LENGTH_SHORT).show()
                    e.message?.let { Log.e(TAG, it) }
                }
            }
        }
    }

    private fun finishTask() {
        val updates = hashMapOf(
            "status" to 2,
            "timeFinishWorking" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                // Call getTaskDetail method again to update the UI
                getTaskDetail(taskId, false)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Pekerjaan berhasil diselesaikan!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Update gagal", Toast.LENGTH_SHORT).show()
                    e.message?.let { Log.e(TAG, it) }
                }
            }
        }
    }

    private fun askForGrading() {
        val updates = hashMapOf(
            "status" to 3,
            "timeAskForGrading" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                // Call getTaskDetail method again to update the UI
                getTaskDetail(taskId, false)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Permintaan penilaian telah dikirim!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Update gagal", Toast.LENGTH_SHORT).show()
                    e.message?.let { Log.e(TAG, it) }
                }
            }
        }
    }
}