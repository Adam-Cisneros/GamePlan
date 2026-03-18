package com.twig.gameplan.dialogues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.twig.gameplan.DeleteConfirmationDialog
import com.twig.gameplan.GamePlanViewModel
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
                                    id = groupToEdit.id,
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
