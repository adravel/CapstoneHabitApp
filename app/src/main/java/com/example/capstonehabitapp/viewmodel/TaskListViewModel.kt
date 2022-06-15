package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.*
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
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
    private val _tasksCount: MutableLiveData<Triple<Int, Int, Int>> = MutableLiveData()
    val tasks: LiveData<Response<List<Task>>> = _tasks
    val tasksCount: LiveData<Triple<Int, Int, Int>> = _tasksCount

    // Fetch all tasks data from Firestore for this parent
    // ordered by the latest task created
    fun getTasksFromFirebase() {
        _tasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val snapshot = parentDocRef
                    .collection("tasks")
                    .orderBy("timeCreated", Query.Direction.DESCENDING)
                    .get()
                    .await()

                // Convert each document into Task object,
                // add them to task list,
                // and count the amount of on going, finished, and failed tasks
                val tasks = mutableListOf<Task>()
                var onGoingTaskCount = 0
                var finishedTaskCount = 0
                var failedTaskCount = 0

                for(document in snapshot.documents) {
                    document.toObject<Task>()?.let {
                        tasks.add(it)

                        when (it.status.toInt()) {
                            1 -> onGoingTaskCount++
                            2, 3, 4 -> finishedTaskCount++
                            5 -> failedTaskCount++
                            else -> {}
                        }
                    }
                }
                val taskCount = Triple(onGoingTaskCount, finishedTaskCount, failedTaskCount)

                _tasks.postValue(Response.Success(tasks))
                _tasksCount.postValue(taskCount)

            } catch (e: Exception) {
                e.message?.let { _tasks.postValue(Response.Failure(it)) }
            }
        }
    }
}