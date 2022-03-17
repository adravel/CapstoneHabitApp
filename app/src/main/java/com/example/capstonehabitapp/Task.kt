package com.example.capstonehabitapp

import java.util.*

data class Task(
    var title: String = "",
    var category: String = "",
    var difficulty: Long = -1,
    var area: String = "",
    var startTimeLimit: String = "",
    var finishTimeLimit: String = "",
    var status: Long = -1,
    var detail: String = "",
    var gradePoints: Long = -1,
    var notes: String = "",
    var createTime: Date? = null,
    var startTime: Date? = null,
    var finishTime: Date? = null,
    var photoUri: String = ""
)
