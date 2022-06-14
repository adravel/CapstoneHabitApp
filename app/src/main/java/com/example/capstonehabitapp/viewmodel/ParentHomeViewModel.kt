package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ParentHomeViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _parent: MutableLiveData<Response<Pair<String, Boolean>>> = MutableLiveData()
    private val _essentialTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    val parent: LiveData<Response<Pair<String, Boolean>>> = _parent
    val essentialTasks: LiveData<Response<List<Task>>> = _essentialTasks

    // Fetch parent data from Firestore
    fun getParentFromFirebase() {
        _parent.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the data
                val snapshot = parentDocRef.get().await()
                val name = snapshot.getString("name")!!
                val isMale = snapshot.getBoolean("isMale")!!

                _parent.postValue(Response.Success(Pair(name, isMale)))

            } catch (e: Exception) {
                e.message?.let { _parent.postValue(Response.Failure(it)) }
            }
        }
    }

    // Fetch essential tasks data from Firestore
    fun getEssentialTasksForParentFromFirebase() {
        _essentialTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query tasks that need grading
                val snapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("status", 3)
                    .get()
                    .await()

                // Convert each document into Task object and add them to task list
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
}