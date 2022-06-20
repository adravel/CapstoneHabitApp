package com.fortfighter.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fortfighter.R
import com.fortfighter.databinding.DialogDetailBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.LevelUpViewModel

class LevelUpDialogFragment: DialogFragment() {

    private var _binding: DialogDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var childId: String
    private var childNewLevel: Int = 0
    private lateinit var childNewLevelName: String

     private val viewModel: LevelUpViewModel by viewModels()

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

        // Initialize task ID using Safe Args provided by navigation component
        val args: LevelUpDialogFragmentArgs by navArgs()
        childId = args.childId
        childNewLevel = args.childNewLevel
        childNewLevelName = args.childNewLevelName

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Calculate the bonus cash that will be obtained
        val bonus = viewModel.getBonus(childNewLevel)

        // Display the dialog image and texts
        binding.image.setImageResource(R.drawable.img_level_up)
        binding.titleText.text = getString(R.string.level_up_message_title)
        binding.descriptionText.text = getString(R.string.level_up_message_description, childNewLevelName, bonus)
        binding.button.text = getString(R.string.button_label_get_bonus)

        // Set button onCLickListener
        binding.button.setOnClickListener {
            // Add the bonus to child cash data in Firestore
            viewModel.updateChildCashWithBonus(childId, bonus)
        }

        // Observe cashUpdateResponse LiveData in ViewModel
        // to determine whether cash update transaction is successful or not
        viewModel.cashUpdateResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Pop this dialog fragment
                    findNavController().popBackStack()
                }
                is Response.Failure -> {
                    Log.e("LevelUp", response.message)
                    Toast.makeText(context, getString(R.string.request_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}