package com.example.capstonehabitapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationTemplateTaskBinding
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.getTaskDifficultyImageResId

class TaskCreationTemplateTaskFragment : Fragment() {

    private var _binding: FragmentTaskCreationTemplateTaskBinding? = null
    private val binding get() = _binding!!

    private var templateTaskCategoryIndex: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskCreationTemplateTaskBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.choose_task)

        // Initialize task category using Safe Args provided by navigation component
        val args: TaskCreationTemplateTaskFragmentArgs by navArgs()
        templateTaskCategoryIndex = args.templateTaskCategoryIndex

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load template task data
        val templateTask = when (templateTaskCategoryIndex!!) {
            0 -> templateTaskCreativity
            1 -> templateTaskHousework
            2 -> templateTaskHealth
            else -> templateTaskSelfCare
        }
        binding.templateTaskCard.apply {
            titleText.text = templateTask.title
            infoText.text = templateTask.area
            difficultyImage.setImageResource(getTaskDifficultyImageResId(templateTask.difficulty.toInt()))
        }

        // Set create task manual button onClickListener
        binding.createTaskManualButton.setOnClickListener {
            // Navigate to TaskCreationManualFragment with false argument
            val action = TaskCreationTemplateTaskFragmentDirections
                .actionTaskCreationTemplateTaskFragmentToTaskCreationManualFragment(false)
            findNavController().navigate(action)
        }

        // Set back button onClickListener
        binding.toolbarLayout.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // TODO: Store these val in ViewModel
    private val templateTaskCreativity = Task(
        title = "Mengerjakan PR",
        category = "Kreativitas",
        area = "Ruang Belajar",
        difficulty = 1
    )
    private val templateTaskHousework = Task(
        title = "Menyapu Kamar",
        category = "Tugas Rumah",
        area = "Kamar",
        difficulty = 1
    )
    private val templateTaskHealth = Task(
        title = "Makan",
        category = "Kesehatan",
        area = "Ruang Makan",
        difficulty = 0
    )
    private val templateTaskSelfCare = Task(
        title = "Mandi",
        category = "Perawatan Diri",
        area = "Kamar Mandi",
        difficulty = 0
    )
}