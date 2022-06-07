package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.House
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

class HouseDetailViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private var childId = ""
    private var houseId = ""

    private val _house: MutableLiveData<Response<House>> = MutableLiveData()
    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    private val _childCash: MutableLiveData<Response<Int>> = MutableLiveData()
    private val _toolPurchaseResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val house: LiveData<Response<House>> = _house
    val tools: LiveData<Response<List<Tool>>> = _tools
    val childCash: LiveData<Response<Int>> = _childCash
    val toolPurchaseResponse: LiveData<Response<Unit>> = _toolPurchaseResponse

    // Functions to set document IDs
    fun setChildId(id: String) { childId = id }
    fun setHouseId(id: String) { houseId = id }

    // Fetch house data from Firestore
    fun getHouseFromFirebase() {
        _house.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("houses")
                    .document(houseId)
                    .get()
                    .await()

                // Convert document to House object
                val house = snapshot.toObject<House>()!!

                _house.postValue(Response.Success(house))

            } catch (e: Exception) {
                e.message?.let { _house.postValue(Response.Failure(it)) }
            }
        }
    }

    // Fetch tools data from Firestore
    // where isForSale value is true
    fun getToolsForSaleFromFirebase() {
        _tools.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("tools")
                    .whereEqualTo("isForSale", true)
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

    // Fetch cash data in child document from Firestore
    fun getChildCashFromFirestore() {
        _childCash.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .get()
                    .await()

                // Get cash data from the document snapshot
                val cash = snapshot.getLong("cash")!!.toInt()

                _childCash.postValue(Response.Success(cash))

            } catch (e: Exception) {
                e.message?.let { _childCash.postValue(Response.Failure(it)) }
            }
        }
    }

    // Use tool to destroy the fort
    // This function subtracts tool price from child cash
    // and tool power from house hp
    fun purchaseTool(toolId: String) {
        _toolPurchaseResponse.postValue(Response.Loading())

        val childDocRef = parentDocRef.collection("children").document(childId)
        val houseDocRef = childDocRef.collection("houses").document(houseId)
        val toolDocRef = childDocRef.collection("tools").document(toolId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runTransaction { transaction ->
                    val childSnapshot = transaction.get(childDocRef)
                    val houseSnapshot = transaction.get(houseDocRef)
                    val toolSnapshot = transaction.get(toolDocRef)

                    // Calculate new value of cash after subtraction
                    val cash = childSnapshot.getLong("cash")!! - toolSnapshot.getLong("price")!!
                    if (cash < 0) {
                        _toolPurchaseResponse.postValue(Response.Failure("cash"))
                        return@runTransaction
                    }

                    // Calculate value of health points data after subtraction
                    val hp = houseSnapshot.getLong("hp")!! - toolSnapshot.getLong("power")!!

                    // Update child and house documents
                    transaction.update(childDocRef, "cash", cash)
                    transaction.update(houseDocRef, "hp", hp)
                }.await()

                // Check if there is any failure in completing transaction
                if (_toolPurchaseResponse.value is Response.Failure) {
                    return@launch
                } else {
                    _toolPurchaseResponse.postValue(Response.Success(Unit))
                }

            } catch (e: Exception) {
                e.message?.let { _toolPurchaseResponse.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set tool name LiveData value to null
    fun toolPurchaseResultResponseHandled() {
        _toolPurchaseResponse.value = null
    }
}