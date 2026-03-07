package com.twig.gameplan

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Task(
    @DocumentId val id: String = "",
    val uid: String = "", // User ID for filtering
    val planId: String? = null, // Link to Plan
    val title: String = "",
    val due: Date? = null,
    val body: String? = null,
    val stage: String = "To Do",
    val milestoneTitle: String? = null
)
