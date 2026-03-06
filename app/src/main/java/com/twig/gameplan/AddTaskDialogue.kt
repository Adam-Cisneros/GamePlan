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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    taskToEdit: Task?, // Accept a nullable Task object
    model: GamePlanViewModel,
    modifier: Modifier = Modifier
) {
    var taskTitle by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var taskBody by remember(taskToEdit) { mutableStateOf(taskToEdit?.body ?: "") }
    var taskMilestone by remember(taskToEdit) { mutableStateOf(taskToEdit?.milestoneTitle) }

    // 1. Initialize as null; we will populate it from the 'plans' list
    var taskPlan by remember { mutableStateOf<Plan?>(null) }
    var taskDue by remember(taskToEdit) { mutableStateOf(taskToEdit?.due) }

    var showDateModal by rememberSaveable { mutableStateOf(false) }

    // 2. Observe all plans (this is already correctly done in your file)
    val plans by model.allPlans.collectAsState(initial = emptyList())

    // 3. Use LaunchedEffect to set the initial plan once 'plans' list is loaded
    LaunchedEffect(taskToEdit, plans) {
        if (taskToEdit != null && taskPlan == null) {
            taskPlan = plans.find { it.id == taskToEdit.planId }
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

                TaskTextInput(
                    taskTitle,
                    label = "Enter task title",
                    onValueChange = { taskTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                TaskTextInput(
                    taskBody,
                    label = "Enter detailed description (Optional)",
                    onValueChange = { taskBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                TaskPlanInput(
                    plans = model.allPlans.collectAsState(initial = emptyList()).value,
                    selectedPlan = taskPlan,
                    onPlanChange = { taskPlan = it },
                    modifier = Modifier.fillMaxWidth()
                )
                TaskMilestoneInput(
                    selectedMilestone = taskMilestone,
                    onMilestoneChange = { taskMilestone = it },
                    milestones = taskPlan?.milestones ?: emptyList<String>(),
                    modifier = Modifier.fillMaxWidth()
                )
                TaskDateField(
                    label = "Due Date (Optional for Sprints)",
                    selectedDate = taskDue,
                    onSelect = {
                        showDateModal = true
                    },
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
                        enabled = taskTitle.isNotBlank() && taskPlan != null,
                        onClick = {
                            if (taskToEdit == null) {
                                // Add new task
                                model.addTask(
                                    Task(
                                        title = taskTitle,
                                        body = taskBody,
                                        planId = taskPlan?.id,
                                        milestoneTitle = taskMilestone,
                                        due = taskDue
                                    )
                                )
                            } else {
                                // Update existing task
                                val updatedTask = taskToEdit.copy(
                                    title = taskTitle,
                                    body = taskBody,
                                    planId = taskPlan?.id,
                                    milestoneTitle = taskMilestone,
                                    due = taskDue
                                )
                                model.updateTask(updatedTask)
                            }
                            onDismiss()
                        }) {
                        // Change button text based on whether we are editing or adding
                        Text(if (taskToEdit == null) "Add Task" else "Update Task")
                    }
                }
            }
        }
        if (showDateModal) {
            DatePickerModal(
                onDateSelected = {
                    taskDue = it?.let { Date(it) }
                    showDateModal = false
                },
                onDismiss = {
                    showDateModal = false
                }
            )
        }
    }
}

@Composable
fun TaskTextInput(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskMilestoneInput(
    onMilestoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    milestones: List<String>,
    selectedMilestone: String? // Pass in the selected milestone
) {
    var currentSelectedMilestone by remember(selectedMilestone) { mutableStateOf(selectedMilestone) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // This already gets .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentSelectedMilestone ?: "Select a milestone",
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
            milestones.forEach { milestone ->
                DropdownMenuItem(
                    text = { Text(milestone) },
                    onClick = {
                        currentSelectedMilestone = milestone
                        onMilestoneChange(milestone)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPlanInput(
    plans: List<Plan>,
    onPlanChange: (Plan) -> Unit,
    modifier: Modifier = Modifier,
    selectedPlan: Plan? // Pass in the selected plan
) {
    var currentSelectedPlan by remember(selectedPlan) { mutableStateOf(selectedPlan) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // This already gets .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = currentSelectedPlan?.title ?: "Select a plan to connect to",
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
            plans.forEach { plan ->
                DropdownMenuItem(
                    text = { Text(plan.title) },
                    onClick = {
                        currentSelectedPlan = plan
                        onPlanChange(plan)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun TaskDateField(
    label: String,
    selectedDate: Date?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    OutlinedTextField(
        value = selectedDate?.let { formatter.format(it) } ?: "",
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = onSelect) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select date (Optional for Sprints)"
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
