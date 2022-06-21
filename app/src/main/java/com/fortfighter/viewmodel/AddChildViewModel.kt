package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.model.House
import com.fortfighter.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddChildViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _addChildResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val addChildResponse: LiveData<Response<Unit>> = _addChildResponse

    // Create new child, tool, and house documents simultaneously
    // using Firestore batch writes
    fun addChildToFirebase(name: String, isMale: Boolean) {
        _addChildResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runBatch { batch ->
                    // Create a new child document
                    val newChild = hashMapOf(
                        "name" to name,
                        "isMale" to isMale,
                        "totalPoints" to 0,
                        "cash" to 0,
                        "level" to 1,
                        "hasLeveledUp" to false,
                        "didWorkToday" to false,
                        "isPunished" to false,
                        "timeCreated" to FieldValue.serverTimestamp()
                    )
                    val newChildDocRef = parentDocRef.collection("children").document()
                    batch.set(newChildDocRef, newChild)

                    // Add "Bangsal Kencono" house document (House type is 0)
                    val houseStaticData = House(type = 0).getHouseStaticData()!!
                    val houseHp = houseStaticData.maxHp
                    val newHouse = hashMapOf(
                        "type" to 0,
                        "status" to 1,
                        "hp" to houseHp,
                        "cp" to 0,
                        "repairCount" to 0,
                        "cleanCount" to 0
                    )
                    val newHouseDocRef = newChildDocRef.collection("houses").document()
                    batch.set(newHouseDocRef, newHouse)

                    // Add currentHouse field in child document
                    // The value is the ID of his/her type 0 House document
                    batch.update(newChildDocRef, "currentHouseId", newHouseDocRef.id)

                    // Add all 4 type of Tools documents
                    for (type in 0..3) {
                        val newTool = hashMapOf(
                            "type" to type,
                            "isForSale" to false
                        )
                        val newToolDocRef = newChildDocRef.collection("tools").document()
                        batch.set(newToolDocRef, newTool)
                    }
                }.await()

                _addChildResponse.postValue(Response.Success(Unit))

            } catch(e: Exception) {
                e.message?.let { _addChildResponse.postValue(Response.Failure(it)) }
            }
        }
    }
}