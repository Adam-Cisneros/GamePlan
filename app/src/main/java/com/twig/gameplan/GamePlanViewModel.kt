package com.twig.gameplan

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.vector.group
import androidx.lifecycle.ViewModel
import com.twig.gameplan.Plan
import java.util.Calendar
import java.util.Date
import kotlin.toString

class GamePlanViewModel : ViewModel() {
    val planList = mutableStateListOf<Plan>()
    val taskList = mutableStateListOf<Task>()
    val groupList = mutableStateListOf<Group>()
    val milestoneList = mutableStateListOf<Milestone>(
        Milestone("Alpha"),
        Milestone("Beta"),
        Milestone("V 1.0"),
    )
    val stageList = mutableStateListOf<String>(
        "To Do",
        "In Progress",
        "In Review",
        "Done"
    )

    init {
        createTestGroups(10)
        createTestPlans(10)
        createTestTasks(10)
    }

    fun findTaskById(id: String) : Task? {
        return taskList.find {it.id.toString() == id }
    }

    fun addTask(task: Task, plan: Plan) {
        taskList.add(0, task)

        val planIndex = planList.indexOf(plan)
        val milestoneIndex = plan.milestones.indexOf(task.milestone)
        if (planIndex != -1 && milestoneIndex != -1) {
            val newTasks = plan.milestones[milestoneIndex].tasks + task // Creates a new list with the new task
            val updatedPlan = plan.copy(milestones = plan.milestones.toMutableList().apply {
                set(milestoneIndex, Milestone(plan.milestones[milestoneIndex].title, newTasks))
            })
            planList[planIndex] = updatedPlan
        }
    }

    fun deleteTask(task: Task) {
        taskList.remove(task)
    }

    val completedTasksExist: Boolean
        get() = taskList.count { it.completed } > 0

    fun deleteCompletedTasks() {
        // Remove only tasks that are completed
        taskList.removeIf { it.completed }
    }

    fun updateTask(task: Task) {
        val index = taskList.indexOf(task)
        taskList[index] = task
    }

    fun toggleTaskCompleted(task: Task) : Task {
        // Observer of MutableList not notified when changing a property, so
        // need to replace element in the list for notification to go through
        val index = taskList.indexOf(task)
        val newTask = task
            .copy(completed = !task.completed)
        taskList[index] = newTask
        return newTask
    }

    fun setDueDate(task: Task, dueDate: Date?) : Task {
        val index = taskList.indexOf(task)
        val newTask = task
            .copy(due = dueDate)
        taskList[index] = newTask
        return newTask
    }

    fun createTestTasks(numTasks: Int = 10) {
        // Add tasks for testing purposes
        for (i in 1..numTasks) {
            val title = "Task $i"
            val body = when (i % 3) {
                0 -> null
                1 -> "description of task $i"
                2 -> (1..5)
                    .joinToString(separator = "\n") {
                        "${i}.${it} subtask"
                    }
                else -> "??"
            }
            val plan = planList[i%planList.size]
            val milestone = milestoneList[i%milestoneList.size]
            val due = if (i%5 == 4) Date() else null

            addTask(Task(title = title, body = body, due = due, plan = plan, milestone = milestone), planList[i%planList.size])
        }
    }

    fun findPlanById(id: String) : Plan? {
        return planList.find {it.id.toString() == id }
    }

    fun addPlan(plan: Plan, group: Group) {
        planList.add(0, plan)

        val groupIndex = groupList.indexOf(group)
        if (groupIndex != -1) {
            val newPlans = group.plans + plan // Creates a new list with the new plan
            val updatedGroup = group.copy(plans = newPlans)
            groupList[groupIndex] = updatedGroup
        }
    }

    fun deletePlan(plan: Plan) {
        planList.remove(plan)
    }

    val completedPlansExist: Boolean
        get() = planList.count { it.completed } > 0

    fun deleteCompletedPlans() {
        // Remove only tasks that are completed
        planList.removeIf { it.completed }
    }


    fun togglePlanCompleted(plan: Plan) : Plan {
        // Observer of MutableList not notified when changing a property, so
        // need to replace element in the list for notification to go through
        val index = planList.indexOf(plan)
        val newPlan = plan
            .copy(completed = !plan.completed)
        planList[index] = newPlan
        return newPlan
    }

    fun createTestPlans(numPlans: Int = 10) {
        // Add plans for testing purposes
        for (i in 1..numPlans) {
            val title = "Plan $i"
            val body = when (i % 2) {
                0 -> null
                1 -> "description of plan $i"
                else -> "??"
            }
            val milestones = milestoneList.slice(0..i%3)

            addPlan(Plan(title = title, body = body, milestones = milestones), groupList[i%groupList.size])
        }
    }

    fun findGroupById(id: String) : Group? {
        return groupList.find {it.id.toString() == id }
    }

    fun addGroup(group: Group) {
        groupList.add(0, group)
    }

    fun deleteGroup(group: Group) {
        groupList.remove(group)
    }

    fun createTestGroups(numGroups: Int = 10) {
        // Add tasks for testing purposes
        for (i in 1..numGroups) {
            val name = "Group $i"

            addGroup(Group(name = name))
        }
    }
}