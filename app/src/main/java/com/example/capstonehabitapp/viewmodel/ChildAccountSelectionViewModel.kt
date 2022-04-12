package com.example.capstonehabitapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Child
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ChildAccountSelectionViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _children: MutableLiveData<List<Child>> = MutableLiveData()
    val children: LiveData<List<Child>> = _children

    // Fetch children data from Firestore
    fun getChildrenFromFirebase() {
        val responseList: MutableList<Child> = mutableListOf()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val querySnapshot = parentDocRef
                    .collection("children")
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
                for (document in querySnapshot.documents) {
                    document.toObject<Child>()?.let { responseList.add(it) }
                }

                _children.postValue(responseList)

            } catch (e: Exception) {
                e.message?.let { Log.e("ChildAccountSelection", it) }
            }
        }
    }
}