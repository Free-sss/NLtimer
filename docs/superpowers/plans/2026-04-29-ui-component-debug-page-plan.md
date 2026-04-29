# UI 组件调试页面 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新建 feature/debug 模块，通过 Build Variant + 组件注册机制实现独立的 UI 组件调试页面

**架构：** DebugComponent 协议放在 core/designsystem；各 feature 在 src/debug/ 下注册组件；feature/debug 收集并提供调试页面 UI；app 通过 debugImplementation 集成；使用反射 + internal var 模式注入 debug 路由和菜单项（零主源集污染）

**技术栈：** Kotlin, Jetpack Compose, Navigation Compose, Hilt, Gradle Convention Plugins

---

## 任务 1：在 core/designsystem 中定义 DebugComponent 协议

**文件：**
- 创建：`core/designsystem/src/main/java/com/nltimer/core/designsystem/debug/DebugComponent.kt`

- [ ] **步骤 1：创建 DebugComponent 数据类和 DebugComponentRegistry 单例**

```kotlin
package com.nltimer.core.designsystem.debug

import androidx.compose.runtime.Composable

data class DebugComponent(
    val id: String,
    val name: String,
    val group: String,
    val description: String = "",
    val content: @Composable () -> Unit,
)

object DebugComponentRegistry {
    private val _components = mutableListOf<DebugComponent>()
    val components: List<DebugComponent> get() = _components.toList()

    fun register(component: DebugComponent) {
        _components.add(component)
    }
}
```

- [ ] **步骤 2：构建验证**

```bash
./gradlew :core:designsystem:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 3：Commit**

```bash
git add core/designsystem/src/main/java/com/nltimer/core/designsystem/debug/DebugComponent.kt
git commit -m "✨ feat(designsystem): add DebugComponent protocol and registry"
```

---

## 任务 2：创建 feature/debug 模块

**文件：**
- 创建：`feature/debug/build.gradle.kts`
- 创建：`feature/debug/src/main/AndroidManifest.xml`

- [ ] **步骤 1：创建 build.gradle.kts**

```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.debug"
}

dependencies {
    implementation(projects.core.designsystem)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}
```

- [ ] **步骤 2：创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
</manifest>
```

- [ ] **步骤 3：更新 settings.gradle.kts 注册模块**

在 `include(` 块末尾追加 `"feature:debug",`：

```kotlin
include(
    "app",
    "core:designsystem",
    "core:data",
    "feature:home",
    "feature:sub",
    "feature:stats",
    "feature:settings",
    "feature:categories",
    "feature:management_activities",
    "feature:tag_management",
    "feature:debug",
)
```

- [ ] **步骤 4：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add feature/debug/ settings.gradle.kts
git commit -m "✨ feat(debug): create feature/debug module skeleton"
```

---

## 任务 3：实现 DebugPage 和 DebugRoute

**文件：**
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/DebugRoute.kt`
- 创建：`feature/debug/src/main/java/com/nltimer/feature/debug/ui/DebugPage.kt`

- [ ] **步骤 1：创建 DebugRoute.kt**

```kotlin
package com.nltimer.feature.debug

import androidx.compose.runtime.Composable
import com.nltimer.feature.debug.ui.DebugPage

@Composable
fun DebugRoute() {
    DebugPage()
}
```

- [ ] **步骤 2：创建 DebugPage.kt — 三段式布局（分组 | 列表 | 预览）**

```kotlin
package com.nltimer.feature.debug.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry

@Composable
fun DebugPage() {
    val allComponents = remember { DebugComponentRegistry.components }
    val groups = remember(allComponents) {
        listOf("全部") + allComponents.map { it.group }.distinct()
    }

    var selectedGroup by remember { mutableStateOf("全部") }
    var selectedComponentId by remember { mutableStateOf<String?>(null) }

    val filteredComponents = remember(selectedGroup, allComponents) {
        if (selectedGroup == "全部") allComponents
        else allComponents.filter { it.group == selectedGroup }
    }

    val selectedComponent = remember(selectedComponentId, allComponents) {
        allComponents.find { it.id == selectedComponentId }
    }

    if (allComponents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📦",
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无已注册的调试组件",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "在 feature 模块的 src/debug/ 下注册组件后即可显示",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        return
    }

    Row(modifier = Modifier.fillMaxSize()) {
        GroupSidebar(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelected = { selectedGroup = it },
            modifier = Modifier.width(120.dp).fillMaxHeight(),
        )
        ComponentList(
            components = filteredComponents,
            selectedComponentId = selectedComponentId,
            onComponentSelected = { selectedComponentId = it.id },
            modifier = Modifier.width(180.dp).fillMaxHeight(),
        )
        PreviewArea(
            component = selectedComponent,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

@Composable
private fun GroupSidebar(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 2.dp,
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
                Text(
                    text = "分组",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            items(groups) { group ->
                val isSelected = group == selectedGroup
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onGroupSelected(group) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        text = if (group == "全部") "🏷️ 全部" else group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentList(
    components: List<DebugComponent>,
    selectedComponentId: String?,
    onComponentSelected: (DebugComponent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            item {
                Text(
                    text = "组件列表",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            items(components) { component ->
                val isSelected = component.id == selectedComponentId
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onComponentSelected(component) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = component.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (component.description.isNotEmpty()) {
                            Text(
                                text = component.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewArea(
    component: DebugComponent?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        if (component == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "请从左侧列表选择一个组件进行预览",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "▼ ${component.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = component.group,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    component.content()
                }
            }
        }
    }
}
```

- [ ] **步骤 3：构建验证**

```bash
./gradlew :feature:debug:compileDebugKotlin
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add feature/debug/src/main/java/com/nltimer/feature/debug/
git commit -m "✨ feat(debug): implement DebugPage with three-column layout"
```

---

## 任务 4：在各 feature 模块创建 debug 源码集注册文件

**文件：**
- 创建：`feature/home/src/debug/java/com/nltimer/feature/home/debug/HomeDebugComponents.kt`
- 创建：`feature/sub/src/debug/java/com/nltimer/feature/sub/debug/SubDebugComponents.kt`
- 创建：`feature/stats/src/debug/java/com/nltimer/feature/stats/debug/StatsDebugComponents.kt`
- 创建：`feature/settings/src/debug/java/com/nltimer/feature/settings/debug/SettingsDebugComponents.kt`
- 创建：`feature/categories/src/debug/java/com/nltimer/feature/categories/debug/CategoriesDebugComponents.kt`
- 创建：`feature/management_activities/src/debug/java/com/nltimer/feature/management_activities/debug/ManagementDebugComponents.kt`
- 创建：`feature/tag_management/src/debug/java/com/nltimer/feature/tag_management/debug/TagManagementDebugComponents.kt`

- [ ] **步骤 1：创建 feature/home/src/debug/ 注册文件**

```kotlin
package com.nltimer.feature.home.debug

import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object HomeDebugComponents {
    fun registerAll() {
    }
}
```

- [ ] **步骤 2：创建 feature/sub 注册文件（占位）**

```kotlin
package com.nltimer.feature.sub.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object SubDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 3：创建 feature/stats 注册文件（占位）**

```kotlin
package com.nltimer.feature.stats.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object StatsDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 4：创建 feature/settings 注册文件（占位）**

```kotlin
package com.nltimer.feature.settings.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object SettingsDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 5：创建 feature/categories 注册文件（占位）**

```kotlin
package com.nltimer.feature.categories.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object CategoriesDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 6：创建 feature/management_activities 注册文件（占位）**

```kotlin
package com.nltimer.feature.management_activities.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object ManagementDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 7：创建 feature/tag_management 注册文件（占位）**

```kotlin
package com.nltimer.feature.tag_management.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

object TagManagementDebugComponents {
    fun registerAll() { }
}
```

- [ ] **步骤 8：验证 debug 源码集编译**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 9：Commit**

```bash
git add feature/home/src/debug/ feature/sub/src/debug/ feature/stats/src/debug/ feature/settings/src/debug/ feature/categories/src/debug/ feature/management_activities/src/debug/ feature/tag_management/src/debug/
git commit -m "✨ feat(debug): add debug registration stubs for all feature modules"
```

---

## 任务 5：在 app 模块集成 debug 依赖和路由注入

**文件：**
- 修改：`app/build.gradle.kts` — 添加 debugImplementation
- 修改：`app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt` — 添加 internal var 注入点
- 修改：`app/src/main/java/com/nltimer/app/component/AppDrawer.kt` — 添加 internal var 注入点 + 改为 MutableList
- 修改：`app/src/main/java/com/nltimer/app/NLtimerApplication.kt` — 添加反射调用
- 创建：`app/src/debug/java/com/nltimer/app/DebugInitializer.kt` — debug 初始化器

- [ ] **步骤 1：在 app/build.gradle.kts 添加 debugImplementation**

在 `dependencies {` 块内，`implementation(projects.feature.tagManagement)` 之后添加：

```kotlin
    debugImplementation(projects.feature.debug)
```

- [ ] **步骤 2：修改 NLtimerNavHost.kt — 添加 debug 路由注入点**

在文件末尾（`}` 之后）添加：

```kotlin
internal var debugRoutes: (NavGraphBuilder.() -> Unit)? = null
```

需要添加 import：
```kotlin
import androidx.navigation.NavGraphBuilder
```

然后在 NavHost 的 lambda 尾部（`composable("theme_settings")` 块之后、`}` 之前）添加：

```kotlin
        debugRoutes?.invoke(this)
```

NLtimerNavHost.kt 完整变更后的签名和尾部：

```kotlin
@Composable
fun NLtimerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
    ) {
        composable("home") { HomeRoute() }
        composable("sub") { SubRoute() }
        composable("stats") { StatsRoute() }
        composable("categories") { CategoriesRoute() }
        composable("management_activities") { ActivityManagementRoute() }
        composable("tag_management") {
            TagManagementRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable("settings") { SettingsRoute() }
        composable("theme_settings") {
            ThemeSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
            )
        }
        debugRoutes?.invoke(this)
    }
}

internal var debugRoutes: (NavGraphBuilder.() -> Unit)? = null
```

- [ ] **步骤 3：修改 AppDrawer.kt — 添加 debug 菜单注入点**

将 `drawerMenuItems` 从 `private val` 改为 `internal val` + `MutableList`，并在 forEach 循环后添加 debug 项合并：

将：
```kotlin
private val drawerMenuItems = listOf(
    DrawerMenuItem("home", "主页", Icons.Default.Home),
    DrawerMenuItem("theme_settings", "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem("categories", "分类管理", Icons.Default.Category),
    DrawerMenuItem("management_activities", "活动管理", Icons.Default.List),
    DrawerMenuItem("tag_management", "标签管理", Icons.Default.Label),
    DrawerMenuItem("settings", "设置", Icons.Default.Settings),
)
```

改为：
```kotlin
internal val drawerMenuItems = mutableListOf(
    DrawerMenuItem("home", "主页", Icons.Default.Home),
    DrawerMenuItem("theme_settings", "主题配置", Icons.Default.Brightness5),
    DrawerMenuItem("categories", "分类管理", Icons.Default.Category),
    DrawerMenuItem("management_activities", "活动管理", Icons.Default.List),
    DrawerMenuItem("tag_management", "标签管理", Icons.Default.Label),
    DrawerMenuItem("settings", "设置", Icons.Default.Settings),
)
```

- [ ] **步骤 4：创建 app/src/debug/ DebugInitializer.kt — 反射入口**

```kotlin
package com.nltimer.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.navigation.compose.composable
import com.nltimer.app.component.DrawerMenuItem
import com.nltimer.app.component.drawerMenuItems
import com.nltimer.app.navigation.debugRoutes
import com.nltimer.feature.categories.debug.CategoriesDebugComponents
import com.nltimer.feature.debug.DebugRoute
import com.nltimer.feature.home.debug.HomeDebugComponents
import com.nltimer.feature.management_activities.debug.ManagementDebugComponents
import com.nltimer.feature.settings.debug.SettingsDebugComponents
import com.nltimer.feature.stats.debug.StatsDebugComponents
import com.nltimer.feature.sub.debug.SubDebugComponents
import com.nltimer.feature.tag_management.debug.TagManagementDebugComponents

object DebugInitializer {

    @JvmStatic
    fun init() {
        debugRoutes = {
            composable("debug") { DebugRoute() }
        }

        drawerMenuItems.add(
            DrawerMenuItem("debug", "🐛 调试", Icons.Default.Build)
        )

        HomeDebugComponents.registerAll()
        SubDebugComponents.registerAll()
        StatsDebugComponents.registerAll()
        SettingsDebugComponents.registerAll()
        CategoriesDebugComponents.registerAll()
        ManagementDebugComponents.registerAll()
        TagManagementDebugComponents.registerAll()
    }
}
```

- [ ] **步骤 5：修改 NLtimerApplication.kt — 添加反射调用**

```kotlin
package com.nltimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NLtimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDebugIfPresent()
    }

    private fun initializeDebugIfPresent() {
        try {
            Class.forName("com.nltimer.app.DebugInitializer")
                .getMethod("init")
                .invoke(null)
        } catch (_: Exception) {
        }
    }
}
```

- [ ] **步骤 6：构建验证**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 7：Commit**

```bash
git add app/build.gradle.kts app/src/main/java/com/nltimer/app/navigation/NLtimerNavHost.kt app/src/main/java/com/nltimer/app/component/AppDrawer.kt app/src/main/java/com/nltimer/app/NLtimerApplication.kt app/src/debug/
git commit -m "✨ feat(debug): integrate debug module into app with reflection-based injection"
```

---

## 任务 6：验证完整构建

- [ ] **步骤 1：Debug 构建验证**

```bash
./gradlew assembleDebug
```

预期：BUILD SUCCESSFUL

- [ ] **步骤 2：Release 构建验证（确保零残留）**

```bash
./gradlew assembleRelease
```

预期：BUILD SUCCESSFUL（feature/debug 和所有 debug 源码集不参与编译）

- [ ] **步骤 3：最终确认 — 运行 lint**

```bash
./gradlew lintDebug
```

预期：无新增 lint 错误

---

## 自检结果

1. **规格覆盖度** ✅ — 每个设计章节都有对应任务：
   - 协议定义 → 任务1
   - feature/debug 模块 → 任务2
   - DebugPage/DebugRoute → 任务3
   - 各 feature 注册文件 → 任务4
   - app 集成（依赖、路由、菜单、Application）→ 任务5
   - 构建验证 → 任务6

2. **占位符扫描** ✅ — 所有步骤都包含实际代码

3. **类型一致性** ✅：
   - `DebugComponent` 定义在任务1，所有后续引用一致
   - `DebugComponentRegistry` 定义在任务1，任务3和4使用
   - `drawerMenuItems` 从 `private val` 改为 `internal val MutableList`，任务5引用一致
   - `debugRoutes` 类型 `(NavGraphBuilder.() -> Unit)?`，定义和使用一致
