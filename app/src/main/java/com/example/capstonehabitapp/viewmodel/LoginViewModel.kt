package com.example.capstonehabitapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.capstonehabitapp.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel: ViewModel() {
    private val auth = Firebase.auth

    private val _user: MutableLiveData<Response<Int>> = MutableLiveData()
    val user: LiveData<Response<Int>> = _user

    // Log user into Firebase Auth
    fun loginUser(email: String, password: String) {
        _user.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new user
                auth.signInWithEmailAndPassword(email, password).await()

                _user.postValue(Response.Success(1))

            } catch(e: Exception) {
                e.message?.let { _user.postValue(Response.Failure(it)) }
            }
        }
    }
}