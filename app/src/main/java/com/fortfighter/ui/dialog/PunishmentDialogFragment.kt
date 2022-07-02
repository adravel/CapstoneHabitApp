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

class PunishmentDialogFragment: DialogFragment() {

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

        // Accept punishment so the dialog will not be displayed again
        viewModel.acceptPunishment()

        // Observe house LiveData in SharedViewModel
        viewModel.house.observe(viewLifecycleOwner) { response ->
            if (response is Response.Success) {
                val house = response.data
                val houseStaticData = house.getHouseStaticData()!!

                // Display the dialog image and texts depending on House status
                when (house.status.toInt()) {
                    1 -> {
                        Glide.with(this)
                            .load(R.drawable.img_game_fort_intact)
                            .into(binding.image)
                        binding.titleText.text = getString(R.string.punishment_message_1_title)
                        binding.descriptionText.text = getString(R.string.punishment_message_1_description, houseStaticData.name)
                        binding.button.text = getString(R.string.button_label_house_rescue)
                    }
                    2 -> {
                        Glide.with(this)
                            .load(houseStaticData.houseDamagedImageResId)
                            .into(binding.image)
                        binding.titleText.text = getString(R.string.punishment_message_2_title)
                        binding.descriptionText.text = getString(R.string.punishment_message_2_description)
                        binding.button.text = getString(R.string.button_label_house_care)
                    }
                }
            }
        }

        // Set button onCLickListener
        binding.button.setOnClickListener {
            // Pop this fragment
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}