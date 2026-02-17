package com.twig.gameplan

import java.util.Date

var lastTaskId = 0

data class Task(
    val id: Int = lastTaskId++,
    val title: String = "",
    val body: String? = null,
    val due: Date? = null,
    val completed: Boolean = false,
)