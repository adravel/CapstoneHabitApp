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

class HistoryViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _finishedTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val finishedTasks: LiveData<Response<List<Task>>> = _finishedTasks

    // Fetch tasks data from Firestore for a specific child
    // where the status is 2, 3, or 4
    // and ordered by the latest task finished
    fun getFinishedTasksFromFirebase(childId: String) {
        _finishedTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the tasks data
                val snapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("childId", childId)
                    .whereGreaterThanOrEqualTo("status", 2)
                    .whereLessThanOrEqualTo("status", 4)
                    .get()
                    .await()

                // Convert each document into Task object
                val tasks = mutableListOf<Task>()
                for(document in snapshot.documents) {
                    document.toObject<Task>()?.let {
                        tasks.add(it)
                    }
                }

                _finishedTasks.postValue(Response.Success(tasks.sortedByDescending { it.timeFinishWorking }))

            } catch (e: Exception) {
                e.message?.let { _finishedTasks.postValue(Response.Failure(it)) }
            }
        }
    }
}