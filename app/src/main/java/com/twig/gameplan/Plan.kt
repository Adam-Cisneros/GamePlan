package com.twig.gameplan

var lastPlanId = 0

data class Plan(
    val id: Int = lastPlanId++,
    val title: String = "",
    val body: String? = null,
    val completed: Boolean = false,
    val group: Group? = null,
    val milestones: List<Milestone> = listOf(),
    val sprintLength: Int = 1,
)

data class Milestone(
    val title: String,
    val tasks: List<Task> = listOf() // Milestones contain tasks
)
