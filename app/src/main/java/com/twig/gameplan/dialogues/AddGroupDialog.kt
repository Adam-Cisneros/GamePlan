package com.twig.gameplan.dialogues

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.twig.gameplan.DeleteConfirmationDialog
import com.twig.gameplan.GamePlanViewModel
import com.twig.gameplan.api.GitHubOrg
import com.twig.gameplan.data.Group

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    groupToEdit: Group? = null,
    model: GamePlanViewModel,
    modifier: Modifier = Modifier
) {
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    var groupName by remember(groupToEdit) { mutableStateOf(groupToEdit?.title ?: "") }
    var groupDescription by remember(groupToEdit) { mutableStateOf(groupToEdit?.description ?: "") }

    val gitHubToken by model.gitHubToken.collectAsState()

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
                Text("Group Details", style = MaterialTheme.typography.headlineSmall)

                if (gitHubToken != null && groupToEdit == null) {
                    GitHubGroupImportSection(model = model) { org ->
                        model.importGitHubOrgAsGroup(org.login)
                        onDismiss()
                    }
                    HorizontalDivider()
                }

                GroupTextInput(
                    groupName,
                    label = "Enter group name",
                    onValueChange = { groupName = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )
                GroupTextInput(
                    groupDescription,
                    label = "Enter detailed description (Optional)",
                    onValueChange = { groupDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (groupToEdit != null) {
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
                        enabled = groupName.isNotBlank(),
                        onClick = {
                            if (groupToEdit == null) {
                                val group = Group(
                                    title = groupName,
                                    description = groupDescription,
                                )
                                model.addGroup(group)
                            } else {
                                val group = groupToEdit.copy(
                                    title = groupName,
                                    description = groupDescription,
                                )
                                model.updateGroup(group)
                            }
                            onDismiss()
                        }) {
                        Text(if (groupToEdit == null) "Add" else "Update")
                    }
                }
            }
        }
        if (showConfirmationDialog) {
            DeleteConfirmationDialog(
                onConfirm = {
                    model.deleteGroup(groupToEdit!!)
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
fun GitHubGroupImportSection(
    model: GamePlanViewModel,
    onImport: (GitHubOrg) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var orgs by remember { mutableStateOf<List<GitHubOrg>>(emptyList()) }
    var selectedOrg by remember { mutableStateOf<GitHubOrg?>(null) }

    LaunchedEffect(Unit) {
        orgs = model.getGitHubOrgs()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Import from GitHub Organizations", style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOrg?.login ?: "Select an organization",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                orgs.forEach { org ->
                    DropdownMenuItem(
                        text = { Text(org.login) },
                        onClick = {
                            selectedOrg = org
                            expanded = false
                        }
                    )
                }
            }
        }
        if (selectedOrg != null) {
            Button(
                onClick = { onImport(selectedOrg!!) },
                modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
            ) {
                Text("Import Org")
            }
        }
    }
}

@Composable
fun GroupTextInput(
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
