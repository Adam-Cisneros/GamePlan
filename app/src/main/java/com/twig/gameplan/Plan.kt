package com.twig.gameplan

import com.google.firebase.firestore.DocumentId

data class Plan(
    @DocumentId val id: String = "",
    val uid: String = "", // User ID for filtering
    val groupId: String? = null, // Link to Group
    val title: String = "",
    val body: String? = null,
    val milestones: List<String> = emptyList(),
    val sprintLength: Int = 2,
    val completed: Boolean = false
)
