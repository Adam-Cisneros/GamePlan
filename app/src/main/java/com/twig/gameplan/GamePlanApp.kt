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
import androidx.compose.runtime.collectAsState
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
import com.google.firebase.auth.FirebaseAuth
import com.twig.gameplan.data.Plan
import com.twig.gameplan.data.Task
import com.twig.gameplan.dialogues.AddGroupDialog
import com.twig.gameplan.dialogues.AddPlanDialog
import com.twig.gameplan.dialogues.AddTaskDialog
import com.twig.gameplan.nav.BottomNav
import com.twig.gameplan.nav.GamePlanTopBar
import com.twig.gameplan.screens.AuthScreen
import com.twig.gameplan.screens.GroupDetail
import com.twig.gameplan.screens.GroupScreen
import com.twig.gameplan.screens.PlanDetail
import com.twig.gameplan.screens.PlanScreen
import com.twig.gameplan.screens.ToDoScreen
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object Auth

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
    model: GamePlanViewModel = viewModel(factory = GamePlanViewModel.Factory)
) {
    var showPlanDialog by rememberSaveable { mutableStateOf(false) }
    var showTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showGroupDialog by rememberSaveable { mutableStateOf(false) }
    var taskToEdit by rememberSaveable { mutableStateOf<Task?>(null) }
    var planToEdit by rememberSaveable { mutableStateOf<Plan?>(null) }
    val navController = rememberNavController()
    
    val currentUser = FirebaseAuth.getInstance().currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) Routes.Auth else Routes.Plans,
    ) {
        composable<Routes.Auth> {
            AuthScreen(onAuthSuccess = {
                navController.navigate(Routes.Plans) {
                    popUpTo(Routes.Auth) { inclusive = true }
                }
            })
        }

        composable<Routes.Plans> {
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = false,
                        canDeleteTasks = model.completedTasksExist.collectAsState(initial = false).value,
                        onDeleteAction = { /* Handle delete completed tasks in TopBar or here */ },
                        model = model
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = { 
                            planToEdit = null
                            showPlanDialog = true 
                        },
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
                        canDeleteTasks = model.completedTasksExist.collectAsState(initial = false).value,
                        onDeleteAction = { },
                        model = model
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
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = true,
                        onUpClick = { navController.navigateUp() },
                        canDeleteTasks = model.completedTasksExist.collectAsState(initial = false).value,
                        onDeleteAction = { },
                        model = model
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = {
                            taskToEdit = Task(
                                id = "",
                                planId = planDetail.planId,
                                title = "",
                                due = null,
                                body = null,
                                stage = "To Do",
                            )
                            showTaskDialog = true
                        },
                        modifier = Modifier.padding(bottom = 80.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task"
                    )
                },
                bottomBar = { BottomNav(navController = navController) }
            ) { innerPadding ->
                PlanDetail(
                    modifier = Modifier.padding(innerPadding),
                    planId = planDetail.planId,
                    onSelectTask = { task ->
                        taskToEdit = task
                        showTaskDialog = true
                    },
                    model = model
                )
            }
        }

        composable<Routes.Group> {
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = false,
                        canDeleteTasks = model.completedTasksExist.collectAsState(initial = false).value,
                        onDeleteAction = { },
                        model = model
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
            Scaffold(
                topBar = {
                    GamePlanTopBar(
                        canNavigateBack = true,
                        onUpClick = { navController.navigateUp() },
                        canDeleteTasks = model.completedTasksExist.collectAsState(initial = false).value,
                        onDeleteAction = { },
                        model = model
                    )
                },
                floatingActionButton = {
                    TSFloatingActionButton(
                        onClick = {
                            planToEdit = Plan(
                                id = "",
                                groupId = groupDetail.groupId,
                                title = "",
                                body = null,
                                sprintLength = 0,
                                milestones = emptyList(),
                                completed = false
                            )
                            showPlanDialog = true
                        },
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Plan"
                    )
                },
                bottomBar = { BottomNav(navController = navController) }
            ) { innerPadding ->
                GroupDetail(
                    groupId = groupDetail.groupId,
                    onSelectPlan = { plan ->
                        navController.navigate(Routes.PlanDetail(plan.id.toString()))
                    },
                    model = model,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    if (showPlanDialog) {
        AddPlanDialog(
            onDismiss = { showPlanDialog = false },
            planToEdit = planToEdit,
            model = model,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }

    if (showTaskDialog) {
        AddTaskDialog(
            taskToEdit = taskToEdit,
            onDismiss = { showTaskDialog = false },
            model = model,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }

    if (showGroupDialog) {
        AddGroupDialog(
            onDismiss = { showGroupDialog = false },
            model = model,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}

@Composable
fun TSFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    contentDescription: String
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
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
        title = { Text("Delete") },
        text = { Text("Are you sure you want to delete this item?") },
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
