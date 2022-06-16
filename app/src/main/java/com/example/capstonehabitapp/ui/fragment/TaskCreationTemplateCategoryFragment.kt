package com.example.capstonehabitapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.databinding.FragmentTaskCreationTemplateCategoryBinding

class TaskCreationTemplateCategoryFragment : Fragment() {

    private var _binding: FragmentTaskCreationTemplateCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentTaskCreationTemplateCategoryBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.choose_category)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set create task manual button onClickListener
        binding.createTaskManualButton.setOnClickListener {

            // Navigate to TaskCreationManualFragment with false argument
            val action = TaskCreationTemplateCategoryFragmentDirections
                .actionTaskCreationTemplateCategoryFragmentToTaskCreationManualFragment(false)
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
}