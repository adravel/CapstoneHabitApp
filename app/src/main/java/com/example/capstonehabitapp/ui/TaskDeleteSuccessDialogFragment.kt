package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentBigIconDialogBinding

class TaskDeleteSuccessDialogFragment: DialogFragment() {

    private var _binding: FragmentBigIconDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentBigIconDialogBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind the data to the Views
        binding.bigIconImage.setBackgroundResource(R.drawable.ic_big_done)
        binding.messageText.text = getString(R.string.task_delete_success_message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}