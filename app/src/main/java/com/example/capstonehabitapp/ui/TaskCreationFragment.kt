package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationBinding
import com.example.capstonehabitapp.viewmodel.TaskCreationViewModel

class TaskCreationFragment : Fragment() {

    private var _binding: FragmentTaskCreationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskCreationViewModel by viewModels()

    override fun onResume() {
        super.onResume()

        // Set array adapter for difficulty drop-down menu
        val difficulties = resources.getStringArray(R.array.difficulties)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, difficulties)
        binding.difficultyAutoCompleteTextView.setAdapter(arrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTaskCreationBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.create_task)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Disable button by default
            createTaskButton.isEnabled = false

            // Set TextChangedListener for task title, area, and difficulty EditTexts
            val editTexts = listOf(titleEditText, areaEditText, difficultyAutoCompleteTextView)
            for (editText in editTexts) {
                editText.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val et1 = titleEditText.text.toString().trim()
                        val et2 = areaEditText.text.toString().trim()
                        val et3 = difficultyAutoCompleteTextView.text.toString().trim()

                        // Enable button if the EditTexts is not empty
                        createTaskButton.isEnabled = et1.isNotEmpty()
                                && et2.isNotEmpty()
                                && et3.isNotEmpty()
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            createTaskButton.setOnClickListener {
                val title = titleEditText.text.toString()
                val area = areaEditText.text.toString()
                val difficulty = viewModel.getDifficultyInt(difficultyAutoCompleteTextView.text.toString())
                val startTimeLimit = startTimeLimitEditText.text.toString()
                val finishTimeLimit = finishTimeLimitEditText.text.toString()
                val detail = detailEditText.text.toString()

                // add new task data to Firestore and get the ID
                val id = viewModel.addTaskToFirebase(title, area, difficulty, startTimeLimit, finishTimeLimit, detail)

                // Navigate to task detail page
                val action = TaskCreationFragmentDirections.actionTaskCreationFragmentToTaskDetailFragment(id)
                view.findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}