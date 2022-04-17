package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.*
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskListViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _tasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val tasks: LiveData<Response<List<Task>>> = _tasks

    // Fetch tasks data from Firestore
    fun getTasksFromFirebase() {
        val responseList: MutableList<Task> = mutableListOf()

        _tasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val querySnapshot = parentDocRef
                    .collection("tasks")
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
                for(document in querySnapshot.documents) {
                    document.toObject<Task>()?.let { responseList.add(it) }
                }

                _tasks.postValue(Response.Success(responseList))

            } catch (e: Exception) {
                e.message?.let { _tasks.postValue(Response.Failure(it)) }
            }
        }
    }
}