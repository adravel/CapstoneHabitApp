package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.model.Task
import com.fortfighter.util.Response
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
    fun addTaskToFirebase(task: Task) {
        _taskId.postValue(Response.Loading())

        val newTask = hashMapOf(
            "title" to task.title,
            "category" to task.category,
            "area" to task.area,
            "difficulty" to task.difficulty,
            "startTimeLimit" to task.startTimeLimit,
            "finishTimeLimit" to task.finishTimeLimit,
            "detail" to task.detail,
            "status" to 0,
            "childId" to "",
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
    fun updateTask(task: Task) {
        _taskId.postValue(Response.Loading())

        val taskUpdate = hashMapOf<String, Any>(
            "title" to task.title,
            "category" to task.category,
            "area" to task.area,
            "difficulty" to task.difficulty,
            "startTimeLimit" to task.startTimeLimit,
            "finishTimeLimit" to task.finishTimeLimit,
            "detail" to task.detail,
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