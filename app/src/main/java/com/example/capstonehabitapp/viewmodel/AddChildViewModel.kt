package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.House
import com.example.capstonehabitapp.model.Tool
import com.example.capstonehabitapp.util.Response
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

    private val _childResponse: MutableLiveData<Response<Int>> = MutableLiveData()
    val childResponse: LiveData<Response<Int>> = _childResponse

    // Create new child, tool, and house documents simultaneously
    // using Firestore batch writes
    fun addChildToFirebase(name: String, isMale: Boolean) {
        _childResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runBatch { batch ->
                    val newChild = hashMapOf(
                        "name" to name,
                        "isMale" to isMale,
                        "totalPoints" to 0,
                        "cash" to 0,
                        "level" to 1,
                        "hasLeveledUp" to false,
                        "badge" to 0,
                        "timeCreated" to FieldValue.serverTimestamp()
                    )

                    // Create a new child document
                    val newChildDocRef = parentDocRef.collection("children").document()
                    batch.set(newChildDocRef, newChild)

                    // Add the house documents
                    for (newHouse in newHouses) {
                        val newHouseDocRef = newChildDocRef.collection("houses").document()
                        batch.set(newHouseDocRef, newHouse)
                    }

                    // Add the tool documents
                    for (newTool in newTools) {
                        val newToolDocRef = newChildDocRef.collection("tools").document()
                        batch.set(newToolDocRef, newTool)
                    }
                }.await()

                _childResponse.postValue(Response.Success(1))

            } catch(e: Exception) {
                e.message?.let { _childResponse.postValue(Response.Failure(it)) }
            }
        }
    }

    // List of house data to be added to Firestore when creating a new child document
    private val newHouses = listOf(
        House(
            "",
            1,
            1,
            150,
            "Bangsal Kencono",
            "Jawa",
            "",
            150,
            ""
        ),
        House(
            "",
            2,
            0,
            250,
            "Bubung Lima",
            "Sumatra",
            "",
            250,
            ""
        )
    )

    // List of tool data to be added to Firestore when creating a new child document
    private val newTools = listOf(
        Tool(
            "",
            1,
            true,
            false,
            "Meriam",
            7,
            150,
            ""
        ),
        Tool(
            "",
            2,
            true,
            false,
            "Bom",
            10,
            200,
            ""
        )
    )
}