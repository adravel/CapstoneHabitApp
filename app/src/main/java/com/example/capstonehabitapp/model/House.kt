package com.example.capstonehabitapp.model

import androidx.annotation.DrawableRes
import com.example.capstonehabitapp.R
import com.google.firebase.firestore.DocumentId

class House(
    @DocumentId val id: String = "",
    val type: Long = 0,
    val status: Long = 0,
    val hp: Long = 0,
    val cp: Long = 0
) {
    // Get tool data depending on its type
    fun getHouseStaticData() = mapOf(
        0 to HouseStaticData("Bangsal Kencono", "Jawa", "", 240, 600, R.drawable.img_game_house_intact)
    )[type.toInt()]
}

data class HouseStaticData(
    val name: String,
    val island: String,
    val description: String,
    val maxHp: Int,
    val maxCP: Int,
    @DrawableRes val imageResId: Int,
)