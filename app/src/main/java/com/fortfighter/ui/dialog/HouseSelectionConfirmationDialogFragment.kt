package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fortfighter.R
import com.fortfighter.databinding.DialogTwoButtonsBinding

class HouseSelectionConfirmationDialogFragment: DialogFragment() {
    private var _binding: DialogTwoButtonsBinding? = null
    private val binding get() = _binding!!

    private lateinit var houseId: String
    private lateinit var houseName: String
    private var houseStatus: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogTwoButtonsBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize house data using Safe Args provided by navigation component
        val args: HouseSelectionConfirmationDialogFragmentArgs by navArgs()
        houseId = args.houseId
        houseName = args.houseName
        houseStatus = args.houseStatus

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(
            when (houseStatus!!) {
                1 -> R.string.house_selection_confirmation_message_1
                2 -> R.string.house_selection_confirmation_message_2
                else -> R.string.house_selection_confirmation_message_3
            },
            houseName
        )
        binding.positiveButton.text = getString(
            when (houseStatus!!) {
                1 -> R.string.button_label_rescue
                2 -> R.string.button_label_take_care
                else -> R.string.button_label_see
            },
        )
        binding.negativeButton.text = getString(R.string.button_label_no)

        // Set button onClickListener for rescuing the house
        binding.positiveButton.setOnClickListener {
            // Navigate to house detail page
            val action = HouseSelectionConfirmationDialogFragmentDirections
                .actionHouseSelectionConfirmationDialogFragmentToHouseDetailFragment(
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