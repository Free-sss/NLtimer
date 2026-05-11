package com.nltimer.app.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.toDisplayString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    layoutLabel: String? = null,
    onLayoutChange: ((HomeLayout) -> Unit)? = null,
) {
    var layoutMenuExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(title)
                if (layoutLabel != null) {
                    val subtitleFontSize = 10.sp
                    Text(
                        text = " $layoutLabel",
                        style = TextStyle(fontSize = subtitleFontSize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (onLayoutChange != null) {
                            Modifier.clickable { layoutMenuExpanded = true }
                        } else Modifier,
                    )
                    if (onLayoutChange != null) {
                        DropdownMenu(
                            expanded = layoutMenuExpanded,
                            onDismissRequest = { layoutMenuExpanded = false },
                        ) {
                            HomeLayout.entries.forEach { layout ->
                                DropdownMenuItem(
                                    text = { Text(layout.toDisplayString()) },
                                    onClick = {
                                        onLayoutChange(layout)
                                        layoutMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCollapsedTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    layoutLabel: String? = null,
    onLayoutChange: ((HomeLayout) -> Unit)? = null,
) {
    var layoutMenuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.Bottom,modifier=Modifier.padding(bottom = 5.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                    ),
                )
                if (layoutLabel != null) {
                    val subtitleFontSize = 10.sp
                    Text(
                        text = " $layoutLabel",
                        style = TextStyle(fontSize = subtitleFontSize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (onLayoutChange != null) {
                            Modifier.clickable { layoutMenuExpanded = true }
                        } else Modifier,
                    )
                    if (onLayoutChange != null) {
                        DropdownMenu(
                            expanded = layoutMenuExpanded,
                            onDismissRequest = { layoutMenuExpanded = false },
                        ) {
                            HomeLayout.entries.forEach { layout ->
                                DropdownMenuItem(
                                    text = { Text(layout.toDisplayString()) },
                                    onClick = {
                                        onLayoutChange(layout)
                                        layoutMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        modifier = modifier,
    )
}
