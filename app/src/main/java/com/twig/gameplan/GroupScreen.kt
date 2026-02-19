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
import androidx.compose.ui.text.font.FontWeight

@Composable
fun GroupScreen(
    modifier: Modifier = Modifier,
    onSelectGroup: (Group) -> Unit = {},
    model: GamePlanViewModel
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(model.groupList) { group ->
            GroupCard(
                group = group,
                onClick = {
                    onSelectGroup(it)
                }
            )
        }
    }
}

@Composable
fun GroupCard(
    group: Group,
    modifier: Modifier = Modifier,
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
                            group,
                            modifier = Modifier.weight(1f)
                        )
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
        text = group.name,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
    )
}

@Composable
fun GroupPlans(
    group: Group,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(
            text = "Plans",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Black
        )
        group.plans.forEach { plan ->
            // Calculate overall progress for the plan
            val allTasks = plan.milestones.flatMap { it.tasks }
            val completedTasks = allTasks.count { it.completed }
            val totalTasks = allTasks.size
            val progress = if (totalTasks > 0) {
                completedTasks.toFloat() / totalTasks.toFloat()
            } else {
                0f
            }

            // Display the plan title and its progress bar
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
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

@Preview(showBackground = true)
@Composable
fun GroupScreenPreview() {
    val todoViewModel = viewModel<GamePlanViewModel>()

    if (todoViewModel.groupList.isEmpty())
        todoViewModel.createTestGroups(50)

    GamePlanTheme {
        GroupScreen(
            modifier = Modifier
                .height(800.dp)
                .width(450.dp),
            model = todoViewModel
        )
    }
}