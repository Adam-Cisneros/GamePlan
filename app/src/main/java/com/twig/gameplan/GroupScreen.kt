package com.twig.gameplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.twig.gameplan.ui.theme.GamePlanTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    onSelectGroup: (Group) -> Unit = {},
    model: GamePlanViewModel
) {
    val groupList by model.allGroups.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(groupList) { group ->
            GroupCard(
                group = group,
                model = model,
                onClick = {
                    onSelectGroup(it)
                }
            )
        }
    }
}

@Composable
fun GroupCard(
    modifier: Modifier = Modifier,
    group: Group,
    model: GamePlanViewModel,
    onClick: (Group) -> Unit = {},
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = {
                isExpanded = !isExpanded
            }),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier // The modifier was passed from the parent, remove it here
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GroupName(
                    group,
                    modifier = Modifier.weight(1f)
                )
                // IconButton to toggle the expanded state
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }
            // This content will only be visible when isExpanded is true
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GroupPlans(
                            modifier = Modifier.weight(1f),
                            group,
                            model = model
                        )
                    }
                    Button(
                        onClick = {
                            onClick(group)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Open Group")
                    }
                }
            }
        }
    }
}

@Composable
fun GroupName(
    group: Group,
    modifier: Modifier = Modifier
) {
    Text(
        text = group.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
    )
}

@Composable
fun GroupPlans(
    modifier: Modifier = Modifier,
    group: Group,
    model: GamePlanViewModel,
) {
    val plans by model.getPlansByGroup(group.id).collectAsState(initial = emptyList())

    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(
            text = "Plans",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )
        plans.forEach { plan ->
            // Calculate overall progress for the plan
            val allTasksInPlan = model.getTasksByPlan(plan.id).collectAsState(initial = emptyList()).value
            val completedTasks = allTasksInPlan.count { it.stage == "Done" }
            val totalTasks = allTasksInPlan.size
            val progress = if (totalTasks > 0) {
                completedTasks.toFloat() / totalTasks.toFloat()
            } else {
                0f
            }

            // Display the plan title and its progress bar
            Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                )
            }
        }
    }
}
