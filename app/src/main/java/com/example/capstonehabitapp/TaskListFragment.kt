package com.example.capstonehabitapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.databinding.FragmentTaskListBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val TAG = "TaskListFragment"

    private lateinit var taskList: MutableList<Task>
    private lateinit var taskAdapter: TaskAdapter

    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val db = Firebase.firestore
    private val tasksCollectionRef = db.collection("parents").document(testParentId).collection("tasks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_list)

        taskList = mutableListOf()

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for task list RecyclerView
        taskAdapter = TaskAdapter(taskList)
        binding.recycleView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Get all task data from Firestore
        getTasks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTasks() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = tasksCollectionRef.get().await()
            for(document in querySnapshot.documents) {
                document.toObject<Task>()?.let { taskList.add(it) }
            }
            withContext(Dispatchers.Main) {
                taskAdapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Daftar pekerjaan telah diperbarui", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}