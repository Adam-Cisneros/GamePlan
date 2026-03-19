package com.twig.gameplan.dialogues

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.util.Date
import com.twig.gameplan.DeleteConfirmationDialog
import com.twig.gameplan.GamePlanViewModel
import com.twig.gameplan.api.GitHubIssue
import com.twig.gameplan.api.GitHubRepo
import com.twig.gameplan.data.Plan
import com.twig.gameplan.data.Task

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    taskToEdit: Task?,
    model: GamePlanViewModel,
    modifier: Modifier = Modifier
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    var taskTitle by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var taskBody by remember(taskToEdit) { mutableStateOf(taskToEdit?.body ?: "") }
    var taskMilestone by remember(taskToEdit) { mutableStateOf(taskToEdit?.milestoneTitle) }

    var taskPlan by remember { mutableStateOf<Plan?>(null) }
    var taskDue by remember(taskToEdit) { mutableStateOf(taskToEdit?.due) }

    var showDateModal by rememberSaveable { mutableStateOf(false) }

    val plans by model.allPlans.collectAsState(initial = emptyList())
    val gitHubToken by model.gitHubToken.collectAsState()

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
                modifier = Modifier.padding(16.dp).fillMaxHeight()
            ) {
                Text("Task Details", style = MaterialTheme.typography.headlineSmall)

                if (gitHubToken != null && taskToEdit == null) {
                    GitHubTaskImportSection(model = model, selectedPlan = taskPlan) { owner, repo, issue ->
                        model.importGitHubIssueAsTask(owner, repo, issue.title, taskPlan?.id)
                        onDismiss()
                    }
                    HorizontalDivider()
                }

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
                    plans = plans,
                    selectedPlan = taskPlan,
                    onPlanChange = { taskPlan = it },
                    modifier = Modifier.fillMaxWidth()
                )
                TaskMilestoneInput(
                    selectedMilestone = taskMilestone,
                    onMilestoneChange = { taskMilestone = it },
                    milestones = taskPlan?.milestones ?: emptyList(),
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
                    if (taskToEdit != null && taskToEdit.id != "") {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ), onClick = {
                                showConfirmationDialog = true
                            }
                        ) {
                            Text("Delete")
                        }
                    }
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
                                if (taskToEdit.id == "") {
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
                                    val updatedTask = taskToEdit.copy(
                                        title = taskTitle,
                                        body = taskBody,
                                        planId = taskPlan?.id,
                                        milestoneTitle = taskMilestone,
                                        due = taskDue
                                    )
                                    model.updateTask(updatedTask)
                                }
                            }
                            onDismiss()
                        }) {
                        Text(if (taskToEdit == null || taskToEdit.id == "") "Add" else "Update")
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

        if (showConfirmationDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    model.deleteTask(taskToEdit!!)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubTaskImportSection(
    model: GamePlanViewModel,
    selectedPlan: Plan?,
    onImport: (String, String, GitHubIssue) -> Unit
) {
    var reposExpanded by remember { mutableStateOf(false) }
    var issuesExpanded by remember { mutableStateOf(false) }
    var repos by remember { mutableStateOf<List<GitHubRepo>>(emptyList()) }
    var issues by remember { mutableStateOf<List<GitHubIssue>>(emptyList()) }
    var selectedRepo by remember { mutableStateOf<GitHubRepo?>(null) }
    var selectedIssue by remember { mutableStateOf<GitHubIssue?>(null) }

    LaunchedEffect(Unit) {
        repos = model.getGitHubRepos()
    }

    LaunchedEffect(selectedRepo) {
        if (selectedRepo != null) {
            issues = model.getGitHubIssues(selectedRepo!!.owner.login, selectedRepo!!.name)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Import Issue from GitHub", style = MaterialTheme.typography.titleMedium)
        
        ExposedDropdownMenuBox(
            expanded = reposExpanded,
            onExpandedChange = { reposExpanded = !reposExpanded }
        ) {
            OutlinedTextField(
                value = selectedRepo?.name ?: "Select a repository",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reposExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = reposExpanded,
                onDismissRequest = { reposExpanded = false }
            ) {
                repos.forEach { repo ->
                    DropdownMenuItem(
                        text = { Text(repo.name) },
                        onClick = {
                            selectedRepo = repo
                            reposExpanded = false
                        }
                    )
                }
            }
        }

        if (selectedRepo != null) {
            ExposedDropdownMenuBox(
                expanded = issuesExpanded,
                onExpandedChange = { issuesExpanded = !issuesExpanded }
            ) {
                OutlinedTextField(
                    value = selectedIssue?.title ?: "Select an issue",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issuesExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = issuesExpanded,
                    onDismissRequest = { issuesExpanded = false }
                ) {
                    issues.forEach { issue ->
                        DropdownMenuItem(
                            text = { Text(issue.title) },
                            onClick = {
                                selectedIssue = issue
                                issuesExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (selectedIssue != null) {
            Button(
                onClick = { onImport(selectedRepo!!.owner.login, selectedRepo!!.name, selectedIssue!!) },
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text("Import Issue")
            }
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
    selectedMilestone: String?
) {
    var currentSelectedMilestone by remember(selectedMilestone) { mutableStateOf(selectedMilestone) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentSelectedMilestone ?: "Select a milestone",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(6.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
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
    selectedPlan: Plan?
) {
    var currentSelectedPlan by remember(selectedPlan) { mutableStateOf(selectedPlan) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentSelectedPlan?.title ?: "Select a plan",
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .padding(6.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
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
    OutlinedTextField(
        value = selectedDate?.toString() ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onSelect) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
            }
        },
        modifier = modifier.padding(6.dp)
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
