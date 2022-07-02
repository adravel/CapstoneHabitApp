package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel: ViewModel() {
    private val auth = Firebase.auth

    private val _loginResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val loginResponse: LiveData<Response<Unit>> = _loginResponse

    // Log user into Firebase Auth
    fun loginUser(email: String, password: String) {
        _loginResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new user
                auth.signInWithEmailAndPassword(email, password).await()

                _loginResponse.postValue(Response.Success(Unit))

            } catch(e: Exception) {
                e.message?.let { _loginResponse.postValue(Response.Failure(it)) }
            }
        }
    }
}