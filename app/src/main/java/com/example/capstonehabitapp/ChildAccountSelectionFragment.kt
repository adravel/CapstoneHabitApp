package com.example.capstonehabitapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.databinding.FragmentChildAccountSelectionBinding
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class ChildAccountSelectionFragment: Fragment() {
    private val TAG = "ChildAccountSelection"

    private var _binding: FragmentChildAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var childAccountList: MutableList<Child>
    private lateinit var childAccountAdapter: ChildAccountAdapter

    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private lateinit var parentDocRef: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Set toolbar title
        activity?.title = getString(R.string.choose_child_account)

        // Initialize Firebase reference
        parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

        // Initialize child account list as an empty MutableList
        childAccountList = mutableListOf()

        _binding = FragmentChildAccountSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for child list RecyclerView
        childAccountAdapter = ChildAccountAdapter(childAccountList)
        binding.childAccountListRecycleView.apply {
            adapter = childAccountAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Get all children data from Firestore
        getChildren()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getChildren() = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocRef
                .collection("children")
                .get()
                .await()

            // Convert each document into Task object and add them to task list
            for(document in querySnapshot.documents) {
                document.toObject<Child>()?.let { childAccountList.add(it) }
            }

            withContext(Dispatchers.Main) {
                childAccountAdapter.notifyDataSetChanged()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}