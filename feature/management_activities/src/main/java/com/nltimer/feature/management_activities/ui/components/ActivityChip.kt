package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.core.data.model.Activity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityChip(
    activity: Activity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emoji = activity.emoji?.takeIf { it.isNotBlank() } ?: "📌"
    val label = "$emoji ${activity.name}"

    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    )
}
