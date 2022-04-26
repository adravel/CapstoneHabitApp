package com.example.capstonehabitapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentParentHomeBinding
import com.example.capstonehabitapp.util.Response
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
        // Inflate layout for this fragment
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
        viewModel.getEssentialTasksForParentFromFirebase()

        // Observe parent name LiveData in ViewModel
        viewModel.parentName.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val parentName = response.data
                    binding.parentNameText.text = "$parentName !"
                }
                is Response.Failure -> {
                    Log.e("ParentHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe essential tasks LiveData in ViewModel
        viewModel.essentialTasks.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tasks = response.data
                    essentialTaskAdapter.updateList(tasks)
                }
                is Response.Failure -> {
                    Log.e("ParentHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply {
            // Set change role button onClickListener
            toolbarLayout.changeRoleButton.setOnClickListener {
                view.findNavController().navigate(R.id.roleSelectionFragment)
            }

            // Set task menu card onClickListener
            taskMenuCard.setOnClickListener {
                it.findNavController().navigate(R.id.taskListFragment)
            }

            // Set shop menu card onClickListener
            shopMenuCard.setOnClickListener {
                it.findNavController().navigate(R.id.childAccountSelectionFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}