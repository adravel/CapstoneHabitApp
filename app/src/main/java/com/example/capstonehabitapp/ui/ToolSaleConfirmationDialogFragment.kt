package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentToolSaleConfirmationDialogBinding

class ToolSaleConfirmationDialogFragment: DialogFragment() {

    private var _binding: FragmentToolSaleConfirmationDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolName: String
    private lateinit var childName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentToolSaleConfirmationDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize tool name and child name using Safe Args provided by navigation component
        val args: ToolSaleConfirmationDialogFragmentArgs by navArgs()
        toolName = args.toolName
        childName = args.childName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.tool_sale_confirmation_message_placeholder, toolName, childName)

        binding.sellButton.setOnClickListener {
            // TODO: Write method to sell this tool and change its status to sold
            Log.i("ToolSaleConfirmation", "Tool $toolName is sold to $childName")

            // Dismiss this dialog
            findNavController().popBackStack()
        }

        binding.cancelButton.setOnClickListener {
            // Dismiss this dialog
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}