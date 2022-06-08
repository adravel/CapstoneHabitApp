package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
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
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _task: MutableLiveData<Response<Task>> = MutableLiveData()
    private val _taskStatusChange: MutableLiveData<Response<Int>> = MutableLiveData()
    private val _taskDeleteResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val task: LiveData<Response<Task>> = _task
    val taskStatusChange: LiveData<Response<Int>> = _taskStatusChange
    val taskDeleteResponse: LiveData<Response<Unit>> = _taskDeleteResponse

    // Calculate task duration
    fun getTaskDurationString(task: Task): String {
        val start = task.timeStartWorking!!
        val finish = task.timeFinishWorking!!
        val duration = (finish.seconds - start.seconds) / 60
        return "$duration menit"
    }

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

    // Fetch task data from Firebase
    fun getTaskFromFirebase(taskId: String) {
        _task.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val snapshot = parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .get()
                    .await()

                // Convert the document into Task object
                val task = snapshot.toObject<Task>()!!

                _task.postValue(Response.Success(task))

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

    // Update task status to 4
    // and update child totalPoints, currentPoints, and level simultaneously
    // using Firestore transaction operation
    fun gradeTask(taskId: String, childId: String, grade: Int, gradePoints: Int, notes: String) {
        _taskStatusChange.postValue(Response.Loading())

        val taskDocRef = parentDocRef.collection("tasks").document(taskId)
        val childDocRef = parentDocRef.collection("children").document(childId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runTransaction { transaction ->
                    // Call Firestore get() method to query child data
                    val snapshot = transaction.get(childDocRef)

                    val totalPoints = snapshot.getLong("totalPoints")!!.toInt() + gradePoints
                    val cash = snapshot.getLong("cash")!!.toInt() + (gradePoints * 10)
                    var level = snapshot.getLong("level")!!.toInt()
                    val pointsToLevelUp = level * 50
                    var hasLeveledUp = false

                    // Level up if the child has enough points to level up and has not reached max level
                    if (totalPoints >= pointsToLevelUp && level < 10) {
                        // Level up
                        level++

                        // Set the level up flag to true
                        hasLeveledUp = true
                    }

                    val taskUpdates = hashMapOf<String, Any>(
                        "status" to 4,
                        "grade" to grade,
                        "notes" to notes
                    )

                    val childUpdates = hashMapOf<String, Any>(
                        "level" to level,
                        "hasLeveledUp" to hasLeveledUp,
                        "totalPoints" to totalPoints,
                        "cash" to cash
                    )

                    // Update task and child documents
                    transaction.update(taskDocRef, taskUpdates)
                    transaction.update(childDocRef, childUpdates)
                }.await()

                _taskStatusChange.postValue(Response.Success(4))

            } catch (e: Exception) {
                e.message?.let { _taskStatusChange.postValue(Response.Failure(it)) }
            }
        }
    }

    // Delete task document in Firestore
    fun deleteTask(taskId: String) {
        _taskDeleteResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                parentDocRef.collection("tasks").document(taskId).delete().await()

                _taskDeleteResponse.postValue(Response.Success(Unit))

            } catch (e: Exception) {
                e.message?.let { _taskDeleteResponse.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set task status change LiveData value to null
    fun taskStatusChangeResponseHandled() {
        _taskStatusChange.value = null
    }

    // Set taskDeleteResponse LiveData value to null
    fun taskDeleteResponseHandled() {
        _taskDeleteResponse.value = null
    }
}