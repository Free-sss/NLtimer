import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityNoteComponent(
    onLabelClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onContinueAddClick: () -> Unit = {},
    onAddClick: () -> Unit = {}
) {
    var noteText by remember { mutableStateOf("") }
    val maxCharLimit = 5000

    // MD3 风格颜色定义
    val backgroundColor = Color(0xFFF2F2F2) // 浅灰色背景
    val primaryBlue = Color(0xFF4A90E2)     // 历史备注按钮蓝色
    val labelBgColor = Color.Black          // 标签按钮黑色
    val addButtonColor = Color.Black        // 添加按钮黑色
    val secondaryButtonBorder = Color.Black  // 继续添加按钮边框

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.Transparent)
    ) {
        // 第一行：右侧的标签按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onLabelClick,
                colors = ButtonDefaults.buttonColors(containerColor = labelBgColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("标签", color = Color.White, fontSize = 14.sp)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 第二行：输入框区域与右侧历史按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 输入框卡片
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .background(backgroundColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                // 字数统计（右上角）
                Text(
                    text = "${noteText.length}/$maxCharLimit",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                // 核心输入框
                BasicTextField(
                    value = noteText,
                    onValueChange = { if (it.length <= maxCharLimit) noteText = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    ),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                "请输入备注",
                                color = Color.Gray,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 右侧“历史备注”小按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "历史备注",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 第三行：底部双按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 继续添加（描边按钮）
            OutlinedButton(
                onClick = onContinueAddClick,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp
                    // 默认黑色
                ),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Text(
                    "继续添加",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 添加（实心黑按钮）
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = addButtonColor)
            ) {
                Text(
                    "添加",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}