package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.BottomBarMode
import com.nltimer.core.designsystem.theme.LocalTheme

@Composable
fun BoxScope.BottomBarDragFab(
    state: DragFabState,
    icon: ImageVector,
    dragOptions: List<String>,
    modifier: Modifier = Modifier,
    label: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    cornerRadius: Dp = 28.dp,
    onClick: () -> Unit,
    onOptionSelected: (String) -> Unit,
) {
    val isCenterFab = LocalTheme.current.bottomBarMode == BottomBarMode.CENTER_FAB

    DragActionFab(
        state = state,
        icon = icon,
        label = label,
        containerColor = containerColor,
        contentColor = contentColor,
        cornerRadius = cornerRadius,
        onClick = onClick,
        onOptionSelected = onOptionSelected,
        modifier = modifier
            .align(if (isCenterFab) Alignment.BottomEnd else Alignment.BottomStart)
            .navigationBarsPadding()
            .then(
                if (isCenterFab) Modifier.padding(end = 24.dp, bottom = 8.dp)
                else Modifier.padding(start = 12.dp, bottom = 8.dp)
            ),
    )

    FabDragOptions(
        state = state,
        options = dragOptions,
    )
}
