package com.example.capstonehabitapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.DialogBigIconBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.TaskDetailViewModel

class GradingSuccessDialogFragment: DialogFragment() {

    private var _binding: DialogBigIconBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskDetailViewModel by activityViewModels()

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

        // Observe task LiveData in SharedViewModel
        viewModel.task.observe(viewLifecycleOwner) { response ->
            if (response is Response.Success) {
                val task = response.data

                // Get the grading data from the LiveData
                val childName = task.childName
                val difficulty = task.difficulty.toInt()
                val grade = task.grade.toInt()
                val gradePoints = viewModel.getGradePoints(difficulty, grade)

                // Bind the data to the Views
                binding.bigIconImage.setBackgroundResource(R.drawable.ic_big_add)
                binding.messageText.text = getString(R.string.grading_success_message, childName, gradePoints)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}