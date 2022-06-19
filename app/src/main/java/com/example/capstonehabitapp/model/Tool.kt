package com.example.capstonehabitapp.model

import androidx.annotation.DrawableRes
import com.example.capstonehabitapp.R
import com.google.firebase.firestore.DocumentId

class Tool(
    @DocumentId val id: String = "",
    val type: Long = 0,
    @field:JvmField val isForSale: Boolean = false
) {
    // Get tool data depending on its type
    fun getToolStaticData() = mapOf(
        0 to ToolStaticData("Meriam", 70, 70, R.drawable.img_tool_cannon, true),
        1 to ToolStaticData("Bom", 60, 60, R.drawable.img_tool_bomb, true),
        2 to ToolStaticData("Sapu", 20, 20, R.drawable.img_tool_broom, false),
        3 to ToolStaticData("Palu", 70, 60, R.drawable.img_tool_hammer, false)
    )[type.toInt()]
}

data class ToolStaticData(
    val name: String,
    val power: Int,
    val price: Int,
    @DrawableRes val imageResId: Int,
    val isCrushingTool: Boolean
)