package com.example.capstonehabitapp

data class Task(
    var title: String = "",
    var area: String = "",
    var startTimeLimit: String = "",
    var finishTimeLimit: String = "",
    var status: Long = -1
)