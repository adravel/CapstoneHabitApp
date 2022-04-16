package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ToolAdapter
import com.example.capstonehabitapp.databinding.FragmentShopBinding
import com.example.capstonehabitapp.model.Tool

class ShopFragment: Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolAdapter: ToolAdapter

    private lateinit var childId: String
    private lateinit var childName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentShopBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.pick_tools)

        // Initialize tool name and child name using Safe Args provided by navigation component
        val args: ShopFragmentArgs by navArgs()
        childId = args.childId
        childName = args.childName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyList = mutableListOf(
            Tool("1", "Meriam 1", crushingTool = true, sold = false, 7, 15, ""),
            Tool("2", "Meriam 2", crushingTool = true, sold = false, 10, 20, ""),
            Tool("3", "Meriam 3", crushingTool = true, sold = false, 20, 35, ""),
            Tool("4", "Meriam 4", crushingTool = true, sold = false, 30, 40, ""),
            Tool("5", "Sapu", crushingTool = false, sold = false, 15, 25, ""),
            Tool("6", "Palu", crushingTool = false, sold = false, 12, 20, "")
        )

        // Set the adapter and layoutManager for task list RecyclerView
        toolAdapter = ToolAdapter(dummyList, childId, childName)
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