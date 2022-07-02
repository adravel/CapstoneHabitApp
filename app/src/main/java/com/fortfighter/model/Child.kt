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
    var level: Long = 1,
    var hasLeveledUp: Boolean = false,
    var didWorkToday: Boolean = false,
    @field:JvmField
    var isPunished: Boolean = false,
    var currentHouseId: String? = null,
    var timeCreated: Timestamp? = null
)