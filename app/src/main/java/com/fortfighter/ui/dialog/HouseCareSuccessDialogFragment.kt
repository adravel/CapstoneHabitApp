package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.databinding.DialogDetailBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HouseDetailViewModel

class HouseCareSuccessDialogFragment: DialogFragment() {

    private var _binding: DialogDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HouseDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogDetailBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Prevent the dialog from being dismissed
        // when user click outside the dialog
        dialog!!.setCanceledOnTouchOutside(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get house data from SharedViewModel
        val response = viewModel.house.value
        if (response is Response.Success) {
            val house = response.data
            val houseStaticData = house.getHouseStaticData()!!

            // Display the dialog image and texts depending on House type
            Glide.with(this)
                .load(houseStaticData.houseIntactImageResId)
                .into(binding.image)
            binding.titleText.text = getString(R.string.house_care_success_message_title, houseStaticData.name)
            binding.descriptionText.text = getString(R.string.house_care_success_message_description)
        }
        binding.button.text = getString(R.string.button_label_house_select)

        // Set button onCLickListener
        binding.button.setOnClickListener {
            // Pop into house list page
            findNavController().popBackStack(R.id.houseListFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}