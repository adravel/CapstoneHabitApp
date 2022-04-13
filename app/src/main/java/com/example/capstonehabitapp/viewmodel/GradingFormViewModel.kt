package com.example.capstonehabitapp.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Child
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

    private val _child: MutableLiveData<Child> = MutableLiveData()
    val child: LiveData<Child> = _child

    // Calculate grade points depending on the difficulty and grade
    fun getGradePointsInt(difficulty: Int, grade: String): Int {
        var gradePoints = 0
        when (difficulty) {
            0 -> gradePoints = when (grade) {
                "Kurang" -> 3
                "Baik" -> 4
                else -> 6
            }
            1 -> gradePoints = when (grade) {
                "Kurang" -> 5
                "Baik" -> 6
                else -> 8
            }
            2 -> gradePoints = when (grade) {
                "Kurang" -> 7
                "Baik" -> 8
                else -> 10
            }
        }
        return gradePoints
    }

    // Fetch child data from Firestore
    fun getChildFromFirebase(childId: String) {
        var response: Child

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data and convert it to Child object
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()
                response = querySnapshot.toObject<Child>()!!

                _child.postValue(response)

            } catch (e: Exception) {
                e.message?.let { Log.e("GradingForm", it) }
            }
        }
    }

    // Update task status to 4
    fun gradeTask(taskId: String, gradePoints: Int, notes: String) {
        val updates = hashMapOf<String, Any>(
            "status" to 4,
            "gradePoints" to gradePoints,
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

//        Log.i("GradingForm", "Level = $level")
//        Log.i("GradingForm", "Total Points = $totalPoints")
//        Log.i("GradingForm", "Current Points = $currentPoints")

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