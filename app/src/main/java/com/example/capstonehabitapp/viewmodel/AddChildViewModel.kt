package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    // Create new child document in Firestore
    fun addChildToFirebase(name: String, isMale: Boolean) {
        _childResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val child = hashMapOf(
                    "name" to name,
                    "isMale" to isMale,
                    "totalPoints" to 0,
                    "cash" to 0,
                    "level" to 1,
                    "badge" to 0,
                    "timeCreated" to FieldValue.serverTimestamp()
                )

                // Add new child document to Firestore "children" collection
                parentDocRef.collection("children").add(child).await()

                _childResponse.postValue(Response.Success(1))

            } catch(e: Exception) {
                e.message?.let { _childResponse.postValue(Response.Failure(it)) }
            }
        }
    }
}