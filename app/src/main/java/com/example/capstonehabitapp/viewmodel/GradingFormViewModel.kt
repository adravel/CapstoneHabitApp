package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GradingFormViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _taskGradePoints: MutableLiveData<Response<Int>> = MutableLiveData()
    val taskGradePoints: LiveData<Response<Int>> = _taskGradePoints

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

    // Update task status to 4 and
    // update child totalPoints, currentPoints, and level simultaneously
    // using Firestore transaction operation
    fun gradeTask(taskId: String, childId: String, grade: Int, gradePoints: Int, notes: String) {
        _taskGradePoints.postValue(Response.Loading())

        val taskDocRef = parentDocRef.collection("tasks").document(taskId)
        val childDocRef = parentDocRef.collection("children").document(childId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Firebase.firestore.runTransaction { transaction ->
                    // Call Firestore get() method to query child data
                    val querySnapshot = transaction.get(childDocRef)

                    val totalPoints = querySnapshot.getLong("totalPoints")!!.toInt() + gradePoints
                    var cash = querySnapshot.getLong("cash")!!.toInt() + (gradePoints * 10)
                    var level = querySnapshot.getLong("level")!!.toInt()
                    val pointsToLevelUp = level * 50

                    // Level up if the child has enough points to level up and has not reached max level
                    if (totalPoints >= pointsToLevelUp && level < 10) {
                        // Gain bonus
                        cash += level * 5 * 10

                        // Level up
                        level++
                    }

                    val taskUpdates = hashMapOf<String, Any>(
                        "status" to 4,
                        "grade" to grade,
                        "notes" to notes
                    )

                    val childUpdates = hashMapOf<String, Any>(
                        "level" to level,
                        "totalPoints" to totalPoints,
                        "cash" to cash
                    )

                    // Update task and child documents
                    transaction.update(taskDocRef, taskUpdates)
                    transaction.update(childDocRef, childUpdates)
                }.await()

                _taskGradePoints.postValue(Response.Success(gradePoints))

            } catch (e: Exception) {
                e.message?.let { _taskGradePoints.postValue(Response.Failure(it)) }
            }
        }
    }
}