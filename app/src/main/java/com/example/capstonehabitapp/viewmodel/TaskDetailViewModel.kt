package com.example.capstonehabitapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Task
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
    private val TAG = "TaskDetail"
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val task: MutableLiveData<Task> = MutableLiveData()

    // Function to return observable immutable LiveData
    fun getTask(): LiveData<Task> = task

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

                task.postValue(response)

            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    // Button click function
    fun changeTaskStatus(taskId: String, taskStatus: Int, isForParent: Boolean, childId: String, childName: String) {
        if (isForParent) {
            // TODO: Implement on button click function to grade task
            Log.i(TAG, "Button on click function is not implemented yet")
        } else {
            when (taskStatus) {
                0 -> startTask(taskId, childId, childName)
                1 -> finishTask(taskId)
                2 -> askForGrading(taskId)
            }
        }
    }

    // Update task state to 1
    private fun startTask(taskId: String, childId: String, childName: String) {
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

                // Fetch task from Firestore again to update the UI
                getTaskFromFirebase(taskId)

            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    // Update task status to 2
    private fun finishTask(taskId: String) {
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

                // Fetch task from Firestore again to update the UI
                getTaskFromFirebase(taskId)

            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    // Update task status to 3
    private fun askForGrading(taskId: String) {
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

                // Fetch task from Firestore again to update the UI
                getTaskFromFirebase(taskId)

            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }
}