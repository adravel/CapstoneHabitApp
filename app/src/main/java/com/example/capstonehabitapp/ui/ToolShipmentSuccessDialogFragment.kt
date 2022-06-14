package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentOneButtonDialogBinding

class ToolShipmentSuccessDialogFragment: DialogFragment() {

    private var _binding: FragmentOneButtonDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var toolName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentOneButtonDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Initialize tool and child data using Safe Args provided by navigation component
        val args: ToolShipmentSuccessDialogFragmentArgs by navArgs()
        toolName = args.toolName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.tool_shipment_success_message, toolName)
        binding.button.text = getString(R.string.button_label_ok)

        // Set button onClickListener
        binding.button.setOnClickListener {
            // Dismiss this dialog
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}