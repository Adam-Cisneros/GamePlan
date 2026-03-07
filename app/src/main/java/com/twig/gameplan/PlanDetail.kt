package com.twig.gameplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.filter

@Composable
fun PlanDetail(
    planId: String,
    modifier: Modifier = Modifier,
    onSelectTask: (Task) -> Unit = {},
    model: GamePlanViewModel
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showPlanDialog by remember { mutableStateOf(false) }

    val plan by model.getPlanById(planId).collectAsState(initial = null)

    // Correct way to observe tasks for this plan
    val allTasks by model.getTasksByPlan(planId).collectAsState(initial = emptyList())

    var planStage by remember { mutableStateOf("To Do") }

    // Derived state: these update automatically when 'allTasks' or 'planStage' change
    val tasksInStage = allTasks.filter { it.stage == planStage }

    val currentMilestone = allTasks
        .filter { it.stage == "Done" }
        .maxByOrNull { it.milestoneTitle ?: "" }?.milestoneTitle ?: " "

    Scaffold(
        modifier = modifier,
        topBar = {
            plan?.let {
                PlanHeader(
                    plan = it,
                    milestone = currentMilestone,
                    onDelete = { showConfirmationDialog = true },
                    onEdit = { showPlanDialog = true }
                )
            }
        },
        bottomBar = {
            plan?.let {
                PlanFooter(
                    plan = it,
                    stage = planStage,
                    onBackClick = { planStage = model.stageList[((model.stageList.indexOf(planStage) - 1)+model.stageList.size)%model.stageList.size] },
                    onForwardClick = { planStage = model.stageList[(model.stageList.indexOf(planStage) + 1)%model.stageList.size] }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(
                items = tasksInStage ?: emptyList(),
                key = { it.id } // This is critical for the UI to track the item moving
            ) { task ->
                TaskCard(
                    task = task,
                    onClick = { onSelectTask(it) },
                    enableSwipe = true,
                    onSwipeRight = { model.moveTaskStage(it, 1) },
                    onSwipeLeft = { model.moveTaskStage(it, -1) }
                )
            }
        }
    }

    if (showConfirmationDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                model.deletePlan(plan!!)
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
            planToEdit = plan,
            model = model,
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}

@Composable
fun PlanHeader(
    modifier: Modifier = Modifier,
    plan: Plan,
    milestone: String? = null,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    onDelete()
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = plan.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(text = milestone ?: " ", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(
                onClick = {
                    onEdit()
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
        }
    }
}

@Composable
fun PlanFooter(
    modifier: Modifier = Modifier,
    plan: Plan,
    stage: String = "To Do",
    onBackClick: () -> Unit = {},
    onForwardClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous"
                )
            }
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stage,
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = { onForwardClick() },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next"
                )
            }
        }
    }
}