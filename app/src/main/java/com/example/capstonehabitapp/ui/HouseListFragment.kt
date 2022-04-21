package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.HouseAdapter
import com.example.capstonehabitapp.databinding.FragmentHouseListBinding
import com.example.capstonehabitapp.model.House

class HouseListFragment: Fragment() {

    private var _binding: FragmentHouseListBinding? = null
    private val binding get() = _binding!!

    private lateinit var houseAdapter: HouseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHouseListBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.pick_houses)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyList = mutableListOf(
            House("abc", 1, 0, 150, "Rumah Adat 1", "Jawa", getString(R.string.lorem_ipsum), ""),
            House("gr1", 2, 1, 200, "Rumah Adat 2", "Kalimantan", getString(R.string.lorem_ipsum), ""),
            House("vyo", 3, 1, 170, "Rumah Adat 3", "Papua",  getString(R.string.lorem_ipsum), ""),
            House("9fu", 4, 2, 180, "Rumah Adat 4", "Sumatera",  getString(R.string.lorem_ipsum), "")
        )

        // Set the adapter and layoutManager for task list RecyclerView
        houseAdapter = HouseAdapter(dummyList)
        binding.houseListRecyclerView.apply {
            adapter = houseAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}