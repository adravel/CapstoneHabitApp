package com.fortfighter.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.FragmentHouseInfoBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HouseDetailViewModel

class HouseInfoFragment: Fragment() {

    private var _binding: FragmentHouseInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HouseDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHouseInfoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get house data from SharedViewModel
        val response = viewModel.house.value
        if (response is Response.Success) {
            val house = response.data
            val houseStaticData = house.getHouseStaticData()!!

            // Set toolbar title
            binding.toolbarLayout.toolbar.title = getString(
                R.string.house_info_toolbar_title_placeholder,
                houseStaticData.name
            )

            // Display the image and texts depending on House type
            Glide.with(this).load(houseStaticData.infoImageResId).into(binding.image)
            binding.nameText.text = houseStaticData.name
            binding.descriptionText.text = houseStaticData.descriptionLong
        }


        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}