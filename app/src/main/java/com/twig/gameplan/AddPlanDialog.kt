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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    planToEdit: Plan? = null,
    model: GamePlanViewModel,
    modifier: Modifier = Modifier
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    var planTitle by remember(planToEdit) { mutableStateOf(planToEdit?.title ?: "") }
    var planBody by remember(planToEdit) { mutableStateOf(planToEdit?.body ?: "") }
    var milestoneText by rememberSaveable { mutableStateOf("") }
    var planGroup by remember { mutableStateOf<Group?>(null) }
    var planMilestones by remember(planToEdit) { mutableStateOf(planToEdit?.milestones ?: listOf<String>()) }
    var planSprintLength by remember(planToEdit) { mutableStateOf<Int?>(planToEdit?.sprintLength) }

    val groups by model.allGroups.collectAsState(initial = emptyList())

    LaunchedEffect(planToEdit, groups) {
        if (planToEdit != null && planGroup == null) {
            planGroup = groups.find { it.id == planToEdit.groupId }
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
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
                            planMilestones = planMilestones + milestoneText
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
                    groups = groups,
                    selectedGroup = planGroup,
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
                        ),
                        enabled = planTitle.isNotBlank() && planSprintLength != null,
                        onClick = {
                            if (planToEdit == null || planToEdit.id == (-1).toLong()) {
                                val plan = Plan(
                                    title = planTitle,
                                    body = planBody,
                                    groupId = planGroup?.id,
                                    milestones = planMilestones,
                                    sprintLength = planSprintLength!!,
                                    completed = false
                                )
                                model.addPlan(plan)
                            } else {
                                val plan = Plan(
                                    id = planToEdit.id,
                                    title = planTitle,
                                    body = planBody,
                                    groupId = planGroup?.id,
                                    milestones = planMilestones,
                                    sprintLength = planSprintLength!!,
                                    completed = planToEdit.completed
                                )
                                model.updatePlan(plan)
                            }
                            onDismiss()
                        }) {
                        Text(if (planToEdit == null) "Add Plan" else "Update Plan")
                    }
                }
            }
        }
        if (showConfirmationDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    model.deletePlan(planToEdit!!)
                    showConfirmationDialog = false
                    onDismiss()
                },
                onDismiss = {
                    showConfirmationDialog = false
                }
            )
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
    milestones: List<String>,
    onRemove: (String) -> Unit
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
                    label = { Text(milestone) },
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
    selectedGroup: Group?,
    onGroupChange: (Group) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSelectedGroup by remember(selectedGroup) { mutableStateOf(selectedGroup) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // This already gets .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentSelectedGroup?.title ?: "Select a group",
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
                    text = { Text(group.title) },
                    onClick = {
                        currentSelectedGroup = group
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
