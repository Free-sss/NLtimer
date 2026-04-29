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

/**
 * 分类卡片组件
 *
 * 展示一个分类名称及其下的所有标签，支持添加标签、点击/长按标签、分类菜单操作。
 *
 * @param categoryName 分类名称
 * @param tags 该分类下的标签列表
 * @param isDefaultCategory 是否为默认（未分类）分类，默认分类不显示菜单按钮
 * @param onAddTag 添加标签回调
 * @param onTagClick 点击标签回调
 * @param onTagLongClick 长按标签回调
 * @param onMenuClick 点击分类菜单回调
 * @param modifier 修饰符
 */
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
            // 分类标题行：左侧分类名称，右侧更多操作按钮
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

                // 非默认分类才显示菜单按钮（默认分类不可编辑）
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

            // 使用 FlowRow 自动换行排列标签和添加按钮
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // 渲染该分类下的所有标签
                tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        onClick = { onTagClick(tag) },
                        onLongClick = { onTagLongClick(tag) },
                    )
                }

                // 添加标签按钮
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
