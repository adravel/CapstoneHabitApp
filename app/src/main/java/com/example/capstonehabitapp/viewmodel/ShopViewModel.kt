package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.Tool
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

class ShopViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    private val _toolName: MutableLiveData<Response<String>> = MutableLiveData()
    val tools: LiveData<Response<List<Tool>>> = _tools
    val toolName: LiveData<Response<String>> = _toolName

    // Fetch tools data from Firestore
    fun getToolsFromFirebase(childId: String) {
        _tools.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("tools")
                    .get()
                    .await()

                // Convert each document into Tool object and add them to the list
                val tools = mutableListOf<Tool>()
                for (document in snapshot.documents) {
                    document.toObject<Tool>()?. let { tools.add(it) }
                }

                _tools.postValue(Response.Success(tools))

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