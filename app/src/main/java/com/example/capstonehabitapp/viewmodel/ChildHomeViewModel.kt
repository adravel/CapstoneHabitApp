package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ChildHomeViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _child: MutableLiveData<Response<Child>> = MutableLiveData()
    val child: LiveData<Response<Child>> = _child
    private val _essentialTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
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

    // Fetch essential tasks data from Firestore
    fun getEssentialTasksFromFirebase(childId: String) {
        val responseList: MutableList<Task> = mutableListOf()

        _essentialTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query tasks that need grading
                val querySnapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("childId", childId)
                    .whereEqualTo("status", 1)
                    .get()
                    .await()

                // Convert each document into Task object and add them to essential task list
                for(document in querySnapshot.documents) {
                    document.toObject<Task>()?.let { responseList.add(it) }
                }

                _essentialTasks.postValue(Response.Success(responseList))

            } catch (e: Exception) {
                e.message?.let { _essentialTasks.postValue(Response.Failure(it)) }
            }
        }
    }
}