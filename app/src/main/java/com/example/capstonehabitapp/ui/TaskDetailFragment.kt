package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

        // Get task detail data from Firestore
        getTaskDetail(taskId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTaskDetail(taskId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocRef
                .collection("tasks")
                .document(taskId)
                .get()
                .await()

            // Convert the document into Task object
            val task = querySnapshot.toObject<Task>()

            // Bind the data to TextViews
            withContext(Dispatchers.Main) {
                if (task != null) {
                    binding.titleDataText.text = task.title
                    binding.areaDataText.text = task.area
                    binding.timeLimitDataText.text = getString(
                        R.string.task_time_limit_placeholder,
                        task.startTimeLimit,
                        task.finishTimeLimit
                    )
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