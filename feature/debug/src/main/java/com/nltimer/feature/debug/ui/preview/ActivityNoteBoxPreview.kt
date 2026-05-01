package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityNoteBoxDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ActivityNoteComponent(
                onHistoryClick = { },
            )
        }
    }
}

@Composable
internal fun ActivityNoteComponent(
    onHistoryClick: () -> Unit = {},
) {
    var noteText by remember { mutableStateOf("") }
    val maxCharLimit = 5000

    val backgroundColor =  MaterialTheme.colorScheme.surface
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
                    .height(110.dp)
                    .background(backgroundColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= maxCharLimit) noteText = it },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 右上角字数统计
                            Text(
                                text = "${noteText.length}/$maxCharLimit",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), 
                                fontSize = 9.sp,
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .offset(y = (-10).dp,x=8.dp)
                        
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 4.dp)
                            ) {
                                // 输入框提示文本
                                if (noteText.isEmpty()) {
                                    Text(
                                        "请输入备注",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        fontSize = 14.sp,
                                    )
                                }
                                innerTextField()
                            }
                        }
                    })
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onHistoryClick,
                color = primaryBlue,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "历史备注",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,   
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
