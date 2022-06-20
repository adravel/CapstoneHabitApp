package com.fortfighter.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fortfighter.R
import com.fortfighter.databinding.DialogTwoButtonsBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.TaskDetailViewModel

class TaskDeleteConfirmationDialogFragment: DialogFragment() {

    private var _binding: DialogTwoButtonsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = DialogTwoButtonsBinding.inflate(inflater, container, false)

        // Set rounded corner background for this dialog
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageText.text = getString(R.string.task_delete_confirmation_message)
        binding.positiveButton.text = getString(R.string.button_label_delete)
        binding.negativeButton.text = getString(R.string.button_label_cancel)

        viewModel.task.observe(viewLifecycleOwner) { response ->
            if (response is Response.Success) {
                val task = response.data

                // Set button onClickListener for deleting the task
                binding.positiveButton.setOnClickListener {
                    // Call the method for deleting the task
                    viewModel.deleteTask(task.id)

                    // Dismiss this dialog
                    findNavController().popBackStack()
                }

                // Set button onClickListener for canceling action
                binding.negativeButton.setOnClickListener {
                    // Dismiss this dialog
                    findNavController().popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}