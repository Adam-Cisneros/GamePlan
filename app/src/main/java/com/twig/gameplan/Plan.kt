package com.twig.gameplan

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "plans",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Plan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long? = null, // Link to Group
    val title: String,
    val body: String? = null,
    val milestones: List<String> = emptyList(),
    val sprintLength: Int = 2,
    val completed: Boolean = false
)
