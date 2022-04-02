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
        essentialTaskAdapter = EssentialTaskAdapter(essentialTaskList, false)
        binding.essentialTaskListRecyclerView.apply {
            adapter = essentialTaskAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Retrieve child ID from shared preference
        val sharedPref = activity?.getSharedPreferences(getString(R.string.role_pref_key), Context.MODE_PRIVATE)
        val childId = sharedPref?.getString(getString(R.string.role_pref_child_id_key), "")

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

            withContext(Dispatchers.Main) {
                if (child != null) {
                    // Get the name of the level
                    val levelName = when (child.level.toInt()) {
                        1 -> getString(R.string.level_1_name)
                        2 -> getString(R.string.level_2_name)
                        3 -> getString(R.string.level_3_name)
                        4 -> getString(R.string.level_4_name)
                        5 -> getString(R.string.level_5_name)
                        6 -> getString(R.string.level_6_name)
                        7 -> getString(R.string.level_7_name)
                        8 -> getString(R.string.level_8_name)
                        9 -> getString(R.string.level_9_name)
                        else -> getString(R.string.level_10_name)
                    }

                    // Determine total points needed to level up
                    val pointsToLevelUp = child.level.toInt() * 50

                    // Bind the data to Views
                    binding.apply {
                        greetingsText.text = getString(R.string.child_greetings_placeholder, child.name)
                        levelText.text = getString(R.string.child_level_placeholder, levelName)
                        expText.text = getString(R.string.child_exp_placeholder, child.totalPoints.toInt(), pointsToLevelUp)
                        expProgressBar.max = pointsToLevelUp
                        expProgressBar.progress = child.totalPoints.toInt()
                    }
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_SHORT).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}