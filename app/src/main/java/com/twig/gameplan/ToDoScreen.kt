package com.twig.gameplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    model: GamePlanViewModel
) {
    val taskList by model.allTasks.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(taskList, key = { it.id }) { task ->
            TaskCard(
                task = task,
                onClick = {
                    onSelectTask(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: (Task) -> Unit = {},
    onSwipeLeft: (Task) -> Unit = {},  // Added for demotion
    onSwipeRight: (Task) -> Unit = {}, // Added for promotion
    enableSwipe: Boolean = false       // Only enable swipe in PlanDetail
) {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    var isExpanded by remember { mutableStateOf(false) }

    // Swipe State
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { // Swiped Right
                    onSwipeRight(task)
                    false // Return false so the card stays in the list
                }
                SwipeToDismissBoxValue.EndToStart -> { // Swiped Left
                    onSwipeLeft(task)
                    false
                }
                else -> false
            }
        }
    )
    // Reset the swipe state if the task identity changes or the stage changes
    LaunchedEffect(task.stage) {
        dismissState.reset()
    }
    if (enableSwipe) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val color = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for promote
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336) // Red for demote
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(color, MaterialTheme.shapes.medium),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                ) {
                    val icon = if (direction == SwipeToDismissBoxValue.StartToEnd) Icons.Default.ArrowForward else Icons.Default.ArrowBack
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        ) {
            MainCardContent(task, isExpanded, onExpandToggle = { isExpanded = !isExpanded }, onClick, formatter, modifier)
        }
    } else {
        MainCardContent(task, isExpanded, onExpandToggle = { isExpanded = !isExpanded }, onClick, formatter, modifier)
    }
}

@Composable
private fun MainCardContent(
    task: Task,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onClick: (Task) -> Unit,
    formatter: SimpleDateFormat,
    modifier: Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = onExpandToggle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TaskTitle(task, modifier = Modifier.weight(1f))
                IconButton(onClick = onExpandToggle) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    task.due?.let {
                        Text(formatter.format(it), style = MaterialTheme.typography.labelSmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TaskBody(task, modifier = Modifier.padding(top = 8.dp))
                        IconButton(onClick = { onClick(task) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
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
        color = if (task.stage == "Done")

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
            color = if (task.stage == "Done")

                Color.Gray else Color.Black
        )
    }
}
