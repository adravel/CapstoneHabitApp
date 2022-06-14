package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTwoButtonsDialogBinding
import com.example.capstonehabitapp.viewmodel.ShopViewModel

class ToolSaleConfirmationDialogFragment: DialogFragment() {

    private var _binding: FragmentTwoButtonsDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolId: String
    private lateinit var toolName: String
    private lateinit var childId: String
    private lateinit var childName: String

    private val viewModel: ShopViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTwoButtonsDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize tool and child data using Safe Args provided by navigation component
        val args: ToolSaleConfirmationDialogFragmentArgs by navArgs()
        toolId = args.toolId
        toolName = args.toolName
        childId = args.childId
        childName = args.childName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.tool_sale_confirmation_message, toolName, childName)
        binding.positiveButton.text = getString(R.string.button_label_send)
        binding.negativeButton.text = getString(R.string.button_label_cancel)

        // Set button onClickListener for selling item
        binding.positiveButton.setOnClickListener {
            // Sell this tool
            viewModel.setToolForSale(childId, toolId, toolName)

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