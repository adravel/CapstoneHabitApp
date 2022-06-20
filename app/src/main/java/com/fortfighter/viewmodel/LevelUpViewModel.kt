package com.fortfighter.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fortfighter.util.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class LevelUpViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val parentId = auth.currentUser!!.uid
    private val parentDocRef = db.collection("parents").document(parentId)

    private val _cashUpdateResponse: MutableLiveData<Response<Unit>> = MutableLiveData()
    val cashUpdateResponse: LiveData<Response<Unit>> = _cashUpdateResponse

    // Calculate level up bonus
    fun getBonus(level: Int): Int {
        return level * 5 * 10
    }

    // Update the cash field and reset the level up flag value to false in Firestore child document
    fun updateChildCashWithBonus(childId: String, bonusCash: Int) {
        _cashUpdateResponse.postValue(Response.Loading())

        val childDocRef = parentDocRef.collection("children").document(childId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.runTransaction { transaction ->
                    // Call Firestore get() method to query child data
                    val snapshot = transaction.get(childDocRef)

                    // Get the sum value of cash data in Firestore and the bonus
                    val cash = snapshot.getLong("cash")!!.toInt() + bonusCash

                    val childUpdates = hashMapOf<String, Any>(
                        "cash" to cash,
                        "hasLeveledUp" to false
                    )

                    // Update the child document
                    transaction.update(childDocRef, childUpdates)
                }.await()

                _cashUpdateResponse.postValue(Response.Success(Unit))

            } catch (e: Exception) {
                e.message?.let { _cashUpdateResponse.postValue(Response.Failure(it)) }
            }
        }
    }
}