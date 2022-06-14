package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentChildHomeBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.util.getLevelNameString
import com.example.capstonehabitapp.viewmodel.ChildHomeViewModel

class ChildHomeFragment: Fragment() {

    private var _binding: FragmentChildHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var essentialTaskAdapter: EssentialTaskAdapter

    private val viewModel: ChildHomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle back button press to close the app
        requireActivity().onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentChildHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for essential task list RecyclerView
        essentialTaskAdapter = EssentialTaskAdapter(mutableListOf(), false)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Retrieve child ID from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

        // Fetch child and essential task data from Firestore
        viewModel.getChildFromFirebase(childId)
        viewModel.getEssentialTasksForChildFromFirebase(childId)

        // Observe essential tasks LiveData in ViewModel
        viewModel.essentialTasks.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tasks = response.data
                    if (tasks.isNotEmpty()) essentialTaskAdapter.updateList(tasks)
                }
                is Response.Failure -> {
                    Log.e("ChildHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe child LiveData in ViewModel
        viewModel.child.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    // Clear the LiveData so the code below will be executed only once
                    // and the level up dialog would not be displayed again if the bonus
                    // has been retrieved
                    viewModel.childResponseHandled()

                    val child = response.data
                    val level = child.level.toInt()

                    val levelName = getLevelNameString(requireContext(), level)
                    val progress = viewModel.getProgress(child.totalPoints.toInt(), level)

                    // Bind the data to Views
                    binding.apply {
                        greetingsText.text = getString(R.string.child_greetings_placeholder, child.name)
                        levelText.text = getString(R.string.child_level_placeholder, levelName)
                        expText.text = getString(R.string.child_exp_placeholder, progress)
                        expProgressBar.progress = progress

                        // Display avatar image according to gender
                        if (child.isMale) {
                            childAvatarImage.setImageResource(R.drawable.img_soldier_male)
                        } else {
                            childAvatarImage.setImageResource(R.drawable.img_soldier_female)
                        }
                    }

                    // Display level up dialog if hasLeveledUp field value is true
                    // and the level is higher than 1
                    if (child.hasLeveledUp && level > 1) {
                        // Go to level up dialog
                        val action = ChildHomeFragmentDirections
                            .actionChildHomeFragmentToLevelUpDialogFragment(childId, level, levelName)
                        findNavController().navigate(action)
                    }
                }
                is Response.Failure -> {
                    Log.e("ChildHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply {
            // Set change role button onClickListener
            toolbarLayout.changeRoleButton.setOnClickListener {
                findNavController().navigate(R.id.roleSelectionFragment)
            }

            // Set task menu card onClickListener
            taskMenuCard.setOnClickListener {
                findNavController().navigate(R.id.taskListFragment)
            }

            // Set house menu card onClickListener
            houseMenuCard.setOnClickListener {
                findNavController().navigate(R.id.houseListFragment)
            }

            // Set ranking menu card onClickListener
            rankingMenuCard.setOnClickListener {
                findNavController().navigate(R.id.rankingFragment)
            }

            // Set history menu card onClickListener
            historyMenuCard.setOnClickListener {
                findNavController().navigate(R.id.historyFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}