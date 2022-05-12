package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _user: MutableLiveData<Response<Int>> = MutableLiveData()
    val user: LiveData<Response<Int>> = _user

    // Create a new user in Firebase Auth
    fun registerUser(email: String, password: String, name: String, isMale: Boolean) {
        _user.postValue(Response.Loading())

        var authSuccess = false
        var dbWriteSuccess = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new user
                auth.createUserWithEmailAndPassword(email, password).await()
                authSuccess = true

                // Add new parent document in Firestore
                // with current user UID as document ID
                val data = hashMapOf(
                    "name" to name,
                    "isMale" to isMale
                )
                val parentId = auth.currentUser!!.uid
                db.collection("parents").document(parentId).set(data).await()
                dbWriteSuccess = true

                _user.postValue(Response.Success(1))

            } catch(e: Exception) {
                e.message?.let { _user.postValue(Response.Failure(it)) }

                // Delete user account and logout
                // if the query to create user Firebase Auth is successful
                // but the query to add parent document in Firestore is not
                if (authSuccess && !dbWriteSuccess) {
                    auth.currentUser!!.delete()
                    auth.signOut()
                }
            }
        }
    }
}