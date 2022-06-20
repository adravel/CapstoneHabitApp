package com.fortfighter.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.fortfighter.R
import com.fortfighter.adapter.HouseAdapter
import com.fortfighter.databinding.FragmentHouseListBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HouseListViewModel

class HouseListFragment: Fragment() {

    private var _binding: FragmentHouseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var houseAdapter: HouseAdapter

    private val viewModel: HouseListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHouseListBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.pick_houses)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for task list RecyclerView
        houseAdapter = HouseAdapter(mutableListOf())
        binding.houseListRecyclerView.apply {
            adapter = houseAdapter
            layoutManager = GridLayoutManager(context, 2)
        }

        // Retrieve child ID from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // Fetch houses data from Firestore
        viewModel.getHousesFromFirebase(childId)

        // Observe houses LiveData in ViewModel
        viewModel.houses.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val houses = response.data
                    houseAdapter.updateList(houses)
                }
                is Response.Failure -> {
                    Log.e("HouseList", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
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