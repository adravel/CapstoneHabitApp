package com.fortfighter.ui.fragment

import android.annotation.SuppressLint
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
import com.fortfighter.R
import com.fortfighter.adapter.EssentialTaskAdapter
import com.fortfighter.databinding.FragmentParentHomeBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.ParentHomeViewModel

class ParentHomeFragment: Fragment() {

    private var _binding: FragmentParentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var essentialTaskAdapter: EssentialTaskAdapter

    private val viewModel: ParentHomeViewModel by viewModels()

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
        _binding = FragmentParentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for essential task list RecyclerView
        essentialTaskAdapter = EssentialTaskAdapter(mutableListOf(), true)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch parent and essential task data from Firestore
        viewModel.getParentFromFirebase()
        viewModel.getEssentialTasksForParentFromFirebase()

        // Observe parent LiveData in ViewModel
        viewModel.parent.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val (name, isMale) = response.data

                    // Display avatar image and name text according to the gender
                    if (isMale) {
                        binding.greetingsText.text = getString(R.string.parent_male_greetings_placeholder, name)
                        binding.parentAvatarImage.setImageResource(R.drawable.img_general_male)
                    } else {
                        binding.greetingsText.text = getString(R.string.parent_female_greetings_placeholder, name)
                        binding.parentAvatarImage.setImageResource(R.drawable.img_general_female)
                    }
                }
                is Response.Failure -> {
                    Log.e("ParentHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe essential tasks LiveData in ViewModel
        viewModel.essentialTasks.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val tasks = response.data
                    essentialTaskAdapter.updateList(tasks)
                }
                is Response.Failure -> {
                    Log.e("ParentHome", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.apply {
            // Display menu cards icon image
            taskMenuCardIcon.setImageResource(R.drawable.img_menu_task)
            storeMenuCardIcon.setImageResource(R.drawable.img_menu_store)
            rankingMenuCardIcon.setImageResource(R.drawable.img_menu_ranking)
            historyMenuCardIcon.setImageResource(R.drawable.img_menu_history)

            // Set change role button onClickListener
            toolbarLayout.changeRoleButton.setOnClickListener {
                findNavController().navigate(R.id.roleSelectionFragment)
            }

            // Set task menu card onClickListener
            taskMenuCard.setOnClickListener {
                findNavController().navigate(R.id.taskListFragment)
            }

            // Set store menu card onClickListener
            storeMenuCard.setOnClickListener {
                val action = ParentHomeFragmentDirections
                    .actionParentHomeFragmentToChildAccountSelectionFragment(true)
                findNavController().navigate(action)
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