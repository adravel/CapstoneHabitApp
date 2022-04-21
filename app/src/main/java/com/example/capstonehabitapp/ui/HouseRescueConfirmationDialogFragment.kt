package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTwoButtonsDialogBinding

class HouseRescueConfirmationDialogFragment: DialogFragment() {
    private var _binding: FragmentTwoButtonsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var houseId: String
    private lateinit var houseName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTwoButtonsDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize house data using Safe Args provided by navigation component
        val args: HouseRescueConfirmationDialogFragmentArgs by navArgs()
        houseId = args.houseId
        houseName = args.houseName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.house_rescue_confirmation_message, houseName)
        binding.positiveButton.text = getString(R.string.button_label_rescue)
        binding.negativeButton.text = getString(R.string.button_label_no)

        // Set button onClickListener for rescuing the house
        binding.positiveButton.setOnClickListener {
            // Navigate to house detail page
            val action = HouseRescueConfirmationDialogFragmentDirections
                .actionHouseRescueConfirmationDialogFragmentToHouseDetailFragment(
                    houseId,
                    houseName
                )
            findNavController().navigate(action)
        }

        // Set button onClickListener for canceling action
        binding.negativeButton.setOnClickListener {
            // Dismiss this dialog
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}