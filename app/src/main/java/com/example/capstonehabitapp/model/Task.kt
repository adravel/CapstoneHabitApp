package com.example.capstonehabitapp.model

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Task(
    @DocumentId
    var id: String = "",
    var childId: String = "",
    var childName: String = "",
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
    var timeCreated: Date? = null,
    var timeStartWorking: Date? = null,
    var timeFinishWorking: Date? = null,
    var timeAskForGrading: Date? = null,
    var photoUri: String = ""
)
