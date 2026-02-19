package com.twig.gameplan

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object ToDo

    @Serializable
    data object Plans

    @Serializable
    data class PlanDetail(val planId: String)

    @Serializable
    data object Group

    @Serializable
    data class GroupDetail(val groupId: String)
}

// Make composable for Main App module
@Composable
fun GamePlanApp(
    model: GamePlanViewModel = viewModel<GamePlanViewModel>()
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showPlanDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showGroupDialog by rememberSaveable { mutableStateOf(false) }
    var taskToEdit by rememberSaveable { mutableStateOf<Task?>(null) }
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Plans, // Your starting screen
    ) {
        composable<Routes.Plans> {
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = false,
                        canDeleteTasks = model.completedTasksExist
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = { showPlanDialog = true },
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Plan"
                    )
                },
                bottomBar = { BottomNav(navController = navController) }
            ) { innerPadding ->
                PlanScreen(
                    model = model,
                    onSelectPlan = { plan ->
                        navController.navigate(Routes.PlanDetail(plan.id.toString()))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable<Routes.ToDo> {
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = false,
                        canDeleteTasks = model.completedTasksExist
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = {
                            taskToEdit = null
                            showTaskDialog = true
                        },
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                },
                bottomBar = { BottomNav(navController = navController) }
            ) { innerPadding ->
                ToDoScreen(
                    model = model,
                    onSelectTask = { task ->
                        taskToEdit = task
                        showTaskDialog = true
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable<Routes.PlanDetail> { backStackEntry ->
            val planDetail: Routes.PlanDetail = backStackEntry.toRoute()
            PlanDetail(planId = planDetail.planId)
        }

        composable<Routes.Group> {
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = false,
                        canDeleteTasks = model.completedTasksExist
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = { showGroupDialog = true },
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Group"
                    )
                },
                bottomBar = { BottomNav(navController = navController) }
            ) { innerPadding ->
                GroupScreen(
                    model = model,
                    onSelectGroup = { group ->
                        navController.navigate(Routes.GroupDetail(group.id.toString()))
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable<Routes.GroupDetail> { backStackEntry ->
            val groupDetail: Routes.GroupDetail = backStackEntry.toRoute()
            GroupDetail(groupId = groupDetail.groupId)
        }
    }


    if (showConfirmationDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                model.deleteCompletedTasks()
                showConfirmationDialog = false
            },
            onDismiss = {
                showConfirmationDialog = false
            }
        )
    }

    if (showPlanDialog) {
        AddPlanDialog(
            onDismiss = { showPlanDialog = false },
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }

    if (showTaskDialog) {
        AddTaskDialog(
            taskToEdit = taskToEdit,
            onDismiss = { showTaskDialog = false },
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }

    if (showGroupDialog) {
        AddGroupDialog(
            onDismiss = { showGroupDialog = false },
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}

@Composable
fun TSFloatingActionButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Tasks") },
        text = { Text("Are you sure you want to delete all completed tasks?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No")
            }
        }
    )
}
