package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.input.key.onKeyEvent
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
                onLabelClick = { },
                onHistoryClick = { },
                onContinueAddClick = { },
                onAddClick = { })
        }
    }
}

@Composable
internal fun ActivityNoteComponent(
    onLabelClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onContinueAddClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    var noteText by remember { mutableStateOf("") }
    val maxCharLimit = 5000

    val backgroundColor = Color(0xFF3B3B4D) // Dark gray/blue for the box background
    val primaryBlue = Color(0xFFC7C7FF) // Light purple for history button
    val labelBgColor = Color(0xFF8CD1FF) // Light blue for label button

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onLabelClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = labelBgColor,
                    contentColor = Color(0xFF1A1C1E)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("标签", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                        color = Color.White.copy(alpha = 0.7f)
                    ),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 右上角字数统计
                            Text(
                                text = "${noteText.length}/$maxCharLimit",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.TopEnd)
                                    .offset(y = (-13).dp,x=4.dp)
                        
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
                                        color = Color.White.copy(alpha = 0.3f),
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
                        tint = Color(0xFF1A1C1E),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "历史备注",
                        color = Color(0xFF1A1C1E),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        //Spacer(modifier = Modifier.height(12.dp))

        // 暂时取消添加按钮
        // Row(
        //     modifier = Modifier.fillMaxWidth(),
        //     horizontalArrangement = Arrangement.spacedBy(8.dp)
        // ) {
        //     OutlinedButton(
        //         onClick = onContinueAddClick,
        //         modifier = Modifier
        //             .weight(1f)
        //             .height(36.dp),
        //         shape = RoundedCornerShape(6.dp),
        //         border = BorderStroke(1.dp, Color.Black),
        //         colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
        //     ) {
        //         Text(
        //             "继续添加",
        //             fontSize = 13.sp,
        //             fontWeight = FontWeight.Bold
        //         )
        //     }

        //     Button(
        //         onClick = onAddClick,
        //         modifier = Modifier
        //             .weight(1f)
        //             .height(36.dp),
        //         shape = RoundedCornerShape(6.dp),
        //         colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        //     ) {
        //         Text(
        //             "添加",
        //             fontSize = 13.sp,
        //             fontWeight = FontWeight.Bold,
        //             color = Color.White
        //         )
        //     }
        // }
    }
}
