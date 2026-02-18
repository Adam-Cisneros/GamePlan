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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
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
    data class TaskDetail(val taskId: String)

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
    val navController = rememberNavController()
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
                imageVector = Icons.Default.Add
            )
        },
        bottomBar = { BottomNav(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Plans, // Your starting screen
            modifier = Modifier.padding(innerPadding) // Apply padding from the Scaffold
        ) {
            composable<Routes.Plans> {
                PlanScreen(
                    model = model,
                    onSelectPlan = { plan ->
                        navController.navigate(Routes.PlanDetail(plan.id.toString()))
                    }
                )
            }

            composable<Routes.ToDo> {
                ToDoScreen(
                    model = model,
                    onSelectTask = { task ->
                        navController.navigate(Routes.TaskDetail(task.id.toString()))
                    }
                )
            }

            composable<Routes.PlanDetail> { backStackEntry ->
                val planDetail: Routes.PlanDetail = backStackEntry.toRoute()
                PlanDetail(planId = planDetail.planId)
            }

            composable<Routes.TaskDetail> { backStackEntry ->
                val taskDetail: Routes.TaskDetail = backStackEntry.toRoute()
                TaskDetail(taskId = taskDetail.taskId)
            }

            composable<Routes.Group> {
                GroupScreen(
                    model = model,
                    onSelectGroup = { group ->
                        navController.navigate(Routes.GroupDetail(group.id.toString()))
                    }
                )
            }

            composable<Routes.GroupDetail> { backStackEntry ->
                val groupDetail: Routes.GroupDetail = backStackEntry.toRoute()
                GroupDetail(groupId = groupDetail.groupId)
            }
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
}

@Composable
fun TSFloatingActionButton(onClick: () -> Unit, imageVector: ImageVector) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Add Task"
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