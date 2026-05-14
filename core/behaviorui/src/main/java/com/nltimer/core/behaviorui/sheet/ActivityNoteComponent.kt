package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.theme.styledAlpha

@Composable
fun NoteInputComponent(
    note: String,
    onNoteChange: (String) -> Unit,
    onTopButton: () -> Unit = {},
    onBottomButton: () -> Unit = {},
) {
    val maxCharLimit = 5000

    val backgroundColor = MaterialTheme.colorScheme.surface
    val primaryBlue = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(99.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { if (it.length <= maxCharLimit) onNoteChange(it) },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = styledAlpha(0.7f))
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "${note.length}/$maxCharLimit",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = styledAlpha(0.3f)),
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(y = (-12).dp, x = 8.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 4.dp)
                                
                            ) {
                                if (note.isEmpty()) {
                                    Text(
                                        text = "请输入备注",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = styledAlpha(0.3f)),
                                        fontSize = 14.sp,
                                        modifier = Modifier.offset(y = (-4).dp),
                                    )
                                }
                                innerTextField()
                            }
                        }
                    })
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.height(99.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                ActionIconButton(
                    icon = Icons.Default.AutoFixHigh,
                    label = "智能识别",
                    onClick = onTopButton,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
                ActionIconButton(
                    icon = Icons.Default.Description,
                    label = "占位拓展",
                    onClick = onBottomButton,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.size(48.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
