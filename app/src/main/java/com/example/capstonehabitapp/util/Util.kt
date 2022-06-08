package com.example.capstonehabitapp.util

import android.content.Context
import android.util.Log
import com.example.capstonehabitapp.R
import com.google.firebase.Timestamp
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

// Convert Firebase Timestamp to String
fun getDateString(timestamp: Timestamp): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp.seconds * 1000
        sdf.format(calendar.time)
    } catch (e: Exception) {
        Log.e("Util", "Date format error", e)
        "Error"
    }
}

// Get the name of the level in the form of a String
fun getLevelNameString(context: Context, level: Int): String {
    val stringResId = when (level) {
        1 -> R.string.level_1_name
        2 -> R.string.level_2_name
        3 -> R.string.level_3_name
        4 -> R.string.level_4_name
        5 -> R.string.level_5_name
        6 -> R.string.level_6_name
        7 -> R.string.level_7_name
        8 -> R.string.level_8_name
        9 -> R.string.level_9_name
        else -> R.string.level_10_name
    }
    return context.getString(stringResId)
}