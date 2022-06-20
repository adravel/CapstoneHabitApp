package com.fortfighter.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Child(
    @DocumentId
    var id: String = "",
    var name: String = "",
    @field:JvmField
    var isMale: Boolean = true,
    var totalPoints: Long = 0,
    var cash: Long = 0,
    var level: Long = 0,
    var hasLeveledUp: Boolean = false,
    var badge: Long = 0,
    var timeCreated: Timestamp? = null
)