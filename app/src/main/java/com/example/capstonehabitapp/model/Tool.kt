package com.example.capstonehabitapp.model

import com.google.firebase.firestore.DocumentId

data class Tool(
    @DocumentId val id: String = "",
    val toolTypeId: Long = 0,
    @field:JvmField val isCrushingTool: Boolean = true,
    @field:JvmField val isForSale: Boolean = true,
    val name: String = "",
    val power: Long = 0,
    val price: Long = 0,
    val assetImage: String = ""
)