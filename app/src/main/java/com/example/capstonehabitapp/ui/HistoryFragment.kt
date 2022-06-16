package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.FinishedTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentHistoryBinding
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.HistoryViewModel

class HistoryFragment: Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var finishedTaskAdapter: FinishedTaskAdapter

    private lateinit var childId: String

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

        // Retrieve user's role from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), false)

        // Load data according to user's role
        if (isParent) {
            // Fetch children data from Firestore
            viewModel.getChildrenFromFirebase()

            // Observe children LiveData in ViewModel
            viewModel.children.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        val children = response.data

                        // Display the selected child data
                        // If the user has not selected any child,
                        // display the oldest child account data
                        // as the selectedChildIndex default value is 0
                        val selectedChildIndex = viewModel.selectedChildIndex
                        children[selectedChildIndex].let { selectedChild ->
                            // Fetch finished tasks data from Firestore
                            viewModel.getFinishedTasksFromFirebase(selectedChild.id)

                            // Display child avatar image and congratulation text data
                            displayMonthSummaryCardChildData(selectedChild, isParent)
                        }

                        // Instantiate a PopupMenu for displaying a list of child name
                        val childrenPopupMenu = PopupMenu(
                            requireContext(),
                            binding.toolbarLayout.chooseChildButton
                        )

                        // Populate childrenPopupMenu item with children data from Firestore
                        for ((index, child) in children.withIndex()) {
                            // Add method takes group ID, item ID, item position, and item title as inputs
                            childrenPopupMenu.menu.add(Menu.NONE, index, index, child.name)
                        }

                        // Set choose child button onClickListener to display childrenPopupMenu
                        binding.toolbarLayout.chooseChildButton.setOnClickListener {
                            childrenPopupMenu.show()
                        }

                        // Set childrenPopupMenu item onClickListener
                        childrenPopupMenu.setOnMenuItemClickListener { item ->
                            val selectedChild = children[item.itemId]

                            // Store selected child index in ViewModel
                            viewModel.selectedChildIndex = item.itemId

                            // Fetch finished tasks data from Firestore
                            viewModel.getFinishedTasksFromFirebase(selectedChild.id)

                            // Display child avatar image and congratulation text data
                            displayMonthSummaryCardChildData(selectedChild, isParent)

                            // Return true to indicate that a menu item is successfully selected
                            true
                        }
                    }
                    is Response.Failure -> {
                        Log.e("History", response.message)
                        Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            // Hide choose child button
            binding.toolbarLayout.chooseChildButton.visibility = View.GONE

            // Get child ID from shared preference
            childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

            // Fetch the finished tasks and child data from Firestore for this child ID
            viewModel.getFinishedTasksFromFirebase(childId)
            viewModel.getChildFromFirebase(childId)

            // Observe child LiveData in ViewModel
            viewModel.child.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {}
                    is Response.Success -> {
                        val child = response.data

                        // Display child avatar image and congratulation text data
                        displayMonthSummaryCardChildData(child, false)
                    }
                    is Response.Failure -> {
                        Log.e("History", response.message)
                        Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

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

    // Method for displaying month summary card child avatar image and congratulation text data
    private fun displayMonthSummaryCardChildData(child: Child, isForParent: Boolean) {
        // Display child avatar image
        if (child.isMale) {
            binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_male)
        } else {
            binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_female)
        }

        // Display congratulation text
        if (isForParent) {
            binding.monthSummaryCongratsText.text = getString(
                R.string.history_congrats_placeholder,
                child.name
            )
        } else {
            binding.monthSummaryCongratsText.text = getString(R.string.history_congrats)
        }
    }
}