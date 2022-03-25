package com.example.capstonehabitapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.capstonehabitapp.databinding.FragmentParentHomeBinding
import com.google.firebase.firestore.ktx.firestore
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

    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val db = Firebase.firestore
    private val parentDocumentRef = db.collection("parents").document(testParentId)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentParentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get parent data from Firestore
        getParent(testParentId)

        binding.taskMenuCard.setOnClickListener {
            it.findNavController().navigate(R.id.taskListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getParent(parentId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocumentRef.get().await()
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
}