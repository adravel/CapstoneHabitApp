package com.fortfighter.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.fortfighter.R
import com.google.firebase.firestore.DocumentId

class Tool(
    @DocumentId val id: String = "",
    val type: Long = 0,
    @field:JvmField val isForSale: Boolean = false
) {
    // Get tool data depending on its type
    fun getToolStaticData() = mapOf(
        0 to ToolStaticData(
            "Meriam",
            70,
            70,
            true,
            R.drawable.img_tool_cannon,
            R.drawable.gif_cannon,
            R.raw.sfx_cannon_shot,
            1000
        ),
        1 to ToolStaticData(
            "Bom",
            60,
            60,
            true,
            R.drawable.img_tool_bomb,
            R.drawable.gif_bomb,
            R.raw.sfx_explosion,
            1500
        ),
        2 to ToolStaticData(
            "Sapu",
            20,
            20,
            false,
            R.drawable.img_tool_broom,
            R.drawable.gif_broom,
            R.raw.sfx_broom_sweeping,
            0
        ),
        3 to ToolStaticData(
            "Palu",
            70,
            60,
            false,
            R.drawable.img_tool_hammer,
            R.drawable.gif_hammer,
            R.raw.sfx_hammer,
            0
        )
    )[type.toInt()]
}

data class ToolStaticData(
    val name: String,
    val power: Int,
    val price: Int,
    val isCrushingTool: Boolean,
    @DrawableRes val imageResId: Int,
    @DrawableRes val animationResId: Int,
    @RawRes val soundResId: Int,
    val soundDelayInMilliseconds: Long
)