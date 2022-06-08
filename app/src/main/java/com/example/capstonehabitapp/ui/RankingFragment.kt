package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ChildRankAdapter
import com.example.capstonehabitapp.databinding.FragmentRankingBinding
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.RankingViewModel

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var childRankAdapter: ChildRankAdapter

    private val viewModel: RankingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentRankingBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.ranking)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check the user's role from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)

        // Set the adapter and layoutManager for child rank RecyclerView
        childRankAdapter = ChildRankAdapter(mutableListOf())
        binding.rankingRecycleView.apply {
            adapter = childRankAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch children data from Firestore
        viewModel.getChildrenFromFirebase()

        // Observe children LiveData in ViewModel
        viewModel.children.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val task = response.data
                    childRankAdapter.updateList(task)
                }
                is Response.Failure -> {
                    Log.e("Ranking", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
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