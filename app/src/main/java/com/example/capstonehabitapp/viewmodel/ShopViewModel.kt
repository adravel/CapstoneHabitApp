package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Tool
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class ShopViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    val tools: LiveData<Response<List<Tool>>> = _tools
    private val _toolName: MutableLiveData<Response<String>> = MutableLiveData()
    val toolName: LiveData<Response<String>> = _toolName

    // Fetch tools data from Firestore
    fun getToolsFromFirebase(childId: String) {
        val responseList: MutableList<Tool> = mutableListOf()

        _tools.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("tools")
                    .get()
                    .await()

                // Convert each document into Tool object and add them to the list
                for (document in querySnapshot.documents) {
                    document.toObject<Tool>()?. let { responseList.add(it) }
                }

                _tools.postValue(Response.Success(responseList))

            } catch (e: Exception) {
                e.message?.let { _tools.postValue(Response.Failure(it)) }
            }
        }
    }

    // Update tool isForSale value to true
    fun setToolForSale(childId: String, toolId: String, toolName: String) {
        _toolName.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("tools")
                    .document(toolId)
                    .update("isForSale", true)
                    .await()

                _toolName.postValue(Response.Success(toolName))

            } catch (e: Exception) {
                e.message?.let { _toolName.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set tool name LiveData value to null
    fun toolNameResponseHandled() {
        _toolName.value = null
    }
}