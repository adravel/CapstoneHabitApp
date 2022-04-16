package com.example.capstonehabitapp.model

data class Tool(
    val id: String = "",
    val name: String = "",
    val crushingTool: Boolean = true,
    val sold: Boolean = true,
    val power: Int = 0,
    val price: Int = 0,
    val assetImage: String = ""
)