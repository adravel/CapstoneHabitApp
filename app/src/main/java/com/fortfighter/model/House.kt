package com.fortfighter.model

import androidx.annotation.DrawableRes
import com.fortfighter.R
import com.google.firebase.firestore.DocumentId

class House(
    @DocumentId val id: String = "",
    val type: Long = 0,
    val status: Long = 0,
    val hp: Long = 0,
    val cp: Long = 0,
    val repairCount: Long = 0,
    val cleanCount: Long = 0
) {
    // Get tool data depending on its type
    fun getHouseStaticData() = mapOf(
        0 to HouseStaticData(
            "Bangsal Kencono",
            "Jawa",
            "",
            240,
            600,
            R.drawable.img_game_house_intact,
            R.drawable.img_game_house_damaged
        )
    )[type.toInt()]
}

data class HouseStaticData(
    val name: String,
    val island: String,
    val description: String,
    val maxHp: Int,
    val maxCP: Int,
    @DrawableRes val houseIntactImageResId: Int,
    @DrawableRes val houseDamagedImageResId: Int
)