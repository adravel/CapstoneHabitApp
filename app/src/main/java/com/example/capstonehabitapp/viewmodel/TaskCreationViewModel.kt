package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskCreationViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _taskId: MutableLiveData<Response<String>> = MutableLiveData()
    val taskId: LiveData<Response<String>> = _taskId

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
        area: String,
        difficulty: Int,
        startTimeLimit: String,
        finishTimeLimit: String,
        detail: String) {

        _taskId.postValue(Response.Loading())

        val newTask = hashMapOf(
            "title" to title,
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
                val querySnapshot = parentDocRef.collection("tasks").add(newTask).await()

                val taskId = querySnapshot.id

                _taskId.postValue(Response.Success(taskId))

            } catch (e: Exception) {
                e.message?.let { _taskId.postValue(Response.Failure(it)) }
            }
        }
    }
}