package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskDetailViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _task: MutableLiveData<Response<Task>> = MutableLiveData()
    val task: LiveData<Response<Task>> = _task
    private val _taskStatusChange: MutableLiveData<Response<Int>> = MutableLiveData()
    val taskStatusChange: LiveData<Response<Int>> = _taskStatusChange

    // Calculate task duration
    fun getTaskDurationString(task: Task): String {
        val start = task.timeStartWorking!!
        val finish = task.timeFinishWorking!!
        val duration = (finish.seconds - start.seconds) / 60
        return "$duration menit"
    }

    // Fetch task data from Firebase
    fun getTaskFromFirebase(taskId: String) {
        var response: Task

        _task.postValue(Response.Loading())
        _taskStatusChange.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val querySnapshot = parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .get()
                    .await()

                // Convert the document into Task object
                response = querySnapshot.toObject<Task>()!!

                _task.postValue(Response.Success(response))

            } catch (e: Exception) {
                e.message?.let { _task.postValue(Response.Failure(it)) }
            }
        }
    }

    // Update task state to 1
    fun startTask(taskId: String, childId: String, childName: String) {
        _taskStatusChange.postValue(Response.Loading())

        val updates = hashMapOf(
            "status" to 1,
            "childId" to childId,
            "childName" to childName,
            "timeStartWorking" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                _taskStatusChange.postValue(Response.Success(1))

            } catch (e: Exception) {
                e.message?.let { _taskStatusChange.postValue(Response.Failure(it)) }
            }
        }
    }

    // Update task status to 2
    fun finishTask(taskId: String) {
        _taskStatusChange.postValue(Response.Loading())

        val updates = hashMapOf(
            "status" to 2,
            "timeFinishWorking" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                _taskStatusChange.postValue(Response.Success(2))

            } catch (e: Exception) {
                e.message?.let { _taskStatusChange.postValue(Response.Failure(it)) }
            }
        }
    }

    // Update task status to 3
    fun askForGrading(taskId: String) {
        _taskStatusChange.postValue(Response.Loading())

        val updates = hashMapOf(
            "status" to 3,
            "timeAskForGrading" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

                _taskStatusChange.postValue(Response.Success(3))

            } catch (e: Exception) {
                e.message?.let { _taskStatusChange.postValue(Response.Failure(it)) }
            }
        }
    }
}