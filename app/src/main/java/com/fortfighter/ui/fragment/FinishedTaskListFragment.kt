package com.fortfighter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fortfighter.R
import com.fortfighter.adapter.FinishedTaskAdapter
import com.fortfighter.databinding.FragmentFinishedTaskListBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HistoryViewModel

class FinishedTaskListFragment: Fragment() {

    private var _binding: FragmentFinishedTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var finishedTaskAdapter: FinishedTaskAdapter

    private val viewModel: HistoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentFinishedTaskListBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.finished_tasks)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for finished task list RecyclerView
        finishedTaskAdapter = FinishedTaskAdapter(mutableListOf(), true)
        binding.finishedTaskListRecyclerView.apply {
            adapter = finishedTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Observe finishedTasks LiveData in ActivityViewModel
        viewModel.finishedTasks.observe(viewLifecycleOwner) { response ->
            if (response is Response.Success) {
                val tasks = response.data

                // Update the RecyclerView
                finishedTaskAdapter.updateList(tasks)
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
}