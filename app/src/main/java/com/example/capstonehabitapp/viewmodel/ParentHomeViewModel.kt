package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

class ParentHomeViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _parentName: MutableLiveData<Response<String>> = MutableLiveData()
    val parentName: LiveData<Response<String>> = _parentName
    private val _essentialTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val essentialTasks: LiveData<Response<List<Task>>> = _essentialTasks

    // Fetch parent data from Firestore
    fun getParentFromFirebase() = CoroutineScope(Dispatchers.IO).launch {
        _parentName.postValue(Response.Loading())

        try {
            // Call Firestore get() method to query the data
            val querySnapshot = parentDocRef.get().await()
            val name = querySnapshot.getString("name")
            val isMale = querySnapshot.getBoolean("isMale")

            val nameWithTitle = if (isMale!!) "Pak $name" else "Ibu $name"

            _parentName.postValue(Response.Success(nameWithTitle))

        } catch (e: Exception) {
            e.message?.let { _parentName.postValue(Response.Failure(it)) }
        }
    }

    // Fetch essential tasks data from Firestore
    fun getEssentialTasksFromFirebase() {
        val responseList: MutableList<Task> = mutableListOf()

        _essentialTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query tasks that need grading
                val querySnapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("status", 3)
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
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