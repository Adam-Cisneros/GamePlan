package com.twig.gameplan

var lastGroupId = 0

data class Group(
    val id: Int = lastGroupId++,
    val name: String = "",
    val plans: List<Plan> = listOf()
)