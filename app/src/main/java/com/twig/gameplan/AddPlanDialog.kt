package com.twig.gameplan

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.twig.gameplan.ui.theme.GamePlanTheme

@Composable
fun AddPlanDialog(
    onDismiss: () -> Unit,
    viewModel: GamePlanViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var planTitle by rememberSaveable { mutableStateOf("") }
    var planBody by rememberSaveable { mutableStateOf("") }
    val tagList by rememberSaveable { mutableStateOf(mutableListOf<String>()) }

    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Card(
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                PlanTextInput(
                    planTitle,
                    label = "Enter task title",
                    onValueChange = { planTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                PlanTagsInput(
                    tagList,
                    onTagAdded = { tagList.add(it) },
                    onTagRemoved = { tagList.remove(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                PlanTextInput(
                    planBody,
                    label = "Enter detailed description",
                    onValueChange = { planBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ), onClick = {
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ), onClick = {
                            Plan(
                                title = planTitle,
                                body = planBody,
                                tags = tagList
                            )
                            viewModel.addPlan(
                                Plan(
                                    title = planTitle,
                                    body = planBody,
                                    tags = tagList
                                )
                            )
                            onDismiss()
                        }) {
                        Text("Add Task")
                    }
                }
            }
        }

    }
}

@Composable
fun PlanTextInput(
    text: String,
    label: String = "Enter text",
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp),
        value = text,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        maxLines = maxLines
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlanTagsInput(
    tagList: List<String>,
    onTagAdded: (String) -> Unit,
    onTagRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GamePlanViewModel = viewModel()
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        viewModel.tagList.forEach { tag ->
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color(0x18000000)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    modifier = Modifier.padding(0.dp),
                    checked = tagList.contains(tag),
                    onCheckedChange = { checked ->
                        if (checked) onTagAdded(tag)
                        else onTagRemoved(tag)
                    }
                )
                Text(
                    tag,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    }
}


@Preview(
    heightDp = 800,
    widthDp = 450
)
@Composable
fun PreviewAddTask() {
    GamePlanTheme {
        AddPlanDialog(
            onDismiss = { },
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}
