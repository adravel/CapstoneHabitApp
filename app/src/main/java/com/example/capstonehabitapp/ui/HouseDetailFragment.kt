package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentHouseDetailBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.HouseDetailViewModel

class HouseDetailFragment: Fragment() {

    private var _binding: FragmentHouseDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var houseId: String
    private lateinit var houseName: String

    private val viewModel: HouseDetailViewModel by viewModels()

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
        activity?.title = houseName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve child ID from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref?.getString(getString(R.string.role_pref_child_id_key), "")

        // Fetch house detail data from Firestore
        if (childId != null && childId != "") {
            viewModel.getHouseFromFirebase(childId, houseId)
        }

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}