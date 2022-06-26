package com.fortfighter.util

import android.content.Context
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.text.color
import com.fortfighter.R
import com.google.firebase.Timestamp
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

// Convert Firebase Timestamp to String
fun convertTimestampToString(timestamp: Timestamp, format: String): String {
    return try {
        val sdf = SimpleDateFormat(format, Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp.seconds * 1000
        sdf.format(calendar.time)
    } catch (e: Exception) {
        Log.e("Util", "Date format error", e)
        "Error"
    }
}

// Get the current date and time data as Date
fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}

// Check if the input String is a valid time in HH:mm format
// Return true if valid
fun validateTimeStringFormat(time: String): Boolean {
    val regex = Regex("^([01][0-9]|2[0-3]):[0-5][0-9]$")
    return time.matches(regex)
}

// Compare two time String, assuming both are valid time in HH:mm format
// Return true if the second parameter is after the first parameter
fun validateTimeLimit(startTimeLimit: String, finishTimeLimit: String): Boolean {
    val sdf = SimpleDateFormat("HH:mm")
    val start = sdf.parse(startTimeLimit)
    val finish = sdf.parse(finishTimeLimit)

    return finish > start
}

// Get a Triple of Integer of day, month, and year from a given Date
fun getDayMonthYearFromDate(date: Date): Triple<Int, Int, Int> {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    return Triple(day, month, year)
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

// Get task difficulty String
fun getTaskDifficultyString(context: Context, difficulty: Int): String {
    val stringResId =  when (difficulty) {
        0 -> R.string.task_difficulty_0
        1 -> R.string.task_difficulty_1
        2 -> R.string.task_difficulty_2
        else -> R.string.task_difficulty_0
    }
    return context.getString(stringResId)
}

// Get resource ID of difficulty icon images
fun getTaskDifficultyImageResId(difficulty: Int): Int {
    return when (difficulty) {
        0 -> R.drawable.img_difficulty_easy
        1 -> R.drawable.img_difficulty_medium
        2 -> R.drawable.img_difficulty_hard
        else -> R.drawable.img_difficulty_easy
    }
}

// String wrapper that return a "-" symbol if text is empty
fun emptyTextWrapper(text: String): String {
    return if (text.trim().isEmpty()) "-" else text
}

// Time limit text wrapper
fun timeLimitTextWrapper(
    context: Context,
    startTimeLimit: String,
    finishTimeLimit: String
): String {
    return if (startTimeLimit.trim().isEmpty() && finishTimeLimit.trim().isEmpty()) {
        context.getString(R.string.no_time_limit)
    } else {
        context.getString(R.string.task_time_limit_placeholder, startTimeLimit, finishTimeLimit)
    }
}

// Text wrapper for the title of a form field that cannot be empty
// Return the title text with a red apostrophe "*" symbol
fun nonEmptiableFieldTitleWrapper(context: Context, title: String): SpannableStringBuilder {
    val red = ContextCompat.getColor(context, R.color.state_error_dark)
    return SpannableStringBuilder()
        .append(title)
        .color(red) { append("*") }
}