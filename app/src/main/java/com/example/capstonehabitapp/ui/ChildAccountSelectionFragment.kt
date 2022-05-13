package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.adapter.ChildAccountAdapter
import com.example.capstonehabitapp.databinding.FragmentChildAccountSelectionBinding
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.viewmodel.ChildAccountSelectionViewModel

class ChildAccountSelectionFragment: Fragment() {

    private var _binding: FragmentChildAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var childAccountAdapter: ChildAccountAdapter

    private var isAddChildButtonVisible: Boolean? = null

    private val viewModel: ChildAccountSelectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout for this fragment
        _binding = FragmentChildAccountSelectionBinding.inflate(inflater, container, false)

        // Set toolbar title
        binding.toolbarLayout.toolbar.title = getString(R.string.choose_child_account)

        // Initialize task ID using Safe Args provided by navigation component
        val args: ChildAccountSelectionFragmentArgs by navArgs()
        isAddChildButtonVisible = args.isAddChildButtonVisible

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get user's role data from shared preference
        val sharedPref = requireActivity().getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val isParent = sharedPref.getBoolean(getString(R.string.role_pref_is_parent_key), true)

        // Set the adapter and layoutManager for child list RecyclerView
        childAccountAdapter = ChildAccountAdapter(mutableListOf(), isParent)
        binding.childAccountListRecycleView.apply {
            adapter = childAccountAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Fetch child accounts data from Firestore
        viewModel.getChildrenFromFirebase()

        // Observe child accounts LiveData in ViewModel
        viewModel.children.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {}
                is Response.Success -> {
                    val children = response.data
                    if (children.isNotEmpty()) childAccountAdapter.updateList(children)
                }
                is Response.Failure -> {
                    Log.e("ChildAccountSelection", response.message)
                    Toast.makeText(context, getString(R.string.data_fetch_failed), Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Show option to add new child only when Child is selecting their accounts
        binding.addChildButton.visibility = if (isAddChildButtonVisible!!) View.VISIBLE else View.GONE

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