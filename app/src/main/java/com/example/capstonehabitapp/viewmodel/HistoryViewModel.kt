package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.*
import com.example.capstonehabitapp.model.Child
import com.example.capstonehabitapp.model.Task
import com.example.capstonehabitapp.util.Response
import com.example.capstonehabitapp.util.getCurrentDateTime
import com.example.capstonehabitapp.util.getDayMonthYearFromDate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class HistoryViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    // Set the default selected child index as the index of the first item on the list
    // So the view will display the oldest child account created data
    // as the default if the user has not selected any child
    var selectedChildIndex = 0

    private val _finishedTasks: MutableLiveData<Response<List<Task>>> = MutableLiveData()
    private val _weeklyTaskCounts: MutableLiveData<List<Int>> = MutableLiveData()
    private val _children: MutableLiveData<Response<List<Child>>> = MutableLiveData()
    private val _child: MutableLiveData<Response<Child>> = MutableLiveData()
    val finishedTasks: LiveData<Response<List<Task>>> = _finishedTasks
    val weeklyTaskCounts: LiveData<List<Int>> = _weeklyTaskCounts
    val children: LiveData<Response<List<Child>>> = _children
    val child: LiveData<Response<Child>> = _child

    // Fetch tasks data from Firestore for a specific child
    // where the status is 2, 3, or 4
    // and ordered by the latest task finished
    fun getFinishedTasksFromFirebase(childId: String) {
        _finishedTasks.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query the tasks data
                val snapshot = parentDocRef
                    .collection("tasks")
                    .whereEqualTo("childId", childId)
                    .whereGreaterThanOrEqualTo("status", 2)
                    .whereLessThanOrEqualTo("status", 4)
                    .get()
                    .await()

                // Get the current month and year as Int
                val currentDateTime = getCurrentDateTime()
                val (_, currentMonth, currentYear) = getDayMonthYearFromDate(currentDateTime)

                // Declare weekly task count variables
                var week1TaskCount = 0
                var week2TaskCount = 0
                var week3TaskCount = 0
                var week4TaskCount = 0

                // Convert each document into Task object,
                // add them to task list,
                // and count the number of tasks finished each week this month
                val tasks = mutableListOf<Task>()
                for(document in snapshot.documents) {
                    document.toObject<Task>()?.let {
                        tasks.add(it)

                        // Get the day, month, and year of each task
                        val taskDateTime = it.timeFinishWorking!!.toDate()
                        val (taskDay, taskMonth, taskYear) = getDayMonthYearFromDate(taskDateTime)

                        // Increment weekly task counts
                        if (taskMonth == currentMonth && taskYear == currentYear) {
                            when (taskDay) {
                                in 1..7 -> week1TaskCount++
                                in 8..14 -> week2TaskCount++
                                in 15..21 -> week3TaskCount++
                                in 22..31 -> week4TaskCount++
                            }
                        }
                    }
                }
                val weeklyTaskCounts = listOf(week1TaskCount, week2TaskCount, week3TaskCount, week4TaskCount)

                _finishedTasks.postValue(Response.Success(tasks.sortedByDescending { it.timeFinishWorking }))
                _weeklyTaskCounts.postValue(weeklyTaskCounts)

            } catch (e: Exception) {
                e.message?.let { _finishedTasks.postValue(Response.Failure(it)) }
            }
        }
    }

    // Fetch children data from Firestore
    fun getChildrenFromFirebase() {
        _children.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Call Firestore get() method to query a list of children data
                // sorted by the oldest created account
                val snapshot = parentDocRef
                    .collection("children")
                    .orderBy("timeCreated", Query.Direction.ASCENDING)
                    .get()
                    .await()

                // Convert each document into Child object and add them to child list
                val children = mutableListOf<Child>()
                for (document in snapshot.documents) {
                    document.toObject<Child>()?.let { children.add(it) }
                }

                _children.postValue(Response.Success(children))

            } catch (e: Exception) {
                e.message?.let { _children.postValue(Response.Failure(it)) }
            }
        }
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
}