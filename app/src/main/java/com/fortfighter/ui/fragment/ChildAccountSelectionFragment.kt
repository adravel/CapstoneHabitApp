package com.fortfighter.ui.fragment

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
import com.fortfighter.R
import com.fortfighter.adapter.ChildAccountAdapter
import com.fortfighter.databinding.FragmentChildAccountSelectionBinding
import com.fortfighter.util.Response
import com.fortfighter.viewmodel.ChildAccountSelectionViewModel

class ChildAccountSelectionFragment: Fragment() {

    private var _binding: FragmentChildAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var childAccountAdapter: ChildAccountAdapter

    private var isForParent: Boolean? = null

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
        isForParent = args.isForParent

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for child list RecyclerView
        childAccountAdapter = ChildAccountAdapter(mutableListOf(), isForParent!!)
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
        // and set its onClickListener
        binding.addChildButton.visibility = if (!isForParent!!) View.VISIBLE else View.GONE
        binding.addChildButton.setOnClickListener {
            // Navigate to add child page
            findNavController().navigate(R.id.addChildFragment)
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