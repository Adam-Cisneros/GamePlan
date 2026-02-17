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
    data class PlanDetail(val planId: String) // Add this for plan details

    @Serializable
    data class TaskDetail(val taskId: String) // Add this for task details
}

// Make composable for Main App module
@Composable
fun GamePlanApp(
    model: GamePlanViewModel = viewModel<GamePlanViewModel>()
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var showPlanDialog by rememberSaveable { mutableStateOf(false) }
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Plans
    ) {
        // --- Plan Screen ---
        composable<Routes.Plans> {
            Scaffold(
                // ...(your scaffold setup)
            ) { innerPadding ->
                PlanScreen(
                    model = model,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    // UNCOMMENT THIS BLOCK
                    onSelectPlan = { plan ->
                        navController.navigate(
                            Routes.PlanDetail(plan.id.toString()) // Navigate to the correct route
                        )
                    }
                )
            }
        }

        // --- To Do Screen ---
        composable<Routes.ToDo> {
            Scaffold(
                // ...(your scaffold setup)
            ) { innerPadding ->
                ToDoScreen(
                    model = model,
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    onSelectTask = { task ->
                        navController.navigate(
                            Routes.TaskDetail(task.id.toString()) // Navigate to the correct route
                        )
                    }
                )
            }
        }

        // --- Plan Detail Screen ---
        composable<Routes.PlanDetail> { backStackEntry ->
            // The toRoute() function extracts the type-safe object
            val planDetail: Routes.PlanDetail = backStackEntry.toRoute()
            PlanDetail(planId = planDetail.planId)
        }

        // --- Task Detail Screen ---
        composable<Routes.TaskDetail> { backStackEntry ->
            // The toRoute() function extracts the type-safe object
            val taskDetail: Routes.TaskDetail = backStackEntry.toRoute()
            TaskDetail(taskId = taskDetail.taskId)
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