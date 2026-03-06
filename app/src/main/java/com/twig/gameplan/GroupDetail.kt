package com.twig.gameplan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GroupDetail(
    groupId: Long,
    modifier: Modifier = Modifier,
    onSelectPlan: (Plan) -> Unit = {},
    model: GamePlanViewModel
) {
    val group by model.getGroupById(groupId).collectAsState(initial = null)
    val allPlans by model.getPlansByGroup(groupId).collectAsState(initial = emptyList())

    Scaffold(
        modifier = modifier,
        topBar = {
            GroupHeader(group = group)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
        ) {
            items(allPlans) { plan ->
                PlanCard(
                    plan = plan,
                    model = model,
                    toggleCompleted = {
                        model.togglePlanCompleted(it)
                    },
                    onClick = {
                        onSelectPlan(it)
                    },
                    toggleC = {
                        model.updateTask(it.copy(completed = !it.completed))
                    }
                )
            }
        }
    }
}

@Composable
fun GroupHeader(
    modifier: Modifier = Modifier,
    group: Group?
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = group?.title ?: " ", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        }
    }
}