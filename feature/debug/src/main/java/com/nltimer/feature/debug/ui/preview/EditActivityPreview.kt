package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun EditActivityPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var showSheet by remember { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "点击下方按钮打开编辑活动弹窗",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { showSheet = true }) {
                    Text("打开编辑活动弹窗")
                }
            }
        }

        if (showSheet) {
            GenericFormSheet(
                spec = ActivityFormSpecs.editActivity(),
                initialData = mapOf("name" to "阅读", "note" to "每天30分钟", "emoji" to "📖"),
                onDismiss = { showSheet = false },
                onSubmit = { showSheet = false },
            )
        }
    }
}
