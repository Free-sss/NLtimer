import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActivityScreen(
    onBackClick: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    // 状态管理
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // 定义截图中特定的颜色
    val screenBackgroundColor = Color(0xFFF7F8FA) // 页面浅灰背景
    val cardBackgroundColor = Color.White
    val primaryBlue = Color(0xFF007AFF) // 主题亮蓝色
    val lightBlueBg = Color(0xFFEAF2FF) // 浅蓝色药丸背景
    val inputBackgroundColor = Color(0xFFF5F6F8) // 输入框浅灰背景
    val labelColor = Color(0xFF555555) // 标签深灰色
    val placeholderColor = Color(0xFFAAAAAA) // 占位符浅灰色

    Scaffold(
        containerColor = screenBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "增加活动", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "返回",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = screenBackgroundColor // 沉浸式背景
                )
            )
        },
        bottomBar = {
            // 底部悬浮按钮区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onSubmitClick,
                    modifier = Modifier
                        .fillMaxWidth(0.65f) // 按钮宽度占比
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp, // 模拟截图中的阴影
                        pressedElevation = 2.dp
                    ),
                    shape = RoundedCornerShape(28.dp) // 全圆角/体育场形状
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "提交",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "增加活动", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        // 主体可滑动内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 第一组：图标与颜色
            SectionCard(backgroundColor = cardBackgroundColor) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 图标部分
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("图标", color = labelColor, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(24.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(inputBackgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📖", fontSize = 24.sp)
                        }
                    }
                    
                    // 颜色部分
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("颜色", color = labelColor, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(24.dp))
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A90E2)) // 截图中的浅蓝色球
                        )
                    }
                }
            }

            // 第二组：名称与备注输入框
            SectionCard(backgroundColor = cardBackgroundColor) {
                Column {
                    TextInputRow(
                        label = "名称",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "请输入",
                        labelColor = labelColor,
                        inputBgColor = inputBackgroundColor,
                        placeholderColor = placeholderColor
                    )
                    // 分割线（极浅）
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = screenBackgroundColor
                    )
                    TextInputRow(
                        label = "备注",
                        value = note,
                        onValueChange = { note = it },
                        placeholder = "请输入",
                        labelColor = labelColor,
                        inputBgColor = inputBackgroundColor,
                        placeholderColor = placeholderColor
                    )
                }
            }

            // 第三组：关联标签与关键词
            SectionCard(backgroundColor = cardBackgroundColor) {
                Column {
                    ActionRow(
                        label = "关联标签",
                        actionText = "+ 增加",
                        showHelp = true,
                        labelColor = labelColor,
                        actionBgColor = lightBlueBg,
                        actionTextColor = primaryBlue
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        thickness = 0.5.dp,
                        color = screenBackgroundColor
                    )
                    ActionRow(
                        label = "关键词",
                        actionText = "+ 增加",
                        showHelp = true,
                        labelColor = labelColor,
                        actionBgColor = lightBlueBg,
                        actionTextColor = primaryBlue
                    )
                }
            }

            // 第四组：所属分类
            SectionCard(backgroundColor = cardBackgroundColor) {
                ActionRow(
                    label = "所属分类",
                    actionText = "未分类",
                    showHelp = false,
                    labelColor = labelColor,
                    actionBgColor = lightBlueBg,
                    actionTextColor = primaryBlue
                )
            }

            // 底部留白，防止被浮动按钮遮挡
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 封装的圆角白色卡片容器
@Composable
fun SectionCard(
    backgroundColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

// 封装的带灰色背景的输入框行
@Composable
fun TextInputRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    labelColor: Color,
    inputBgColor: Color,
    placeholderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(60.dp) // 固定左侧标签宽度以便对齐
        )
        Spacer(modifier = Modifier.width(16.dp))
        
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .background(inputBgColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(text = placeholder, color = placeholderColor)
                }
                innerTextField() // 核心文本框展示
            }
        )
    }
}

// 封装的操作行 (带右侧浅蓝色药丸按钮)
@Composable
fun ActionRow(
    label: String,
    actionText: String,
    showHelp: Boolean,
    labelColor: Color,
    actionBgColor: Color,
    actionTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = labelColor,
            fontWeight = FontWeight.Medium
        )
        
        if (showHelp) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = "帮助信息",
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 右侧操作“药丸”按钮
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(actionBgColor)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = actionText,
                color = actionTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}