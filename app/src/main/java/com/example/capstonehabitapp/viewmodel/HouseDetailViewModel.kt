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

    private var childId = ""
    private var houseId = ""

    private val _house: MutableLiveData<Response<House>> = MutableLiveData()
    val house: LiveData<Response<House>> = _house
    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    val tools: LiveData<Response<List<Tool>>> = _tools
    private val _childCash: MutableLiveData<Response<Int>> = MutableLiveData()
    val childCash: LiveData<Response<Int>> = _childCash
    private val _toolPurchaseResult: MutableLiveData<Response<Int>> = MutableLiveData()
    val toolPurchaseResult: LiveData<Response<Int>> = _toolPurchaseResult

    // Functions to set document IDs
    fun setChildId(id: String) { childId = id }
    fun setHouseId(id: String) { houseId = id }

    // Fetch house data from Firestore
    fun getHouseFromFirebase() {
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
    fun getToolsForSaleFromFirebase() {
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
    fun getChildCashFromFirestore() {
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

    // Use tool to destroy the fort
    // This function subtracts tool price from child cash
    // and tool power from house hp
    fun purchaseTool(toolId: String) {
        _toolPurchaseResult.postValue(Response.Loading())

        val childDocRef = parentDocRef.collection("children").document(childId)
        val houseDocRef = childDocRef.collection("houses").document(houseId)
        val toolDocRef = childDocRef.collection("tools").document(toolId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Firebase.firestore.runTransaction { transaction ->
                    val childSnapshot = transaction.get(childDocRef)
                    val houseSnapshot = transaction.get(houseDocRef)
                    val toolSnapshot = transaction.get(toolDocRef)

                    // Calculate new value of cash after subtraction
                    val cash = childSnapshot.getLong("cash")!! - toolSnapshot.getLong("price")!!
                    if (cash < 0) {
                        _toolPurchaseResult.postValue(Response.Failure("cash"))
                        return@runTransaction
                    }

                    // Calculate value of health points data after subtraction
                    val hp = houseSnapshot.getLong("hp")!! - toolSnapshot.getLong("power")!!

                    // Update child and house documents
                    transaction.update(childDocRef, "cash", cash)
                    transaction.update(houseDocRef, "hp", hp)
                }.await()

                // Check if there is any failure in completing transaction
                if (_toolPurchaseResult.value is Response.Failure) {
                    return@launch
                } else {
                    _toolPurchaseResult.postValue(Response.Success(1))
                }

            } catch (e: Exception) {
                e.message?.let { _toolPurchaseResult.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set tool name LiveData value to null
    fun toolPurchaseResultResponseHandled() {
        _toolPurchaseResult.value = null
    }
}