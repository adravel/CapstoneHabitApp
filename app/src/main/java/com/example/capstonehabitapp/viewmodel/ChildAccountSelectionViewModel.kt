package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.util.Response
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

    private val _children: MutableLiveData<Response<List<Child>>> = MutableLiveData()
    val children: LiveData<Response<List<Child>>> = _children

    // Fetch children data from Firestore
    fun getChildrenFromFirebase() {
        _children.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val snapshot = parentDocRef
                    .collection("children")
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
                val children = mutableListOf<Child>()
                for (document in snapshot.documents) {
                    document.toObject<Child>()?.let { children.add(it) }
                }

                _children.postValue(Response.Success(children))

            } catch (e: Exception) {
                e.message?.let { _children.postValue(Response.Failure(it)) }
            }
        }
    }
}