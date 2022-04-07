package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.TaskAdapter
import com.example.capstonehabitapp.databinding.FragmentTaskListBinding
import com.example.capstonehabitapp.viewmodel.TaskListViewModel

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private lateinit var viewModel: TaskListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.task_list)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[TaskListViewModel::class.java]

        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check the user's role from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref?.getBoolean(getString(R.string.role_pref_is_parent_key), true)

        // Set the adapter and layoutManager for task list RecyclerView
        taskAdapter = TaskAdapter(mutableListOf(), isParent!!)
        binding.taskListRecycleView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch tasks data from Firestore
        viewModel.getTasksFromFirebase()

        // Observe tasks LiveData in ViewModel
        viewModel.getTasks().observe(viewLifecycleOwner) { task ->
            taskAdapter.updateTaskList(task)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}