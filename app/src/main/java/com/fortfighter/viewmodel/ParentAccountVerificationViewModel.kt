package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.util.Response
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ParentAccountVerificationViewModel: ViewModel() {
    private val auth = Firebase.auth

    private val _reauthenticationResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val reauthenticationResponse: LiveData<Response<Unit>> = _reauthenticationResponse

    // Reauthenticate the user by providing the password
    // to ensure that the one who currently use the app is the parent
    fun reauthenticateUser(password: String) {
        _reauthenticationResponse.postValue(Response.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = auth.currentUser!!
                val email = user.email!!

                // Get auth credentials from the user for reauthentication
                val credential = EmailAuthProvider.getCredential(email, password)

                // Reauthenticate the user
                user.reauthenticate(credential).await()

                _reauthenticationResponse.postValue(Response.Success(Unit))

            } catch(e: Exception) {
                e.message?.let { _reauthenticationResponse.postValue(Response.Failure(it)) }
            }
        }
    }
}