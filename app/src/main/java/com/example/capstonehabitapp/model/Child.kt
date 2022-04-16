package com.example.capstonehabitapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Child(
    @DocumentId
    var id: String = "",
    var name: String = "",
    @field:JvmField
    var isMale: Boolean = true,
    var totalPoints: Long = 0,
    var currentPoints: Long = 0,
    var level: Long = 0,
    var badge: Long = 0,
    var timeCreated: Timestamp? = null
)