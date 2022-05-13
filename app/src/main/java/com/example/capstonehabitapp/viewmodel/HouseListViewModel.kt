package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.model.House
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

class HouseListViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _houses: MutableLiveData<Response<List<House>>> = MutableLiveData()
    val houses: LiveData<Response<List<House>>> = _houses

    // Fetch houses data from Firestore
    fun getHousesFromFirebase(childId: String) {
        _houses.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = parentDocRef
                    .collection("children")
                    .document(childId)
                    .collection("houses")
                    .get()
                    .await()

                // Convert each document into House object and add them to the list
                val houses = mutableListOf<House>()
                for (document in snapshot.documents) {
                    document.toObject<House>()?. let { houses.add(it) }
                }

                _houses.postValue(Response.Success(houses))

            } catch (e: Exception) {
                e.message?.let { _houses.postValue(Response.Failure(it)) }
            }
        }
    }
}