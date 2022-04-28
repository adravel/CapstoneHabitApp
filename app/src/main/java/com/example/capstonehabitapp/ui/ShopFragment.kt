package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ToolAdapter
import com.example.capstonehabitapp.databinding.FragmentShopBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.ShopViewModel

class ShopFragment: Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolAdapter: ToolAdapter

    private lateinit var childId: String
    private lateinit var childName: String

    private val viewModel: ShopViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentShopBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.pick_tools)

        // Initialize tool name and child name using Safe Args provided by navigation component
        val args: ShopFragmentArgs by navArgs()
        childId = args.childId
        childName = args.childName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for tool list RecyclerView
        toolAdapter = ToolAdapter(mutableListOf(), true, childId, childName)
        binding.toolListRecyclerView.apply {
            adapter = toolAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        // Fetch tools data from Firestore
        viewModel.getToolsFromFirebase(childId)

        // Observe tools LiveData in ViewModel
        viewModel.tools.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tools = response.data
                    toolAdapter.updateList(tools)
                }
                is Response.Failure -> {
                    Log.e("Shop", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe toolName LiveData in ViewModel
        // This value determines whether tool sale query is successful or not
        viewModel.toolName.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.toolNameResponseHandled()

                    val toolName = response.data

                    // Fetch tools data again to update the sale status
                    viewModel.getToolsFromFirebase(childId)

                    // Show tool sale success dialog
                    val action = ShopFragmentDirections.actionShopFragmentToToolSaleSuccessDialogFragment(toolName)
                    findNavController().navigate(action)
                }
                is Response.Failure -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.toolNameResponseHandled()

                    Log.e("Shop", response.message)
                    Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                }
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