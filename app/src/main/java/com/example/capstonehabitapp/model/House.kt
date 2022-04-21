package com.example.capstonehabitapp.model

import com.google.firebase.firestore.DocumentId

data class House(
    @DocumentId val id: String = "",
    val houseTypeId: Long = 0,
    val status: Long = 0,
    val hp: Long = 0,
    val name: String = "",
    val island: String = "",
    val description: String = "",
    val assetImage: String = ""
)
