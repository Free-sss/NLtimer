package com.nltimer.app.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(modifier = modifier) {
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = null,
                )
            },
            label = { Text("选项一") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Brightness5,
                    contentDescription = null,
                )
            },
            label = { Text("选项二") },
            selected = false,
            onClick = onClose,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}
