package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.model.Child
import com.fortfighter.model.Task
import com.fortfighter.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ChildHomeViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _child: MutableLiveData<Response<Child>> = MutableLiveData()
    private val _essentialTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val child: LiveData<Response<Child>> = _child
    val essentialTasks: LiveData<Response<List<Task>>> = _essentialTasks

    // Determine the progress for leveling up in the scale of 50 points
    fun getProgress(totalPoints: Int, level: Int): Int {
        val scale = 50
        val maxLevel = 10
        val progress = totalPoints - ((level - 1) * scale)
        return if (progress >= scale && level == maxLevel) scale else progress
    }

    // Fetch child data from Firestore
    fun getChildFromFirebase(childId: String) {
        _child.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data and convert it to Child object
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()
                val child = snapshot.toObject<Child>()!!

                _child.postValue(Response.Success(child))

            } catch (e: Exception) {
                e.message?.let { _child.postValue(Response.Failure(it)) }
            }
        }
    }

    // Fetch essential tasks data from Firestore
    fun getEssentialTasksForChildFromFirebase(childId: String) {
        _essentialTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query tasks that need grading
                val snapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("childId", childId)
                    .whereEqualTo("status", 1)
                    .get()
                    .await()

                // Convert each document into Task object and add them to essential task list
                val tasks = mutableListOf<Task>()
                for(document in snapshot.documents) {
                    document.toObject<Task>()?.let { tasks.add(it) }
                }

                _essentialTasks.postValue(Response.Success(tasks))

            } catch (e: Exception) {
                e.message?.let { _essentialTasks.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set child LiveData value to null
    fun childResponseHandled() {
        _child.value = null
    }
}