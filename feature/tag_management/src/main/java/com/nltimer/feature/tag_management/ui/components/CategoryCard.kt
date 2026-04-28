package com.nltimer.feature.tag_management.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryCard(
    categoryName: String,
    tags: List<Tag>,
    isDefaultCategory: Boolean = false,
    onAddTag: () -> Unit,
    onTagClick: (Tag) -> Unit,
    onTagLongClick: (Tag) -> Unit,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                if (!isDefaultCategory) {
                    IconButton(onClick = onMenuClick, modifier = Modifier.padding(0.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onClick = { onTagClick(tag) },
                        onLongClick = { onTagLongClick(tag) },
                    )
                }

                IconButton(
                    onClick = onAddTag,
                    modifier = Modifier.padding(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加标签",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
