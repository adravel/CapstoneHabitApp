package com.example.capstonehabitapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstonehabitapp.R
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.adapter.EssentialTaskAdapter
import com.example.capstonehabitapp.databinding.FragmentParentHomeBinding
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

class ParentHomeFragment: Fragment() {

    private val TAG = "ParentHomeFragment"

    private var _binding: FragmentParentHomeBinding? = null
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

        _binding = FragmentParentHomeBinding.inflate(inflater, container, false)
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

        // Get parent data from Firestore
        getParent()

        // Get essential tasks data from Firestore
        getEssentialTasks()

        binding.taskMenuCard.setOnClickListener {
            it.findNavController().navigate(R.id.taskListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getParent() = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocRef.get().await()
            val parentName = querySnapshot.getString("name")
            val parentIsMale = querySnapshot.getBoolean("isMale")

            // Bind the data to TextViews
            withContext(Dispatchers.Main) {
                binding.parentNameText.text = if (parentIsMale!!) "Pak $parentName !" else "Ibu $parentName !"
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Pengambilan data gagal", Toast.LENGTH_LONG).show()
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    private fun getEssentialTasks() = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query tasks that need grading
            val querySnapshot = parentDocRef
                .collection("tasks")
                .whereEqualTo("status", 3)
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