package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.model.Child
import com.fortfighter.model.House
import com.fortfighter.model.Tool
import com.fortfighter.util.Response
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

    // Flags for showing dialog
    var showHouseRescueIntroDialog = true
    var showHouseCareIntroDialog = true
    var showHouseCareSuccessDialog = true
    var showPunishmentDialog = true

    private val _house: MutableLiveData<Response<House>> = MutableLiveData()
    private val _tools: MutableLiveData<Response<List<Tool>>> = MutableLiveData()
    private val _child: MutableLiveData<Response<Child>> = MutableLiveData()
    private val _toolPurchaseResponse: MutableLiveData<Response<Int>> = MutableLiveData()
    private val _punishmentAcceptResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val house: LiveData<Response<House>> = _house
    val tools: LiveData<Response<List<Tool>>> = _tools
    val child: LiveData<Response<Child>> = _child
    val toolPurchaseResponse: LiveData<Response<Int>> = _toolPurchaseResponse
    val punishmentAcceptResponse: LiveData<Response<Unit>> = _punishmentAcceptResponse

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

    // Fetch child data in child document from Firestore
    fun getChildFromFirestore() {
        _child.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                    val child = childSnapshot.toObject<Child>()!!
                    val cash = child.cash.toInt() - toolPrice
                    if (cash < 0) {
                        _toolPurchaseResponse.postValue(Response.Failure("NOT_ENOUGH_CASH_ERROR"))
                        return@runTransaction
                    }

                    // Update cash field in child document
                    transaction.update(childDocRef, "cash", cash)

                    // Update the ID of the House that
                    // the child is currently working on
                    // in the child document to determine
                    // the targeted House for punishment
                    if (child.currentHouseId != houseId) {
                        transaction.update(childDocRef, "currentHouseId", houseId)
                    }

                    // Check the status of House and
                    // calculate value of HP/CP data after subtraction/addition
                    val house = houseSnapshot.toObject<House>()!!
                    val houseStatus = house.status.toInt()
                    val houseRepairCount = house.repairCount.toInt()
                    val houseCleanCount = house.cleanCount.toInt()
                    when (houseStatus) {
                        // User is destroying the fort
                        1 -> {
                            // Calculate HP
                            var hp = house.hp.toInt() - toolPower

                            // Update house status if HP becomes less than or equal to 0
                            if (hp <= 0) {
                                hp = 0
                                transaction.update(houseDocRef, "status", 2)
                            }

                            // Update HP field in house document
                            transaction.update(houseDocRef, "hp", hp)
                        }
                        // User is taking care of the house
                        2 -> {
                            // User is taking care of the house
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

                            // Calculate CP
                            var cp = house.cp.toInt() + toolPower
                            val maxCP = house.getHouseStaticData()!!.maxCP

                            // Update house status if CP becomes more than or equal to Max CP
                            // and set the currentHouseId field in child document to null
                            if (cp >= maxCP) {
                                 cp = maxCP
                                transaction.update(houseDocRef, "status", 3)
                                transaction.update(childDocRef, "currentHouseId", null)
                            }

                            // Update CP field in house document
                            transaction.update(houseDocRef, "cp", cp)
                        }
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

    // Update isPunished field to false in child document in Firestore
    // The user has acknowledged the punishment so the dialog fragment
    // will not be displayed again
    fun acceptPunishment() {
        _punishmentAcceptResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                parentDocRef
                    .collection("children")
                    .document(childId)
                    .update("isPunished", false)
                    .await()

                _punishmentAcceptResponse.postValue(Response.Success(Unit))

            } catch (e: Exception) {
                e.message?.let { _punishmentAcceptResponse.postValue(Response.Failure(it)) }
            }
        }
    }

    // Set tool name LiveData value to null
    fun toolPurchaseResultResponseHandled() {
        _toolPurchaseResponse.value = null
    }
}