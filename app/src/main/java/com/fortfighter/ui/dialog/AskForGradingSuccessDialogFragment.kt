package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fortfighter.R
import com.fortfighter.databinding.DialogBigIconBinding

class AskForGradingSuccessDialogFragment: DialogFragment() {

    private var _binding: DialogBigIconBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogBigIconBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind the data to the Views
        binding.bigIconImage.setImageResource(R.drawable.ic_big_done)
        binding.messageText.text = getString(R.string.ask_for_grading_success_message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}