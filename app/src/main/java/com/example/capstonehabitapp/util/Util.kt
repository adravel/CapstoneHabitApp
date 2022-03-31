package com.example.capstonehabitapp.util

import android.util.Log
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