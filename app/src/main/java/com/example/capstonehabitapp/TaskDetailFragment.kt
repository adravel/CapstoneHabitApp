package com.example.capstonehabitapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.databinding.FragmentTaskDetailBinding
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
    private val db = Firebase.firestore
    private val tasksCollectionRef = db.collection("parents").document(testParentId).collection("tasks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_detail)

        // Initialize task ID using Safe Args provided by navigation component
        val args: TaskDetailFragmentArgs by navArgs()
        taskId = args.taskId

        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get task detail data from Firestore
        // Log.d(TAG, "Fetching task detail for task with ID: $taskId")
        getTaskDetail(taskId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTaskDetail(taskId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = tasksCollectionRef.document(taskId).get().await()

            // Convert the document into Task object
            val task = querySnapshot.toObject<Task>()

            // Bind the data to TextViews
            withContext(Dispatchers.Main) {
                if (task != null) {
                    binding.titleDataText.text = task.title
                    binding.areaDataText.text = task.area
                    val timeLimit = "${task.startTimeLimit} - ${task.finishTimeLimit}"
                    binding.timeLimitDataText.text = timeLimit
                    binding.durationDataText.text = "-"
                    binding.statusDataText.text = task.status.toString()
                    binding.detailDataText.text = task.detail
                    binding.gradePointsDataText.text = task.gradePoints.toString()
                    binding.notesDataText.text = task.notes
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