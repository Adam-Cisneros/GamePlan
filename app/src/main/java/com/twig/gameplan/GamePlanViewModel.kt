package com.twig.gameplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class GamePlanViewModel(
    private val taskDao: TaskDao,
    private val planDao: PlanDao,
    private val groupDao: GroupDao
) : ViewModel() {

    // Database Flows - Observe these in your Composables
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allPlans: Flow<List<Plan>> = planDao.getAllPlans()
    val allGroups: Flow<List<Group>> = groupDao.getAllGroups()

    val completedTasksExist: Flow<Boolean> = allTasks.map { tasks -> tasks.any { it.completed } }
    val completedPlansExist: Flow<Boolean> = allPlans.map { plans -> plans.any { it.completed } }

    val stageList = listOf("To Do", "In Progress", "In Review", "Done")

    // --- Init Operations ---
    init {
        clearDatabase()
    }

    fun clearDatabase() {
        viewModelScope.launch {
            taskDao.deleteAllTasks()
            planDao.deleteAllPlans()
            groupDao.deleteAllGroups()
        }
    }


    // --- Task Operations ---
    fun getTasksByPlan(planId: Long): Flow<List<Task>> {
        return taskDao.getTasksByPlan(planId)
    }

    fun addTask(task: Task) {
        viewModelScope.launch { taskDao.insertTask(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { taskDao.updateTask(task) }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            allTasks.collect { tasks ->
                tasks.filter { it.completed }.forEach { task ->
                    taskDao.deleteTask(task)
                }
            }
        }
    }

    fun moveTaskStage(task: Task, direction: Int) {
        val currentIndex = stageList.indexOf(task.stage)
        // If currentIndex is -1, the stage wasn't found in the list
        if (currentIndex == -1) return

        val newIndex = currentIndex + direction

        viewModelScope.launch {
            if (newIndex >= stageList.size) {
                taskDao.deleteTask(task)
            } else if (newIndex >= 0) {
                taskDao.updateTask(task.copy(stage = stageList[newIndex]))
            }
        }
    }

    // --- Plan Operations ---
    fun getPlanById(planId: Long): Flow<Plan?> {
        return planDao.getPlanById(planId)
    }

    fun getPlansByGroup(groupId: Long): Flow<List<Plan>> {
        return planDao.getPlansByGroup(groupId)
    }

    fun addPlan(plan: Plan) {
        viewModelScope.launch { planDao.insertPlan(plan) }
    }

    fun togglePlanCompleted(plan: Plan) {
        viewModelScope.launch {
            planDao.updatePlan(plan.copy(completed = !plan.completed))
        }
    }

    fun deletePlan(plan: Plan) {
        viewModelScope.launch { planDao.deletePlan(plan) }
    }

    // --- Group Operations ---
    fun getGroupById(groupId: Long): Flow<Group?> {
        return groupDao.getGroupById(groupId)
    }

    fun addGroup(group: Group) {
        viewModelScope.launch { groupDao.insertGroup(group) }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch { groupDao.deleteGroup(group) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])

                // Initialize your database (assuming it's named AppDatabase)
                val database = androidx.room.Room.databaseBuilder(
                    application.applicationContext,
                    AppDatabase::class.java,
                    "gameplan_db"
                ).build()

                return GamePlanViewModel(
                    database.taskDao(),
                    database.planDao(),
                    database.groupDao()
                ) as T
            }
        }
    }
}