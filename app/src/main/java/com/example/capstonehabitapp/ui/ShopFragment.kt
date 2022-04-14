package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ToolAdapter
import com.example.capstonehabitapp.databinding.FragmentShopBinding
import com.example.capstonehabitapp.model.Tool

class ShopFragment: Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolAdapter: ToolAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentShopBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.pick_tools)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummy = mutableListOf(
            Tool("Meriam 1", crushingTool = true, sold = true, 7, 15, ""),
            Tool("Meriam 2", crushingTool = true, sold = false, 10, 20, ""),
            Tool("Meriam 3", crushingTool = true, sold = false, 20, 35, ""),
            Tool("Meriam 4", crushingTool = true, sold = false, 30, 40, ""),
            Tool("Meriam 1", crushingTool = true, sold = true, 7, 15, ""),
            Tool("Meriam 2", crushingTool = true, sold = false, 10, 20, ""),
            Tool("Meriam 3", crushingTool = true, sold = false, 20, 35, ""),
            Tool("Meriam 4", crushingTool = true, sold = false, 30, 40, "")
        )

        // Set the adapter and layoutManager for task list RecyclerView
        toolAdapter = ToolAdapter(dummy)
        binding.toolListRecyclerView.apply {
            adapter = toolAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}