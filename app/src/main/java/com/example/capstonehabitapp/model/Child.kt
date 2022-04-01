package com.example.capstonehabitapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.*

data class Child(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var age: Long = -1,
    var totalPoints: Long = -1,
    var currentPoints: Long = -1,
    var level: Long = -1,
    var badge: Long = -1,
    var timeCreated: Timestamp? = null
)