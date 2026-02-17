package com.twig.gameplan

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
) {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = {
                onClick(plan)
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
                PlanTitle(plan,
                    modifier = Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Outlined.List,
                    contentDescription = "Notes"
                )
                Checkbox(
                    checked = plan.completed,
                    modifier = Modifier.alignByBaseline(),
                    onCheckedChange = {
                        toggleCompleted(plan)
                    })

            }
            Row(
                modifier= Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                PlanTags(plan)
                plan.due?.let {
                    Text(
                        formatter.format(plan.due),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            PlanBody(
                plan,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
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
fun PlanTags(plan: Plan, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        plan.tags.forEach { tag ->
            Text(
                tag,
                modifier = Modifier
                    .alignByBaseline()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
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