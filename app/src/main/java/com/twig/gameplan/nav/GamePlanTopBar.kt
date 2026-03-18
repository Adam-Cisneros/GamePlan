package com.twig.gameplan.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.twig.gameplan.GamePlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlanTopBar(
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onUpClick: () -> Unit = { },
    canDeleteTasks: Boolean = false,
    onDeleteAction: () -> Unit = {},
    model: GamePlanViewModel,
) {
    TopAppBar(
        title = { Text(
            "GamePlan",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        ) },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onUpClick
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (canDeleteTasks) {
                IconButton(
                    onClick = onDeleteAction,
                    enabled = true,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Tasks"
                    )
                }
            }
        }
    )
}