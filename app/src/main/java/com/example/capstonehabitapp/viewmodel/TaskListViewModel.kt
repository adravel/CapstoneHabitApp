package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.*
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskListViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _tasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val tasks: LiveData<Response<List<Task>>> = _tasks

    // Fetch tasks data from Firestore
    fun getTasksFromFirebase() {
        _tasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val snapshot = parentDocRef
                    .collection("tasks")
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
                val tasks = mutableListOf<Task>()
                for(document in snapshot.documents) {
                    document.toObject<Task>()?.let { tasks.add(it) }
                }

                _tasks.postValue(Response.Success(tasks))

            } catch (e: Exception) {
                e.message?.let { _tasks.postValue(Response.Failure(it)) }
            }
        }
    }
}