package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class TaskCreationTemplateViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private var selectedTemplateTaskCategory = ""

    private val _taskId: MutableLiveData<Response<String>> = MutableLiveData()
    val taskId: LiveData<Response<String>> = _taskId

    // Method for accessing selected template task
    fun setTemplateTaskCategory(category: String) { selectedTemplateTaskCategory = category }
    fun getTemplateTask(): Task {
        // Return the first task that match the selected template task category
        val index = templateTasks.indexOfFirst { task -> task.category == selectedTemplateTaskCategory}
        return templateTasks[index]
    }

    // Add new task to Firestore and return its ID
    fun addTaskToFirebase(task: Task) {
        _taskId.postValue(Response.Loading())

        val newTask = hashMapOf(
            "title" to task.title,
            "category" to task.category,
            "area" to task.area,
            "difficulty" to task.difficulty,
            "startTimeLimit" to task.startTimeLimit,
            "finishTimeLimit" to task.finishTimeLimit,
            "detail" to task.detail,
            "status" to 0,
            "timeCreated" to FieldValue.serverTimestamp()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Add the data to Firestore
                val snapshot = parentDocRef.collection("tasks").add(newTask).await()

                // Get task ID from the snapshot
                val taskId = snapshot.id

                _taskId.postValue(Response.Success(taskId))

            } catch (e: Exception) {
                e.message?.let { _taskId.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set taskId LiveData value to null
    fun taskIdResponseHandled() {
        _taskId.value = null
    }

    // A list containing template task data
    // 1 template task for each 4 categories
    private val templateTasks = listOf(
        Task(
            title = "Mengerjakan PR",
            category = "Kreativitas",
            area = "Ruang Belajar",
            difficulty = 1
        ),
        Task(
            title = "Menyapu Kamar",
            category = "Tugas Rumah",
            area = "Kamar",
            difficulty = 1
        ),
        Task(
            title = "Makan",
            category = "Kesehatan",
            area = "Ruang Makan",
            difficulty = 0
        ),
        Task(
            title = "Mandi",
            category = "Perawatan Diri",
            area = "Kamar Mandi",
            difficulty = 0
        )
    )
}