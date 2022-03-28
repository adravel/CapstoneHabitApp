package com.example.capstonehabitapp.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.databinding.FragmentChildHomeBinding
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

class ChildHomeFragment: Fragment() {

    private val TAG = "ChildHomeFragment"

    private var _binding: FragmentChildHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var essentialTaskList: MutableList<Task>
    private lateinit var essentialTaskAdapter: EssentialTaskAdapter

    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private lateinit var parentDocRef: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize Firebase reference
        parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

        // Initialize essential task list as an empty MutableList
        essentialTaskList = mutableListOf()

        _binding = FragmentChildHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the adapter and layoutManager for essential task list RecyclerView
        essentialTaskAdapter = EssentialTaskAdapter(essentialTaskList, true)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Retrieve child ID from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_preference_key), Context.MODE_PRIVATE)
        val childId = sharedPref?.getString("selectedChildId", "")

        // Get child and essential task data from Firestore
        if (childId != null && childId != "") {
            getChild(childId)
            getEssentialTasks(childId)
        }

        binding.taskMenuCard.setOnClickListener {
            it.findNavController().navigate(R.id.taskListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getChild(childId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data and convert it to Child object
            val querySnapshot = parentDocRef
                .collection("children")
                .document(childId)
                .get()
                .await()
            val child = querySnapshot.toObject<Child>()

            // Bind the data to Views
            withContext(Dispatchers.Main) {
                if (child != null) {
                    binding.greetingsText.text = "Halo, ${child.name} !"
                    binding.levelText.text = "Level : ${child.level}"
                    binding.expText.text = "Exp : ${child.points}/100"
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    private fun getEssentialTasks(childId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query tasks that need grading
            val querySnapshot = parentDocRef
                .collection("tasks")
                .whereEqualTo("childId", childId)
                .whereEqualTo("status", 1)
                .get()
                .await()

            // Convert each document into Task object and add them to task list
            for(document in querySnapshot.documents) {
                document.toObject<Task>()?.let { essentialTaskList.add(it) }
            }

            withContext(Dispatchers.Main) {
                essentialTaskAdapter.notifyDataSetChanged()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}