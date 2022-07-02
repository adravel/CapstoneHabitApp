package com.fortfighter.ui.fragment

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
import com.bumptech.glide.Glide
import com.fortfighter.R
import com.fortfighter.adapter.ChildRankAdapter
import com.fortfighter.databinding.FragmentRankingBinding
import com.fortfighter.model.Child
import com.fortfighter.util.Response
import com.fortfighter.util.getLevelNameString
import com.fortfighter.viewmodel.RankingViewModel

class RankingFragment : Fragment() {

    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var childRankAdapter: ChildRankAdapter

    private lateinit var childId: String

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
        // and get the child ID if the current Role is Child
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)
        if (!isParent) childId = sharedPref.getString(getString(R.string.role_pref_child_id_key), "")!!

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
                    val children = response.data
                    childRankAdapter.updateList(children)

                    // Get the index of the Child data that match the ID if the current user is Child
                    // and show the highlighted rank card
                    if (!isParent) {
                        val index = children.indexOfFirst { it.id == childId}
                        displayCurrentChildRankCard(children[index], index + 1)
                    }
                }
                is Response.Failure -> {
                    Log.e("Ranking", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Hide the highlighted rank card if the current user is Parent
        if (isParent) {
            binding.greyBackground.visibility = View.GONE
            binding.highlightedChildRankCard.root.visibility = View.GONE
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

    // Method for displaying the highlighted rank card
    private fun displayCurrentChildRankCard(child: Child, rankNumber: Int) {
        binding.highlightedChildRankCard.apply {
            rankNumberText.text = rankNumber.toString()
            nameText.text = child.name
            levelNameText.text = getLevelNameString(requireContext(), child.level.toInt())
            totalPointsText.text = getString(R.string.child_total_points_placeholder, child.totalPoints.toInt())

            // Display medal image according to ranking
            val medalImageResId = when (rankNumber) {
                1 -> R.drawable.img_medal_gold
                2 -> R.drawable.img_medal_silver
                3 -> R.drawable.img_medal_bronze
                else -> null
            }
            if (medalImageResId != null) {
                Glide.with(this@RankingFragment)
                    .load(medalImageResId)
                    .into(medalImage)
            }

            // Display avatar image according to gender
            Glide.with(this@RankingFragment)
                .load(if (child.isMale) {
                    R.drawable.img_soldier_male
                } else {
                    R.drawable.img_soldier_female
                })
                .into(avatarImage)
        }
    }
}