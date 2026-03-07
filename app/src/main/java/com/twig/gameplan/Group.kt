package com.twig.gameplan

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId val id: String = "",
    val uid: String = "", // User ID for filtering
    val title: String = "",
    val description: String? = null,
)