# NLtimer 界面优化与开发准备设计规格

## 概述

为 NLtimer 应用进行界面布局优化和开发准备工作，包括侧边栏宽度调整、预测性返回功能启用，以及在各组件和页面中添加规范的 Todo 注释标记体系。

## 设计决策

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 侧边栏宽度 | 50% 屏幕宽度 | 用户需求明确指定，提供更平衡的布局体验 |
| 返回功能 | Android Predictive Back | 系统级预测性返回，支持预览和取消，提升用户体验 |
| Todo 标记体系 | 逻辑/样式/其他三类 | 清晰划分职责，为后续正式开发提供指导框架 |
| 最小宽度限制 | 280dp | 保证在小屏设备上侧边栏内容可读性 |
| 最大宽度限制 | 屏幕宽度 50% | 遵循用户需求，大屏设备也能合理使用 |

## 修改清单

### 1. AndroidManifest.xml - 启用预测性返回

```xml
<application 
    android:enableOnBackInvokedCallback="true">
```

### 2. AppDrawer.kt - 侧边栏宽度调整 + Todo 标记

**文件路径:** `app/src/main/java/com/nltimer/app/component/AppDrawer.kt`

**宽度实现:**
- 使用 `Modifier.fillMaxWidth(0.5f)` 占据 50% 屏幕宽度
- 添加最小宽度限制 `Modifier.widthIn(min = 280.dp, max = LocalConfiguration.current.screenWidthDp.dp / 2)`
- 确保在小屏和大屏上都有良好表现

**Todo 标记体系:**

```kotlin
// Todo 逻辑 - 侧边栏菜单项点击事件处理、状态管理、导航逻辑
// Todo 样式 - 侧边栏布局优化、间距调整、动画效果完善
// Todo... - 无障碍支持、主题适配、多语言支持等其他开发事项
```

### 3. HomeScreen.kt - 主页 Todo 标记

**文件路径:** `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

```kotlin
// Todo 逻辑 - 主页数据加载、计时器状态显示、用户交互处理
// Todo 样式 - 主页布局优化、视觉层次调整、响应式适配
// Todo... - 性能优化、测试用例、错误处理等开发事项
```

### 4. SubScreen.kt - 副页 Todo 标记

**文件路径:** `feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt`

```kotlin
// Todo 逻辑 - 副页功能实现、数据绑定、业务逻辑处理
// Todo 样式 - 副页界面优化、组件对齐、视觉一致性
// Todo... - 功能完善、测试覆盖、边界情况处理等开发事项
```

### 5. StatsScreen.kt - 统计页 Todo 标记

**文件路径:** `feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt`

```kotlin
// Todo 逻辑 - 统计数据计算、图表数据准备、用户筛选交互
// Todo 样式 - 统计页面布局、图表样式、数据可视化优化
// Todo... - 性能调优、大数据量处理、导出功能等开发事项
```

### 6. SettingsScreen.kt - 设置页 Todo 标记

**文件路径:** `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`

```kotlin
// Todo 逻辑 - 设置项数据管理、偏好设置存储、开关状态同步
// Todo 样式 - 设置列表布局、分组样式、交互反馈优化
// Todo... - 设置项扩展、导入导出、重置功能等开发事项
```

## 组件设计详述

### AppDrawer.kt 实现方案

```kotlin
@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Todo 样式 - 侧边栏宽度调整为 50% 屏幕宽度，保持美观性
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    ModalDrawerSheet(
        modifier = modifier
            .widthIn(
                min = 280.dp,
                max = (screenWidth * 0.5f).coerceAtLeast(280.dp)
            )
    ) {
        // Todo 逻辑 - 侧边栏标题区域，应用名称和图标显示
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Todo 逻辑 - 菜单项点击事件处理，导航路由跳转
        // Todo 样式 - 菜单项选中状态样式、图标和文字对齐
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
        
        // Todo... - 更多菜单项、分隔线、底部信息区域等
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
```

### 预测性返回集成

在 `MainActivity.kt` 中已确保 AndroidManifest 启用：
```xml
android:enableOnBackInvokedCallback="true"
```

Compose 中侧边栏关闭行为会自动适配系统返回手势：
- 侧边栏打开时，返回手势优先关闭侧边栏
- 侧边栏关闭时，返回手势处理导航栈回退

## 模块依赖关系

无变更，保持现有模块结构。

## 开发要点

1. **响应式布局**：侧边栏宽度使用 50% 屏幕宽度，但设置最小宽度 280dp 保证可读性
2. **预测性返回**：通过 AndroidManifest 配置启用，系统自动处理返回手势和预览动画
3. **Todo 标记规范**：统一使用 `// Todo 逻辑`、`// Todo 样式`、`// Todo...` 三类标记
4. **代码稳定性**：仅添加注释和宽度调整，不改变现有功能逻辑
5. **多设备适配**：宽度计算考虑屏幕尺寸变化，兼顾手机和平板设备

## 验证标准

1. 侧边栏打开时宽度为屏幕 50%（最小 280dp）
2. 预测性返回手势在 Android 13+ 设备上可用
3. 所有 6 个文件（AppDrawer + 4 个页面 + AndroidManifest）包含规范的 Todo 标记
4. 现有功能不受影响，编译通过
