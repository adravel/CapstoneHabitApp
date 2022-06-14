package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.FinishedTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentHistoryBinding
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.Timestamp

class HistoryFragment: Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var finishedTaskAdapter: FinishedTaskAdapter

    // private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.history)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Delete this
        // Dummies
        val tasks = mutableListOf(
            Task(
                title = "A",
                timeFinishWorking = Timestamp(1560523991, 86000000),
                childName = "Lorem",
                difficulty = 0
            ),
            Task(
                title = "B",
                timeFinishWorking = Timestamp(1569523991, 86000000),
                childName = "Ipsum",
                difficulty = 1
            ),
            Task(
                title = "C",
                timeFinishWorking = Timestamp(1520523991, 86000000),
                childName = "Dolor",
                difficulty = 2
            )
        )
        val childName = "Lorem"
        val finishedTaskCount = 12
        val isMale = true

        // Set the adapter and layoutManager for finished task list RecyclerView
        finishedTaskAdapter = FinishedTaskAdapter(tasks)
        binding.finishedTaskListRecyclerView.apply {
            adapter = finishedTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Retrieve child ID and level from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), false)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // TODO: Retrieve these from LiveData
        // Display child month summary data
        binding.monthSummaryCongratsText.text = getString(R.string.history_congrats_placeholder, childName)
        binding.monthSummaryDescriptionText.text = getString(R.string.history_description_placeholder, finishedTaskCount)
        if (isMale) {
            binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_male)
        } else {
            binding.monthSummaryAvatarImage.setImageResource(R.drawable.img_soldier_female)
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