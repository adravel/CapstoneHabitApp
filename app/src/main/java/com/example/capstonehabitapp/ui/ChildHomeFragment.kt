package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentChildHomeBinding
import com.example.capstonehabitapp.viewmodel.ChildHomeViewModel

class ChildHomeFragment: Fragment() {

    private var _binding: FragmentChildHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var essentialTaskAdapter: EssentialTaskAdapter

    private val viewModel: ChildHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChildHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for essential task list RecyclerView
        essentialTaskAdapter = EssentialTaskAdapter(mutableListOf(), false)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Retrieve child ID from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref?.getString(getString(R.string.role_pref_child_id_key), "")

        // Fetch child and essential task data from Firestore
        if (childId != null && childId != "") {
            viewModel.getChildFromFirebase(childId)
            viewModel.getEssentialTasksFromFirebase(childId)
        }

        // Observe essential tasks LiveData in ViewModel
        viewModel.getEssentialTasks().observe(viewLifecycleOwner) { tasks ->
            essentialTaskAdapter.updateEssentialTasksList(tasks)
        }

        // Observe child LiveData in ViewModel
        viewModel.getChild().observe(viewLifecycleOwner) { child ->
            val levelName = when (child.level.toInt()) {
                1 -> getString(R.string.level_1_name)
                2 -> getString(R.string.level_2_name)
                3 -> getString(R.string.level_3_name)
                4 -> getString(R.string.level_4_name)
                5 -> getString(R.string.level_5_name)
                6 -> getString(R.string.level_6_name)
                7 -> getString(R.string.level_7_name)
                8 -> getString(R.string.level_8_name)
                9 -> getString(R.string.level_9_name)
                else -> getString(R.string.level_10_name)
            }
            val pointsToLevelUp = viewModel.getPointsToLevelUp(child.level.toInt())

            // Bind the data to Views
            binding.apply {
                greetingsText.text = getString(R.string.child_greetings_placeholder, child.name)
                levelText.text = getString(R.string.child_level_placeholder, levelName)
                expText.text = getString(R.string.child_exp_placeholder, child.totalPoints.toInt(), pointsToLevelUp)
                expProgressBar.max = pointsToLevelUp
                expProgressBar.progress = child.totalPoints.toInt()
            }
        }

        binding.taskMenuCard.setOnClickListener {
            it.findNavController().navigate(R.id.taskListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}