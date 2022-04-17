package com.example.capstonehabitapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GradingFormViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _child: MutableLiveData<Response<Child>> = MutableLiveData()
    val child: LiveData<Response<Child>> = _child

    // Convert grade string to integer
    fun getGradeInt(grade: String): Int {
        return when (grade) {
            "Kurang" -> 1
            "Baik" -> 2
            "Sangat Baik" -> 3
            else -> 0
        }
    }

    // Calculate grade points depending on the difficulty and grade
    fun getGradePoints(difficulty: Int, grade: Int): Int {
        var gradePoints = 0
        when (difficulty) {
            0 -> gradePoints = when (grade) {
                1 -> 3
                2 -> 4
                3 -> 6
                else -> 0
            }
            1 -> gradePoints = when (grade) {
                1 -> 5
                2 -> 6
                3 -> 8
                else -> 0
            }
            2 -> gradePoints = when (grade) {
                1 -> 7
                2 -> 8
                3 -> 10
                else -> 0
            }
        }
        return gradePoints
    }

    // Fetch child data from Firestore
    fun getChildFromFirebase(childId: String) {
        var response: Child

        _child.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data and convert it to Child object
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()
                response = querySnapshot.toObject<Child>()!!

                _child.postValue(Response.Success(response))

            } catch (e: Exception) {
                e.message?.let { _child.postValue(Response.Failure(it)) }
            }
        }
    }

    // Update task status to 4
    fun gradeTask(taskId: String, grade: Int, notes: String) {
        val updates = hashMapOf<String, Any>(
            "status" to 4,
            "grade" to grade,
            "notes" to notes
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()

            } catch (e: Exception) {
                e.message?.let { Log.e("GradingForm", it) }
            }
        }
    }

    // Update child totalPoints, currentPoints, and level after grading the task
    fun updateChildPointsAndLevel(child: Child, gradePoints: Int) {
        val totalPoints = child.totalPoints.toInt() + gradePoints
        var currentPoints = child.currentPoints.toInt() + gradePoints
        var level = child.level.toInt()
        val pointsToLevelUp = level * 50

        // Level up if the child has enough points to level up and has not reached max level
        if (totalPoints >= pointsToLevelUp && level < 10) {
            // Gain bonus
            currentPoints += level * 5

            // Level up
            level++
        }

        val updates = hashMapOf<String, Any>(
            "level" to level,
            "totalPoints" to totalPoints,
            "currentPoints" to currentPoints
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("children")
                    .document(child.id)
                    .update(updates)
                    .await()

            } catch (e: Exception) {
                e.message?.let { Log.e("GradingForm", it) }
            }
        }
    }
}