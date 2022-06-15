package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.FinishedTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentHistoryBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.HistoryViewModel

class HistoryFragment: Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var finishedTaskAdapter: FinishedTaskAdapter

    private val viewModel: HistoryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.history)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for finished task list RecyclerView
        // and show only the latest 3 finished tasks
        finishedTaskAdapter = FinishedTaskAdapter(mutableListOf(), false)
        binding.finishedTaskListRecyclerView.apply {
            adapter = finishedTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // TODO: For Parent, retrieve child ID from toolbar dropdown menu selection
        // Retrieve child ID from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), false)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // Fetch the data from Firestore
        viewModel.getFinishedTasksFromFirebase(childId)
        viewModel.getChildFromFirebase(childId)

        // Observe finishedTasks LiveData in ActivityViewModel
        viewModel.finishedTasks.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tasks = response.data

                    // Update the RecyclerView
                    finishedTaskAdapter.updateList(tasks)
                }
                is Response.Failure -> {
                    Log.e("History", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe weeklyTaskCounts LiveData in ViewModel
        viewModel.weeklyTaskCounts.observe(viewLifecycleOwner) {
            binding.apply {
                // Display current month task count
                val currentMonthTaskCount = it.sum()
                monthSummaryDescriptionText.text = getString(
                    R.string.history_description_placeholder,
                    currentMonthTaskCount
                )

                // Display weekly task counts
                week1SummaryCardNumber.text = it[0].toString()
                week2SummaryCardNumber.text = it[1].toString()
                week3SummaryCardNumber.text = it[2].toString()
                week4SummaryCardNumber.text = it[3].toString()
            }
        }

        // Observe child LiveData in ViewModel
        viewModel.child.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val child = response.data

                    // Display child avatar image
                    if (child.isMale) {
                        binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_male)
                    } else {
                        binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_female)
                    }

                    // Display congratulation text
                    binding.monthSummaryCongratsText.text = if (isParent) {
                        getString(R.string.history_congrats_placeholder, child.name)
                    } else {
                        getString(R.string.history_congrats)
                    }
                }
                is Response.Failure -> {
                    Log.e("History", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Set see all finished task list button onClickListener
        binding.seeAllButton.setOnClickListener {
            findNavController().navigate(R.id.finishedTaskListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}