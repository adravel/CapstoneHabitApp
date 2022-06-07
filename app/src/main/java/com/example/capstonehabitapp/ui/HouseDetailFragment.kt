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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ToolAdapter
import com.example.capstonehabitapp.databinding.FragmentHouseDetailBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.HouseDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior

class HouseDetailFragment: Fragment() {

    private var _binding: FragmentHouseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolAdapter: ToolAdapter

    private lateinit var houseId: String
    private lateinit var houseName: String

    private lateinit var shopBottomSheetBehavior: BottomSheetBehavior<*>

    private val viewModel: HouseDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHouseDetailBinding.inflate(inflater, container, false)

        // Initialize house data using Safe Args provided by navigation component
        val args: HouseRescueConfirmationDialogFragmentArgs by navArgs()
        houseId = args.houseId
        houseName = args.houseName

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = houseName

        // Initialize bottom sheet
        shopBottomSheetBehavior = BottomSheetBehavior.from(binding.shopBottomSheetCard)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve child ID from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // Set child and house document IDs
        viewModel.setChildId(childId)
        viewModel.setHouseId(houseId)

        // Set the adapter and layoutManager for tool list RecyclerView
        toolAdapter = ToolAdapter(mutableListOf(), false, childId)
        binding.toolListRecyclerView.apply {
            adapter = toolAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        // Fetch house detail, tool list, and child cash data from Firestore
        viewModel.getHouseFromFirebase()
        viewModel.getToolsForSaleFromFirebase()
        viewModel.getChildCashFromFirestore()

        // Observe house LiveData in ViewModel
        viewModel.house.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val house = response.data

                    binding.apply {
                        houseNameText.text = house.name
                        houseHpText.text = getString(R.string.house_hp_placeholder, house.hp, house.maxHp)
                        houseHpProgressBar.max = house.maxHp.toInt()
                        houseHpProgressBar.progress = house.hp.toInt()
                    }
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe tools LiveData in ViewModel
        viewModel.tools.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tools = response.data
                    toolAdapter.updateList(tools)
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe child cash LiveData in ViewModel
        viewModel.childCash.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val cash = response.data
                    binding.cashText.text = getString(R.string.child_cash_placeholder, cash)
                }
                is Response.Failure -> {
                    Log.e("HouseDetail", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe toolPurchaseResponse LiveData in ViewModel
        // to determine whether tool purchase transaction is successful or not
        viewModel.toolPurchaseResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    viewModel.toolPurchaseResultResponseHandled()

                    Toast.makeText(context, getString(R.string.tool_purchase_success), Toast.LENGTH_SHORT).show()

                    // Fetch the data to update the Views
                    viewModel.getHouseFromFirebase()
                    viewModel.getChildCashFromFirestore()
                }
                is Response.Failure -> {
                    // Show error message as a toast
                    if (response.message == "cash") {
                        Toast.makeText(context, getString(R.string.not_enough_cash_failure), Toast.LENGTH_LONG).show()
                    } else {
                        Log.e("HouseDetail", response.message)
                        Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()                    }
                }
            }
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // Set bottom sheet behavior to flip arrow icon when bottom sheet is expanded
        shopBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> binding.arrowIconImage.scaleY = -1F
                    BottomSheetBehavior.STATE_COLLAPSED -> binding.arrowIconImage.scaleY = 1F
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Set arrow icon onClickListener to expand/collapse the bottom sheet
        binding.arrowIconImage.setOnClickListener {
            if (shopBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                shopBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                shopBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}