package com.twig.gameplan.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twig.gameplan.DeleteConfirmationDialog
import com.twig.gameplan.GamePlanViewModel
import com.twig.gameplan.data.Group
import com.twig.gameplan.data.Plan
import com.twig.gameplan.dialogues.AddGroupDialog

@Composable
fun GroupDetail(
    groupId: String,
    modifier: Modifier = Modifier,
    onSelectPlan: (Plan) -> Unit = {},
    model: GamePlanViewModel
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    val group by model.getGroupById(groupId).collectAsState(initial = null)
    val allPlans by model.getPlansByGroup(groupId).collectAsState(initial = emptyList())

    Scaffold(
        modifier = modifier,
        topBar = {
            GroupHeader(
                group = group,
                onDelete = {
                    showConfirmationDialog = true
                },
                onEdit = {
                    showGroupDialog = true
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(allPlans) { plan ->
                PlanCard(
                    plan = plan,
                    model = model,
                    onClick = {
                        onSelectPlan(it)
                    },
                )
            }
        }
    }

    if (showConfirmationDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                model.deleteGroup(group!!)
                showConfirmationDialog = false
            },
            onDismiss = {
                showConfirmationDialog = false
            }
        )
    }

    if (showGroupDialog) {
        AddGroupDialog(
            onDismiss = { showGroupDialog = false },
            groupToEdit = group,
            model = model,
            modifier = Modifier.fillMaxWidth(0.9f)
        )
    }
}

@Composable
fun GroupHeader(
    modifier: Modifier = Modifier,
    group: Group?,
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
                Text(text = group?.title ?: " ", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
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