package com.twig.gameplan

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Plan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long? = null, // Link to Plan
    val title: String,
    val due: Date? = null,
    val body: String? = null,
    val stage: String = "To Do",
    val completed: Boolean = false,
    val milestoneTitle: String? = null // Store milestone as string for simplicity
)
