package com.example.capstonehabitapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentParentHomeBinding
import com.example.capstonehabitapp.viewmodel.ParentHomeViewModel

class ParentHomeFragment: Fragment() {

    private var _binding: FragmentParentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var essentialTaskAdapter: EssentialTaskAdapter

    private val viewModel: ParentHomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentParentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for essential task list RecyclerView
        essentialTaskAdapter = EssentialTaskAdapter(mutableListOf(), true)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch parent and essential task data from Firestore
        viewModel.getParentFromFirebase()
        viewModel.getEssentialTasksFromFirebase()

        // Observe parent name LiveData in ViewModel
        viewModel.getParentName().observe(viewLifecycleOwner) { parentName ->
            binding.parentNameText.text = "$parentName !"
        }

        // Observe essential tasks LiveData in ViewModel
        viewModel.getEssentialTasks().observe(viewLifecycleOwner) { tasks ->
            essentialTaskAdapter.updateEssentialTasksList(tasks)
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