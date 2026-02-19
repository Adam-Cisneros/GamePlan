package com.twig.gameplan

import android.annotation.SuppressLint
import androidx.compose.animation.core.copy
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.twig.gameplan.ui.theme.GamePlanTheme

@Composable
fun AddPlanDialog(
    onDismiss: () -> Unit,
    viewModel: GamePlanViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var planTitle by rememberSaveable { mutableStateOf("") }
    var planBody by rememberSaveable { mutableStateOf("") }
    var milestoneText by rememberSaveable { mutableStateOf("") }
    var planGroup by rememberSaveable { mutableStateOf<Group?>(null) }
    var planMilestones by rememberSaveable { mutableStateOf(listOf<Milestone>()) }
    var planSprintLength by rememberSaveable { mutableStateOf<Int?>(null) }

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
                    label = "Enter plan title",
                    onValueChange = { planTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                PlanTextInput(
                    planBody,
                    label = "Enter detailed description",
                    onValueChange = { planBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                PlanMilestoneInput(
                    text = milestoneText,
                    label = "Enter milestone",
                    onValueChange = { milestoneText = it },
                    onAdd = {
                        if (milestoneText.isNotBlank()) {
                            planMilestones = planMilestones + Milestone(
                                title = milestoneText,
                                tasks = emptyList()
                            )
                            milestoneText = "" // Clear text after adding
                        }
                    },
                    milestones = planMilestones,
                    onRemove = { milestoneToRemove ->
                        planMilestones = planMilestones.filterNot { it == milestoneToRemove }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                )
                PlanGroupInput(
                    groups = viewModel.groupList,
                    onGroupChange = { planGroup = it },
                    modifier = Modifier.fillMaxWidth()
                )
                PlanSprintLengthInput(
                    sprintLength = planSprintLength,
                    onValueChange = { planSprintLength = it },
                    modifier = Modifier.fillMaxWidth()
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
                                group = planGroup,
                                milestones = planMilestones,
                                sprintLength = planSprintLength!!
                            )
                            viewModel.addPlan(
                                Plan(
                                    title = planTitle,
                                    body = planBody,
                                    group = planGroup,
                                    milestones = planMilestones,
                                    sprintLength = planSprintLength!!
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

@Composable
fun PlanMilestoneInput(
    text: String,
    label: String = "Enter text",
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
    milestones: List<Milestone>,
    onRemove: (Milestone) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = onValueChange,
                label = { Text(label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    onAdd()
                    keyboardController?.hide()
                })
            )
        }

        FlowRow(
            modifier = Modifier.padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            milestones.forEach { milestone ->
                InputChip(
                    selected = false,
                    onClick = { /* Not used */ },
                    label = { Text(milestone.title) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove milestone",
                            modifier = Modifier.clickable { onRemove(milestone) }
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanGroupInput(
    groups: List<Group>,
    onGroupChange: (Group) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGroup by rememberSaveable { mutableStateOf<Group?>(null) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // This already gets .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedGroup?.name ?: "Select a group",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            // Apply fillMaxWidth() here
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(6.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize() // This ensures the dropdown matches the text field width
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = { Text(group.name) },
                    onClick = {
                        selectedGroup = group
                        onGroupChange(group)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun PlanSprintLengthInput(
    label: String = "Sprint Length (Days)",
    sprintLength: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            label = { Text(label) },
            singleLine = true,
            value = if (sprintLength != null) sprintLength.toString() else "",
            onValueChange = {
                onValueChange(it.toIntOrNull() ?: 0)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        )
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
