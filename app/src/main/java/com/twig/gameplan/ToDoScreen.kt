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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.twig.gameplan.ui.theme.GamePlanTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ToDoScreen(
    modifier: Modifier = Modifier,
    onSelectTask: (Task) -> Unit = {},
    model: GamePlanViewModel = viewModel<GamePlanViewModel>()
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(model.taskList) { task ->
            TaskCard(
                task = task,
                toggleCompleted = {
                    model.toggleTaskCompleted(it)
                },
                onClick = {
                    onSelectTask(it)
                }
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    toggleCompleted: (Task) -> Unit = {},
    onClick: (Task) -> Unit = {},
) {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
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
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = {
                        toggleCompleted(task)
                    }
                )
                TaskTitle(
                    task,
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
                        task.due?.let {
                            Text(
                                formatter.format(task.due),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskBody(
                            task,
                            modifier = Modifier.padding(16.dp, 8.dp, 0.dp, 0.dp)
                        )
                        IconButton(onClick = {
                            onClick(task)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTitle(
    task: Task,
    modifier: Modifier = Modifier
) {
    Text(
        text = task.title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
        color = if (task.completed)

            Color.Gray else Color.Black
    )
}

@Composable
fun TaskBody(
    task: Task,
    modifier: Modifier = Modifier
) {
    task.body?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineBreak = LineBreak.Paragraph
            ),
            modifier = modifier,
            color = if (task.completed)

                Color.Gray else Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ToDoScreenPreview() {
    val todoViewModel = viewModel<GamePlanViewModel>()

    if (todoViewModel.taskList.isEmpty())
        todoViewModel.createTestTasks(50)

    GamePlanTheme {
        ToDoScreen(
            modifier = Modifier
                .height(800.dp)
                .width(450.dp),
            model = todoViewModel
        )
    }
}

@Preview(
    showBackground = true,
    heightDp = 450,
    widthDp = 800
)
@Composable
fun ToDoScreenLandscapePreview() {
    val todoViewModel = viewModel<GamePlanViewModel>()

    if (todoViewModel.taskList.isEmpty())
        todoViewModel.createTestTasks(50)

    GamePlanTheme {
        ToDoScreen(
            modifier = Modifier
                .height(450.dp)
                .width(800.dp),
            model = todoViewModel
        )
    }
}
