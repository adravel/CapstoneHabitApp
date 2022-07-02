package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fortfighter.R
import com.fortfighter.databinding.DialogTwoButtonsBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.HouseDetailViewModel

class ToolPurchaseConfirmationDialogFragment: DialogFragment() {

    private var _binding: DialogTwoButtonsBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolId: String
    private lateinit var toolName: String
    private var toolType: Int? = null

    private val viewModel: HouseDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogTwoButtonsBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize tool and child data using Safe Args provided by navigation component
        val args: ToolPurchaseConfirmationDialogFragmentArgs by navArgs()
        toolId = args.toolId
        toolName = args.toolName
        toolType = args.toolType

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get house data from SharedViewModel
        val response = viewModel.house.value
        if (response is Response.Success) {
            val house = response.data

            // Display the dialog message text depending on House type
            when (house.status.toInt()) {
                1 -> binding.messageText.text = getString(R.string.tool_purchase_confirmation_message_1, toolName)
                2 -> binding.messageText.text = getString(
                    if (toolType == 2) {
                        R.string.tool_purchase_confirmation_message_2_clean
                    } else {
                        R.string.tool_purchase_confirmation_message_2_repair
                    },
                    toolName
                )
            }
        }

        // Display dialog buttons texts
        binding.positiveButton.text = getString(R.string.button_label_use)
        binding.negativeButton.text = getString(R.string.button_label_no)

        // Set button onClickListener for purchasing tool item
        binding.positiveButton.setOnClickListener {
            // Buy tool item
            viewModel.purchaseTool(toolId)

            // Dismiss this dialog
            findNavController().popBackStack()
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