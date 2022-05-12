package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel: ViewModel() {
    private val auth = Firebase.auth

    private val _user: MutableLiveData<Response<FirebaseUser>> = MutableLiveData()
    val user: LiveData<Response<FirebaseUser>> = _user

    // Create a new user in Firebase
    fun registerUser(email: String, password: String) {
        _user.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new user
                auth.createUserWithEmailAndPassword(email, password).await()
                val newUser = auth.currentUser!!

                _user.postValue(Response.Success(newUser))

            } catch(e: Exception) {
                e.message?.let { _user.postValue(Response.Failure(it)) }
            }
        }
    }
}