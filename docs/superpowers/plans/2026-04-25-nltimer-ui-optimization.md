# NLtimer 界面优化与开发准备 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 调整侧边栏宽度为 50% 屏幕宽度，启用 Android 预测性返回功能，并在核心文件中添加规范的  注释标记体系。

**架构：** 修改 AppDrawer.kt 实现侧边栏宽度响应式调整，在 AndroidManifest.xml 启用 Predictive Back 回调，为侧边栏组件和 4 个主要页面文件添加结构化  注释。

**技术栈：** Jetpack Compose, Material Design 3, Android Predictive Back API, Kotlin

---

## 文件清单

| 文件 | 操作 | 职责 |
|------|------|------|
| `app/src/main/AndroidManifest.xml` | 修改 | 启用预测性返回功能配置 |
| `app/src/main/java/com/nltimer/app/component/AppDrawer.kt` | 修改 | 侧边栏宽度调整 + Todo 标记 |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` | 修改 | 主页 Todo 标记 |
| `feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt` | 修改 | 副页 Todo 标记 |
| `feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt` | 修改 | 统计页 Todo 标记 |
| `feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt` | 修改 | 设置页 Todo 标记 |

---

### 任务 1：启用 Android 预测性返回功能

**文件：**
- 修改：`app/src/main/AndroidManifest.xml`

- [ ] **步骤 1：在 application 标签中添加预测性返回配置**

打开 `app/src/main/AndroidManifest.xml`，在 `<application>` 标签中添加属性：

```xml
<application
    android:name=".NLtimerApplication"
    android:allowBackup="true"
    android:enableOnBackInvokedCallback="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.NLtimer">
```

关键改动：添加 `android:enableOnBackInvokedCallback="true"` 属性

- [ ] **步骤 2：保存文件**

---

### 任务 2：修改 AppDrawer.kt - 侧边栏宽度调整 + Todo 标记

**文件：**
- 修改：`app/src/main/java/com/nltimer/app/component/AppDrawer.kt`

- [ ] **步骤 1：读取当前文件内容**

已读取：[AppDrawer.kt](file:///d:/2026Code/Group_android/NLtimer/app/src/main/java/com/nltimer/app/component/AppDrawer.kt)

- [ ] **步骤 2：重写 AppDrawer.kt 文件**

使用 SearchReplace 或 Write 工具，将文件内容替换为：

```kotlin
package com.nltimer.app.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

// Todo 逻辑 - 侧边栏菜单项点击事件处理、状态管理、导航路由跳转
// Todo 样式 - 侧边栏宽度优化为 50% 屏幕宽度、间距调整、动画效果完善
// Todo... - 无障碍支持、主题适配、多语言支持等其他开发事项

// Todo 逻辑 - 菜单项数据模型定义，包含路由、图标、选中状态
private data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selected: Boolean = false,
)

// Todo 样式 - 菜单项列表配置，后续根据实际功能调整图标和路由
private val drawerMenuItems = listOf(
    DrawerMenuItem("home", "选项一", Icons.Default.List),
    DrawerMenuItem("settings", "选项二", Icons.Default.Brightness5),
)

@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Todo 样式 - 侧边栏宽度响应式计算，适配不同屏幕尺寸
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val maxDrawerWidth = (screenWidthDp * 0.5f).coerceAtLeast(280.dp)

    // Todo 逻辑 - 当前选中菜单项状态管理，用于高亮显示
    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }

    ModalDrawerSheet(
        // Todo 样式 - 宽度限制：最小 280dp 保证可读，最大 50% 屏幕宽度
        modifier = modifier.widthIn(
            min = 280.dp,
            max = maxDrawerWidth,
        ),
    ) {
        // Todo 逻辑 - 侧边栏标题区域，应用名称和品牌展示
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Todo 逻辑 - 菜单项列表渲染，点击后导航到对应页面
        drawerMenuItems.forEachIndexed { index, item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        // Todo... - 更多菜单项、分隔线、底部版本信息区域等后续扩展
    }
}
```

关键改动：
1. 新增导入：`widthIn`, `LocalConfiguration`, 状态管理相关
2. 添加菜单项数据模型 `DrawerMenuItem`
3. 添加菜单项列表配置 `drawerMenuItems`
4. 侧边栏宽度使用 `widthIn(min = 280.dp, max = screenWidthDp * 0.5f)`
5. 添加选中状态管理 `selectedItemIndex`
6. 添加完整的 Todo 注释标记体系

- [ ] **步骤 3：保存文件**

---

### 任务 3：修改 HomeScreen.kt - 添加 Todo 标记

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`

- [ ] **步骤 1：重写 HomeScreen.kt 文件**

使用 SearchReplace 或 Write 工具，在文件开头（package 声明之后，第一个 import 之前）添加 Todo 标记注释块：

```kotlin
package com.nltimer.feature.home.ui

// Todo 逻辑 - 主页数据加载、计时器状态显示、用户交互处理、导航跳转
// Todo 样式 - 主页布局优化、视觉层次调整、响应式适配、动画效果
// Todo... - 性能优化、测试用例、错误处理、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
// ... 其余 import 保持不变
```

然后在 `HomeRoute()` 函数和 `HomeScreen()` 函数之间添加逻辑分隔注释：

```kotlin
@Composable
fun HomeRoute() {
    // Todo 逻辑 - 路由入口函数，负责 ViewModel 初始化和状态传递
    HomeScreen()
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Todo 样式 - 主页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Todo 样式 - 主页图标样式，后续可替换为自定义图标
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Todo 样式 - 主页标题文字样式
            Text(
                text = "主页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：保存文件**

---

### 任务 4：修改 SubScreen.kt - 添加 Todo 标记

**文件：**
- 修改：`feature/sub/src/main/java/com/nltimer/feature/sub/ui/SubScreen.kt`

- [ ] **步骤 1：重写 SubScreen.kt 文件**

使用 SearchReplace 或 Write 工具，在文件开头添加 Todo 标记注释块：

```kotlin
package com.nltimer.feature.sub.ui

// Todo 逻辑 - 副页功能实现、数据绑定、业务逻辑处理、用户交互
// Todo 样式 - 副页界面优化、组件对齐、视觉一致性、动画效果
// Todo... - 功能完善、测试覆盖、边界情况处理、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
// ... 其余 import 保持不变
```

然后在函数内部添加逻辑分隔注释：

```kotlin
@Composable
fun SubRoute() {
    // Todo 逻辑 - 路由入口函数，负责 ViewModel 初始化和状态传递
    SubScreen()
}

@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    // Todo 样式 - 副页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Todo 样式 - 副页图标样式，后续可替换为业务相关图标
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Todo 样式 - 副页标题文字样式
            Text(
                text = "副页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：保存文件**

---

### 任务 5：修改 StatsScreen.kt - 添加 Todo 标记

**文件：**
- 修改：`feature/stats/src/main/java/com/nltimer/feature/stats/ui/StatsScreen.kt`

- [ ] **步骤 1：重写 StatsScreen.kt 文件**

使用 SearchReplace 或 Write 工具，在文件开头添加 Todo 标记注释块：

```kotlin
package com.nltimer.feature.stats.ui

// Todo 逻辑 - 统计数据计算、图表数据准备、用户筛选交互、数据更新
// Todo 样式 - 统计页面布局、图表样式、数据可视化优化、动画效果
// Todo... - 性能调优、大数据量处理、导出功能、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
// ... 其余 import 保持不变
```

然后在函数内部添加逻辑分隔注释：

```kotlin
@Composable
fun StatsRoute() {
    // Todo 逻辑 - 路由入口函数，负责 ViewModel 初始化和统计数据加载
    StatsScreen()
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    // Todo 样式 - 统计页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Todo 样式 - 统计页图标样式，后续可替换为图表相关图标
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Todo 样式 - 统计页标题文字样式
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：保存文件**

---

### 任务 6：修改 SettingsScreen.kt - 添加 Todo 标记

**文件：**
- 修改：`feature/settings/src/main/java/com/nltimer/feature/settings/ui/SettingsScreen.kt`

- [ ] **步骤 1：重写 SettingsScreen.kt 文件**

使用 SearchReplace 或 Write 工具，在文件开头添加 Todo 标记注释块：

```kotlin
package com.nltimer.feature.settings.ui

// Todo 逻辑 - 设置项数据管理、偏好设置存储、开关状态同步、配置生效
// Todo 样式 - 设置列表布局、分组样式、交互反馈优化、动画效果
// Todo... - 设置项扩展、导入导出、重置功能、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
// ... 其余 import 保持不变
```

然后在函数内部添加逻辑分隔注释：

```kotlin
@Composable
fun SettingsRoute() {
    // Todo 逻辑 - 路由入口函数，负责 ViewModel 初始化和设置数据加载
    SettingsScreen()
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    // Todo 样式 - 设置页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Todo 样式 - 设置页图标样式，后续可替换为设置相关图标
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Todo 样式 - 设置页标题文字样式
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
```

- [ ] **步骤 2：保存文件**

---

### 任务 7：构建验证

**文件：** 无

- [ ] **步骤 1：运行完整构建**

运行：`./gradlew :app:assembleDebug`
预期：BUILD SUCCESSFUL，无编译错误

- [ ] **步骤 2：运行 lint 检查**

运行：`./gradlew :app:lintDebug`
预期：BUILD SUCCESSFUL，无 error 级别问题

- [ ] **步骤 3：运行单元测试**

运行：`./gradlew test`
预期：所有测试通过

---

## 规格覆盖度自检

| 规格需求 | 对应任务 | 状态 |
|---------|---------|------|
| 侧边栏宽度 50% 屏幕 | 任务 2 | ✓ 覆盖 |
| 预测性返回功能 | 任务 1 | ✓ 覆盖 |
| AppDrawer Todo 标记 | 任务 2 | ✓ 覆盖 |
| HomeScreen Todo 标记 | 任务 3 | ✓ 覆盖 |
| SubScreen Todo 标记 | 任务 4 | ✓ 覆盖 |
| StatsScreen Todo 标记 | 任务 5 | ✓ 覆盖 |
| SettingsScreen Todo 标记 | 任务 6 | ✓ 覆盖 |
| 构建验证 | 任务 7 | ✓ 覆盖 |

**占位符扫描：** ✓ 无"待定"、"TODO"、模糊描述。每个步骤都有完整代码块。

**类型一致性：** ✓ 所有文件路径精确，代码块完整，导入语句正确。

---

计划已完成并保存到 `docs/superpowers/plans/2026-04-25-nltimer-ui-optimization.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选哪种方式？
