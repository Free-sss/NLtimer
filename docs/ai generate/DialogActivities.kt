import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 活动数据模型
 * @param name 活动名称
 * @param color 活动的主题色（用于边框和文字）
 */
data class Activity(
    val name: String,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityGridComponent(
    activities: List<Activity>,
    onActivityClick: (Activity) -> Unit,
    onManageClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 使用 FlowRow 实现自动换行的流式布局
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), // 左右间距
        verticalArrangement = Arrangement.spacedBy(4.dp),  // 上下间距
        maxItemsInEachRow = 4 // 尽量匹配截图中的每行数量
    ) {
        // 渲染活动列表
        activities.forEach { activity ->
            ActivityChip(
                activity = activity,
                onClick = { onActivityClick(activity) }
            )
        }
        
        // 固定功能按钮：管理（倒数第二个）
        FunctionChip(
            label = "管理",
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "管理",
                    modifier = Modifier.size(18.dp)
                )
            },
            containerColor = Color(0xFF616161).copy(alpha = 0.15f),
            contentColor = Color(0xFF616161).copy(alpha = 0.9f),
            borderColor = Color(0xFF616161).copy(alpha = 0.5f),
            onClick = onManageClick
        )
        
        // 固定功能按钮：新增（倒数第一个）
        FunctionChip(
            label = "新增",
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "新增",
                    modifier = Modifier.size(18.dp)
                )
            },
            containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
            contentColor = Color(0xFFFF5252).copy(alpha = 0.9f),
            borderColor = Color(0xFFFF5252).copy(alpha = 0.5f),
            onClick = onAddClick
        )
    }
}

@Composable
fun ActivityChip(
    activity: Activity,
    onClick: () -> Unit
) {
    // 根据 MD3 规范，使用轻量级的背景色
    val containerColor = activity.color.copy(alpha = 0.15f)
    val contentColor = activity.color.copy(alpha = 0.9f)
    val borderColor = activity.color.copy(alpha = 0.5f)

    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = activity.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = borderColor,
            borderWidth = 1.5.dp
        ),
        shape = MaterialTheme.shapes.medium // MD3 中等圆角
    )
}

@Composable
fun FunctionChip(
    label: String,
    icon: @Composable (() -> Unit)? = null,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        },
        icon = icon,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = borderColor,
            borderWidth = 1.5.dp
        ),
        shape = MaterialTheme.shapes.medium // MD3 中等圆角
    )
}

/**
 * 预览与示例数据
 */
@Composable
fun ActivityGridPreview() {
    val sampleActivities = listOf(
        Activity("学习", Color(0xFF1B5E20)),
        Activity("读书", Color(0xFF43A047)),
        Activity("英语", Color(0xFF66BB6A)),
        Activity("c语言", Color(0xFF81C784)),
        Activity("微积分", Color(0xFF757575)),
        Activity("休息", Color(0xFF212121)),
        Activity("睡觉", Color(0xFF00BFA5)),
        Activity("Take a nap", Color(0xFF4DB6AC)),
        Activity("冥想", Color(0xFF006064)),
        Activity("信息流👺", Color(0xFFB71C1C)),
        Activity("生活", Color(0xFF8D6E63)),
        Activity("厕所", Color(0xFFD7CCC8)),
        Activity("金刚功", Color(0xFF795548)),
        Activity("吃饭", Color(0xFFD2B48C)),
        Activity("多巴胺🧠", Color(0xFFB8860B))
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ActivityGridComponent(
                activities = sampleActivities,
                onActivityClick = { activity -> 
                    // 处理活动点击
                },
                onManageClick = { 
                    // 跳转到管理页面
                },
                onAddClick = { 
                    // 打开添加活动对话框/页面
                }
            )
        }
    }
}