package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskCreationManualViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private var editedTaskId = ""

    private val _taskId: MutableLiveData<Response<String>> = MutableLiveData()
    val taskId: LiveData<Response<String>> = _taskId

    // Method to set the ID of the task that is currently being edited
    fun setEditedTaskId(id: String) { editedTaskId = id }

    // Get difficulty as integer
    fun getDifficultyInt(difficulty: String): Int {
        return when (difficulty) {
            "Mudah" -> 0
            "Sedang" -> 1
            "Sulit" -> 2
            else -> 0
        }
    }

    // Add new task to Firestore and return its ID
    fun addTaskToFirebase(
        title: String,
        category: String,
        area: String,
        difficulty: Int,
        startTimeLimit: String,
        finishTimeLimit: String,
        detail: String) {

        _taskId.postValue(Response.Loading())

        val newTask = hashMapOf(
            "title" to title,
            "category" to category,
            "area" to area,
            "difficulty" to difficulty,
            "startTimeLimit" to startTimeLimit,
            "finishTimeLimit" to finishTimeLimit,
            "detail" to detail,
            "status" to 0,
            "timeCreated" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Add the data to Firestore
                val snapshot = parentDocRef.collection("tasks").add(newTask).await()

                // Get task ID from the snapshot
                val taskId = snapshot.id

                _taskId.postValue(Response.Success(taskId))

            } catch (e: Exception) {
                e.message?.let { _taskId.postValue(Response.Failure(it)) }
            }
        }
    }

    // Add new task to Firestore and return its ID
    fun updateTask(
        title: String,
        area: String,
        difficulty: Int,
        startTimeLimit: String,
        finishTimeLimit: String,
        detail: String) {

        _taskId.postValue(Response.Loading())

        val taskUpdate = hashMapOf<String, Any>(
            "title" to title,
            "area" to area,
            "difficulty" to difficulty,
            "startTimeLimit" to startTimeLimit,
            "finishTimeLimit" to finishTimeLimit,
            "detail" to detail
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update task document in Firestore
                parentDocRef
                    .collection("tasks")
                    .document(editedTaskId)
                    .update(taskUpdate)
                    .await()

                _taskId.postValue(Response.Success(editedTaskId))

            } catch (e: Exception) {
                e.message?.let { _taskId.postValue(Response.Failure(it)) }
            }
        }
    }
}