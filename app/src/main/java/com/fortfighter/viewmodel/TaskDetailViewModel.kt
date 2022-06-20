package com.fortfighter.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.model.Task
import com.fortfighter.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat

class TaskDetailViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)
    private val storageRef = Firebase.storage.reference

    private val _task: MutableLiveData<Response<Task>> = MutableLiveData()
    private val _taskStatusChange: MutableLiveData<Response<Int>> = MutableLiveData()
    private val _taskDeleteResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    private val _taskPhoto: MutableLiveData<Response<Bitmap>> = MutableLiveData()
    val task: LiveData<Response<Task>> = _task
    val taskStatusChange: LiveData<Response<Int>> = _taskStatusChange
    val taskDeleteResponse: LiveData<Response<Unit>> = _taskDeleteResponse
    val taskPhoto: LiveData<Response<Bitmap>> = _taskPhoto

    // Check whether task is finished within the time limit
    fun isTimeLimitSurpassed(task: Task): Boolean {
        // Check whether task has time limit
        if (task.startTimeLimit != "" && task.finishTimeLimit != "") {
            // Get the date child start working as String in "yyyy/MM/dd" format
            val timeLimitDateString = convertTimestampToString(task.timeStartWorking!!, "yyyy/MM/dd")

            // Get the finish time limit as String in "HH:mm" format
            val timeLimitTimeString = task.finishTimeLimit

            // Combine the time limit to obtain date and time as String
            // in "yyyy/MM/dd HH:mm" format
            val timeLimitDateTimeString = "$timeLimitDateString $timeLimitTimeString"

            // Get the current Date
            val currentDateTime = getCurrentDateTime()

            // Parse the time limit String to Date
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val timeLimitDateTime = sdf.parse(timeLimitDateTimeString)

            // Check whether task time limit is surpassed and return the value
            return currentDateTime > timeLimitDateTime
        } else {
            // Return false if task does not have time limit
            return false
        }
    }

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

    // Fetch task photo image from Cloud Storage
    fun getTaskPhotoFromStorage(taskId: String) {
        // Check if task photo has been successfully fetched
        val response = _taskPhoto.value
        if (response !is Response.Success) {
            // Task photo has not been successfully fetched
            _taskPhoto.postValue(Response.Loading())

            val photoStorageRef = storageRef.child(parentId).child(taskId)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Download image with maximum size 1 MB
                    val maxDownloadSize = 1L * 1024 * 1024
                    val bytes = photoStorageRef.getBytes(maxDownloadSize).await()
                    val imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    _taskPhoto.postValue(Response.Success(imageBitmap))

                } catch (e: Exception) {
                    e.message?.let { _taskPhoto.postValue(Response.Failure(it)) }
                }
            }
        }
    }

    // Fetch task data from Firestore
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
    // and upload photo to Cloud Storage
    fun askForGrading(taskId: String, bitmap: Bitmap) {
        _taskStatusChange.postValue(Response.Loading())

        val updates = hashMapOf(
            "status" to 3,
            "timeAskForGrading" to FieldValue.serverTimestamp()
        )

        val photoStorageRef = storageRef.child(parentId).child(taskId)

        var storageWriteSuccess = false
        var dbWriteSuccess = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Convert photo bitmap into ByteArray
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                // Upload photo to Cloud Storage
                // in path of "parentId/taskId"
                photoStorageRef.putBytes(data).await()
                storageWriteSuccess = true

                // Update the necessary field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update(updates)
                    .await()
                dbWriteSuccess = true

                _taskStatusChange.postValue(Response.Success(3))

            } catch (e: Exception) {
                e.message?.let { _taskStatusChange.postValue(Response.Failure(it)) }

                // Delete the saved photo from Cloud Storage
                // if the query to upload photo to Cloud Storage is successful
                // but the query to update task document in Firestore is not
                if (storageWriteSuccess && !dbWriteSuccess) {
                    photoStorageRef.delete()
                }
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

    // Update task status to 5
    fun failTask(taskId: String) {
        _taskStatusChange.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update the status field in Firestore document
                parentDocRef
                    .collection("tasks")
                    .document(taskId)
                    .update("status", 5)
                    .await()

                _taskStatusChange.postValue(Response.Success(5))

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

    // Set taskPhoto LiveData value to null
    fun clearTaskPhoto() {
        _taskPhoto.value = null
    }
}