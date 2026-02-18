package com.twig.gameplan

import java.util.Date

var lastPlanId = 0

data class Plan(
    val id: Int = lastPlanId++,
    val title: String = "",
    val body: String? = null,
    val due: Date? = null,
    val completed: Boolean = false,
    val tags: List<String> = listOf(),
    val tasks: List<Task> = listOf()
)