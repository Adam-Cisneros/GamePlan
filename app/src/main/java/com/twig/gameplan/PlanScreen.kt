package com.twig.gameplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
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
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.twig.gameplan.ui.theme.GamePlanTheme
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.LinearProgressIndicator


@Composable
fun PlanScreen(
    modifier: Modifier = Modifier,
    onSelectPlan: (Plan) -> Unit = {},
    model: GamePlanViewModel = viewModel<GamePlanViewModel>()
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(model.planList) { plan ->
            PlanCard(
                plan = plan,
                toggleCompleted = {
                    model.togglePlanCompleted(it)
                },
                onClick = {
                    onSelectPlan(it)
                },
                toggleC = {
                    model.toggleTaskCompleted(it)
                }
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: Plan,
    modifier: Modifier = Modifier,
    toggleCompleted: (Plan) -> Unit = {},
    onClick: (Plan) -> Unit = {},
    toggleC: (Task) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = {
                expanded = !expanded
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
                modifier = modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IconButton to toggle the expanded state
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
                PlanTitle(plan,
                    modifier = Modifier.weight(1f))
                Checkbox(
                    checked = plan.completed,
                    modifier = Modifier.alignByBaseline(),
                    onCheckedChange = {
                        toggleCompleted(plan)
                    })

            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    PlanBody(
                        plan,
                        modifier = Modifier.padding(16.dp, 0.dp)
                    )
                    PlanProgress(
                        plan,
                        modifier = Modifier.padding(16.dp, 0.dp)
                    )
                    PlanTodo(
                        plan,
                        modifier = Modifier.padding(16.dp, 0.dp),
                        toggleCompleted = toggleC
                    )
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = {
                            onClick(plan)
                        }
                    ) {
                        Text("Open Plan")
                    }
                }
            }
        }
    }
}

@Composable
fun PlanTitle(
    plan: Plan,
    modifier: Modifier = Modifier
) {
    Text(
        text = plan.title,
        style = MaterialTheme.typography.displaySmall,
        modifier = modifier,
        color = if (plan.completed)

            Color.Gray else Color.Black
    )
}

@Composable
fun PlanBody(
    plan: Plan,
    modifier: Modifier = Modifier
) {
    plan.body?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineBreak = LineBreak.Paragraph
            ),
            modifier = modifier,
            color = if (plan.completed)

                Color.Gray else Color.Black
        )
    }
}

@Composable
fun PlanProgress(
    plan: Plan,
    modifier: Modifier = Modifier
) {
    if (plan.milestones.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.titleLarge,
                color = if (plan.completed) Color.Gray else Color.Black
            )
            plan.milestones.forEach { milestone ->
                // Calculate the progress for each milestone
                val completedTasks = milestone.tasks.count { it.completed }
                val totalTasks = milestone.tasks.size
                val progress = if (totalTasks > 0) {
                    completedTasks.toFloat() / totalTasks.toFloat()
                } else {
                    0f
                }

                // Display the milestone title and its progress bar
                Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (plan.completed) Color.Gray else Color.Black
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
}

@Composable
fun PlanTodo(
    plan: Plan,
    modifier: Modifier = Modifier,
    toggleCompleted: (Task) -> Unit = {},
) {
    if (plan.milestones.isNotEmpty()) {
        Column(modifier = modifier) {
            Text(
                text = "To Do",
                style = MaterialTheme.typography.titleLarge,
                color = if (plan.completed) Color.Gray else Color.Black
            )
            plan.milestones.forEach { milestone ->
                milestone.tasks.forEach { task ->
                    if (!task.completed) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .align(Alignment.Start),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.completed,
                                modifier = Modifier.alignByBaseline(),
                                onCheckedChange = {
                                    toggleCompleted(task)
                                }
                            )
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (plan.completed) Color.Gray else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlanScreenPreview() {
    val todoViewModel = viewModel<GamePlanViewModel>()

    if (todoViewModel.planList.isEmpty())
        todoViewModel.createTestTasks(50)

    GamePlanTheme {
        PlanScreen(
            modifier = Modifier
                .height(800.dp)
                .width(450.dp),
            model = todoViewModel
        )
    }
}

@Preview(showBackground = true,
    heightDp = 450,
    widthDp = 800)
@Composable
fun PlanScreenLandscapePreview() {
    val todoViewModel = viewModel<GamePlanViewModel>()

    if (todoViewModel.planList.isEmpty())
        todoViewModel.createTestTasks(50)

    GamePlanTheme {
        PlanScreen(
            modifier = Modifier
                .height(450.dp)
                .width(800.dp),
            model = todoViewModel
        )
    }
}
