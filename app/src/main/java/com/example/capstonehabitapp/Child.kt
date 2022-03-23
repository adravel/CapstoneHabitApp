package com.example.capstonehabitapp

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Child(
    @DocumentId
    var id: String = "",
    var name: String = "",
    var isMale: Boolean = true,
    var age: Long = -1,
    var points: Long = -1,
    var level: Long = -1,
    var badge: Long = -1,
    var createTime: Date? = null
)
