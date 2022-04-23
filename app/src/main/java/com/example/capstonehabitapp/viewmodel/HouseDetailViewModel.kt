package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.House
import com.example.capstonehabitapp.model.Tool
import com.example.capstonehabitapp.util.Response
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class HouseDetailViewModel: ViewModel() {
    private val testParentId = "2p8at5eicReHAP1P4zDu"
    private val parentDocRef = Firebase.firestore.collection("parents").document(testParentId)

    private val _house: MutableLiveData<Response<House>> = MutableLiveData()
    val house: LiveData<Response<House>> = _house
    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    val tools: LiveData<Response<List<Tool>>> = _tools
    private val _childCash: MutableLiveData<Response<Int>> = MutableLiveData()
    val childCash: LiveData<Response<Int>> = _childCash

    // Fetch house data from Firestore
    fun getHouseFromFirebase(childId: String, houseId: String) {
        _house.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("houses")
                    .document(houseId)
                    .get()
                    .await()

                // Convert document to House object
                val response = querySnapshot.toObject<House>()!!

                _house.postValue(Response.Success(response))

            } catch (e: Exception) {
                e.message?.let { _house.postValue(Response.Failure(it)) }
            }
        }
    }

    // Fetch tools data from Firestore
    // where isForSale value is true
    fun getToolsForSaleFromFirebase(childId: String) {
        val responseList: MutableList<Tool> = mutableListOf()

        _tools.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("tools")
                    .whereEqualTo("isForSale", true)
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

    // Fetch cash data in child document from Firestore
    fun getChildCashFromFirestore(childId: String) {
        _childCash.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()

                // Get cash data from the document snapshot
                val response = querySnapshot.getLong("cash")!!.toInt()

                _childCash.postValue(Response.Success(response))

            } catch (e: Exception) {
                e.message?.let { _childCash.postValue(Response.Failure(it)) }
            }
        }
    }
}