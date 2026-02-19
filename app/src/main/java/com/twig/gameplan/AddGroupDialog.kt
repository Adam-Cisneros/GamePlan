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
fun AddGroupDialog(
    onDismiss: () -> Unit,
    model: GamePlanViewModel = viewModel<GamePlanViewModel>(),
    modifier: Modifier = Modifier
) {
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }
    var groupPlans by rememberSaveable { mutableStateOf(listOf<Plan>()) }

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
                GroupPlanInput(
                    plans = model.planList,
                    onPlanChange = { groupPlans = groupPlans + it },
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
                            Group(
                                name = groupName,
                                description = groupDescription,
                                plans = groupPlans
                            )
                            model.addGroup(
                                Group(
                                    name = groupName,
                                    description = groupDescription,
                                    plans = groupPlans
                                )
                            )
                            onDismiss()
                        }) {
                        Text("Add Group")
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupPlanInput(
    plans: List<Plan>,
    onPlanChange: (Plan) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by rememberSaveable { mutableStateOf<Plan?>(null) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier // This already gets .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedPlan?.title ?: "Select plans to connect (Optional)",
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
                        selectedPlan = plan
                        onPlanChange(plan)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
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
fun PreviewAddGroup() {
    GamePlanTheme {
        AddGroupDialog(
            onDismiss = { },
            modifier = Modifier.fillMaxSize(0.9f)
        )
    }
}
