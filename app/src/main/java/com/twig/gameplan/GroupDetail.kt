package com.twig.gameplan

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun GroupDetail(groupId: String) {
    Text(text = "Details for Plan ID: $groupId")
}