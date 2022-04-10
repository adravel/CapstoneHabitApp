package com.example.capstonehabitapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
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
        detail: String): String {

        // Generate empty document reference
        val taskDocRef = parentDocRef.collection("tasks").document()

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
                // Send the data using Firestore set() method
                taskDocRef.set(newTask).await()

            } catch (e: Exception) {
                e.message?.let { Log.e("TaskCreation", it) }
            }
        }
        return taskDocRef.id
    }
}