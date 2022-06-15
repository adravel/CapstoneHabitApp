package com.example.capstonehabitapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Task(
    @DocumentId
    var id: String = "",
    var childId: String = "",
    var childName: String = "",
    var title: String = "",
    var category: String = "",
    var difficulty: Long = 0,
    var area: String = "",
    var startTimeLimit: String = "",
    var finishTimeLimit: String = "",
    var status: Long = 0,
    var detail: String = "",
    var grade: Long = 0,
    var notes: String = "",
    var timeCreated: Timestamp? = null,
    var timeStartWorking: Timestamp? = null,
    var timeFinishWorking: Timestamp? = null,
    var timeAskForGrading: Timestamp? = null,
    var photoUri: String = ""
)
