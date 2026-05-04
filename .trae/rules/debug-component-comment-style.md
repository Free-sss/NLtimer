---
alwaysApply: false
description: 调试debug模块以及更新debug模块时
---
# Debug 组件注释规范

添加调试组件时必须遵循本注释规范，确保代码可读性和一致性。

## 注释分为两层

### 第一层：KDoc 函数文档注释（`/** ... */`）

每个 `@Composable` 函数必须附带 KDoc，描述函数的功能、使用场景和关键参数。

```kotlin
/**
 * 时间步进调节器调试预览入口
 * 展示当前时间并提供一个水平步进式按钮组来调节时间，
 * 点击 +-N 按钮对时间进行分钟级增减，点击"现在"按钮重置为系统当前时间
 */
@Composable
fun TimeAdjustmentDebugPreview() { ... }
```

KDoc 要求：
- **首行**：一句话概述函数功能
- **后续行**：补充关键行为和使用场景的简要描述
- **@param**：对每个参数进行简要说明，特别是回调参数
- 使用 `[ClassName]` 语法引用类名

### 第二层：行内注释（`//`）

在函数体内部，对关键代码块用中文行内注释解释其意图。注释放在对应代码块**上方**，与代码保持相同的缩进级别。

```kotlin
@Composable
private fun TimeAdjustmentComponent(...) {
    // 定义时间调整选项：-30, -5, -1, 1, 5, 30 分钟
    val adjustments = listOf(-30, -5, -1, 1, 5, 30)

    // 使用 Row 和 horizontalScroll 确保在小屏幕上能够横向滑动
    Row(...) {
        // 遍历生成时间步进按钮
        adjustments.forEach { amount ->
            // 正值前加 + 号，负值自带 - 号
            val text = if (amount > 0) "+$amount" else "$amount"
            TimeButton(
                text = text,
                onClick = {
                    // 核心逻辑：对传入的时间进行分钟级增减
                    onTimeChanged(currentTime.plusMinutes(amount.toLong()))
                },
            )
        }

        // "现在"按钮：重置为系统当前时间
        TimeButton(
            text = "现在",
            onClick = {
                // 核心逻辑：重置为当前最新系统时间
                onTimeChanged(LocalDateTime.now())
            },
        )
    }
}
```

行内注释要求：
- **中文**
- **解释意图而非复述代码**："定义时间调整选项" ✅，`val adjustments = listOf(...)` ❌
- **覆盖关键逻辑块**：变量定义、布局容器、循环体、分支条件、核心算法
- **不注释显而易见的代码**：`Spacer(modifier = ...)` 不需要注释

## 示例模板

```kotlin
/**
 * [组件名]调试预览入口
 * 一句话描述预览组件的功能和用途
 */
@Composable
fun XxxDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        // 初始化状态
        var state by remember { mutableStateOf(initialValue) }

        Column(...) {
            // 顶部区域描述
            HeaderSection(...)

            // 中间内容区域描述
            ContentSection(...)
        }
    }
}

/**
 * [子组件名]
 * 功能描述
 *
 * @param param1 参数1说明
 * @param param2 参数2说明
 * @param modifier 可选的修饰符
 */
@Composable
private fun SubComposable(
    param1: Type1,
    param2: Type2,
    modifier: Modifier = Modifier,
) {
    // 关键变量声明说明
    val computed = ...

    // 布局容器说明
    Row(...) {
        // 子项说明
        ...
    }
}
```

## 注册文件注释

`XxxDebugComponents.kt` 注册文件使用 KDoc 描述模块的调试组件集合：

```kotlin
/**
 * feature/xxx 模块的调试组件注册器
 * 将本模块内待调试的 Composable 组件注册到 [DebugComponentRegistry]，
 * 使它们出现在调试页面的组件选择列表中
 */
object XxxDebugComponents {
    fun registerAll() {
        DebugComponentRegistry.register(
            DebugComponent(
                id = "ComponentId",
                name = "组件中文名",
                group = "分组名",
                description = "组件描述",
            ) {
                ComponentPreview()
            }
        )
    }
}
```
