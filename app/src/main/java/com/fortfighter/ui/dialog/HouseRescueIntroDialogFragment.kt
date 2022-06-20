package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.fortfighter.R
import com.fortfighter.databinding.DialogDetailBinding

class HouseRescueIntroDialogFragment: DialogFragment() {

    private var _binding: DialogDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogDetailBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        // Prevent the dialog from being dismissed
        // when user click outside the dialog or pressed the back button
        dialog!!.setCancelable(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display the dialog image and texts
        binding.image.setImageResource(R.drawable.img_game_house_intact)
        binding.titleText.text = getString(R.string.house_rescue_intro_message_title)
        binding.descriptionText.text = getString(R.string.house_rescue_intro_message_description)
        binding.button.text = getString(R.string.button_label_house_rescue)

        // Set button onCLickListener
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