package com.twig.gameplan

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TaskDetail(taskId: String) {
    Text(text = "Details for Task ID: $taskId")
}