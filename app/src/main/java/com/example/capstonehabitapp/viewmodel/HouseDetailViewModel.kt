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

    var showHouseRescueIntroDialog = true
    var showHouseCareIntroDialog = true

    private val _house: MutableLiveData<Response<House>> = MutableLiveData()
    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    private val _childCash: MutableLiveData<Response<Int>> = MutableLiveData()
    private val _toolPurchaseResponse: MutableLiveData<Response<Int>> = MutableLiveData()
    val house: LiveData<Response<House>> = _house
    val tools: LiveData<Response<List<Tool>>> = _tools
    val childCash: LiveData<Response<Int>> = _childCash
    val toolPurchaseResponse: LiveData<Response<Int>> = _toolPurchaseResponse

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

    // Use tool to destroy the fort or take card of the house
    // Calculates child cash and house CP/HP
    // Change house status if the requirement is fulfilled
    fun purchaseTool(toolId: String) {
        _toolPurchaseResponse.postValue(Response.Loading())

        val childDocRef = parentDocRef.collection("children").document(childId)
        val houseDocRef = childDocRef.collection("houses").document(houseId)
        val toolDocRef = childDocRef.collection("tools").document(toolId)

        var toolType = 0

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runTransaction { transaction ->
                    val childSnapshot = transaction.get(childDocRef)
                    val houseSnapshot = transaction.get(houseDocRef)
                    val toolSnapshot = transaction.get(toolDocRef)

                    // Obtain tool price and power data
                    val tool = toolSnapshot.toObject<Tool>()!!
                    val toolStaticData = tool.getToolStaticData()!!
                    val toolPrice = toolStaticData.price
                    val toolPower = toolStaticData.power

                    // Set tool type Int value
                    toolType = tool.type.toInt()

                    // Calculate new value of cash after subtraction
                    val cash = childSnapshot.getLong("cash")!!.toInt() - toolPrice
                    if (cash < 0) {
                        _toolPurchaseResponse.postValue(Response.Failure("NOT_ENOUGH_CASH_ERROR"))
                        return@runTransaction
                    }

                    // Update child document
                    transaction.update(childDocRef, "cash", cash)

                    // Check the status of House and
                    // calculate value of HP/CP data after subtraction/addition
                    val houseStatus = houseSnapshot.getLong("status")!!.toInt()
                    val houseRepairCount = houseSnapshot.getLong("repairCount")!!.toInt()
                    val houseCleanCount = houseSnapshot.getLong("cleanCount")!!.toInt()
                    if (houseStatus == 1) {
                        // User is destroying the fort
                        // Calculate HP
                        var hp = houseSnapshot.getLong("hp")!!.toInt() - toolPower

                        // Update house status if HP becomes less than or equal to 0
                        if (hp <= 0) {
                            hp = 0
                            transaction.update(houseDocRef, "status", 2)
                        }

                        // Update HP field in house document
                        transaction.update(houseDocRef, "hp", hp)

                    } else if (houseStatus == 2) {
                        // User is taking care of the house
                        // Calculate CP
                        val cp = houseSnapshot.getLong("cp")!!.toInt() + toolPower

                        // Handle tools usage count data
                        if (toolType == 2) {
                            // Tool is "Broom"
                            if (houseCleanCount < 2) {
                                transaction.update(houseDocRef, "cleanCount", houseCleanCount + 1)
                            } else {
                                _toolPurchaseResponse.postValue(Response.Failure("MAX_CLEAN_COUNT_REACHED"))
                                return@runTransaction
                            }
                        } else if (toolType == 3) {
                            // Tool is "Hammer"
                            if (houseRepairCount < 8) {
                                transaction.update(houseDocRef, "repairCount", houseRepairCount + 1)
                            } else {
                                _toolPurchaseResponse.postValue(Response.Failure("MAX_REPAIR_COUNT_REACHED"))
                                return@runTransaction
                            }
                        }

                        // Update CP field in house document
                        transaction.update(houseDocRef, "cp", cp)
                    }
                }.await()

                // Check if there is any failure in completing transaction
                if (_toolPurchaseResponse.value is Response.Failure) {
                    return@launch
                } else {
                    _toolPurchaseResponse.postValue(Response.Success(toolType))
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