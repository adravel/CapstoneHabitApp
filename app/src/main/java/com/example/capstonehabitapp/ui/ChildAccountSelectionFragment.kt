package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ChildAccountAdapter
import com.example.capstonehabitapp.databinding.FragmentChildAccountSelectionBinding
import com.example.capstonehabitapp.viewmodel.ChildAccountSelectionViewModel

class ChildAccountSelectionFragment: Fragment() {

    private var _binding: FragmentChildAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var childAccountAdapter: ChildAccountAdapter

    private val viewModel: ChildAccountSelectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentChildAccountSelectionBinding.inflate(inflater, container, false)

        // Set toolbar title
        activity?.title = getString(R.string.choose_child_account)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user's role data from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref?.getBoolean(getString(R.string.role_pref_is_parent_key), true)

        // Set the adapter and layoutManager for child list RecyclerView
        childAccountAdapter = ChildAccountAdapter(mutableListOf(), isParent!!)
        binding.childAccountListRecycleView.apply {
            adapter = childAccountAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch child accounts data from Firestore
        viewModel.getChildrenFromFirebase()

        // Observe child accounts LiveData in ViewModel
        viewModel.children.observe(viewLifecycleOwner) { children ->
            if (children.isNotEmpty()) {
                childAccountAdapter.updateList(children)
            }
        }

        // Show option to add new child only when Child is selecting their accounts
        binding.addChildButton.visibility = if (isParent) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}