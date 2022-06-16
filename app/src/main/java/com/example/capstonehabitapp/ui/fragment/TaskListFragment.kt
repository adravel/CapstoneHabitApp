package com.example.capstonehabitapp.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.TaskAdapter
import com.example.capstonehabitapp.databinding.FragmentTaskListBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.TaskListViewModel

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: TaskAdapter

    private val viewModel: TaskListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set listener so this fragment will be able to know if a task document has been deleted
        setFragmentResultListener("taskDelete") { _, bundle ->
            // Check whether a task document has been deleted
            val isTaskDeleted = bundle.getBoolean("isTaskDeleted")

            // Display task delete success dialog if the flag is set to true
            if (isTaskDeleted) {
                findNavController().navigate(R.id.taskDeleteSuccessDialogFragment)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.task_list)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check the user's role from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)

        // Set the adapter and layoutManager for task list RecyclerView
        taskAdapter = TaskAdapter(mutableListOf(), isParent)
        binding.taskListRecycleView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch tasks data from Firestore
        viewModel.getTasksFromFirebase()

        // Observe tasks LiveData in ViewModel
        viewModel.tasks.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val task = response.data

                    // Update the RecyclerView
                    taskAdapter.updateList(task)
                }
                is Response.Failure -> {
                    Log.e("TaskList", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Display FAB and tasks count depending on the user's role
        if (isParent) {
            // Set FAB OnClickListener
            binding.fab.setOnClickListener {
                // Navigate to TaskCreationTemplateCategoryFragment
                findNavController().navigate(R.id.taskCreationTemplateCategoryFragment)
            }

            // Hide the tasks count cards
            binding.onGoingTaskCountCard.visibility = View.GONE
            binding.finishedTaskCountCard.visibility = View.GONE
            binding.failedTaskCountCard.visibility = View.GONE
        } else {
            // Hide the FAB if user is Child
            binding.fab.visibility = View.GONE

            // Observe tasksCount LiveData in ViewModel
            // to know how many tasks are on going, finished, or failed to be completed
            viewModel.tasksCount.observe(viewLifecycleOwner) {
                val (onGoingTaskCount, finishedTaskCount, failedTaskCount) = it

                binding.onGoingTaskCountDataText.text = onGoingTaskCount.toString()
                binding.finishedTaskCountDataText.text = finishedTaskCount.toString()
                binding.failedTaskCountDataText.text = failedTaskCount.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}