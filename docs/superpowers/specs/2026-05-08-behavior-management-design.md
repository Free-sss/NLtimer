# 行为管理页面设计规格

## 概述

新增行为管理页面，用于聚合查看所有行为数据，支持多维度筛选、紧凑布局展示、编辑、导出和导入。页面从侧边抽屉进入，位于"标签管理"下方。

## 模块结构

### 新增模块

#### `core/behaviorui/` — 行为UI共享组件

将 `feature:home/ui/sheet/` 下以下文件移入：

- `AddBehaviorSheet.kt`
- `AddCurrentBehaviorSheet.kt`
- `AddTargetBehaviorSheet.kt`
- `ActivityPicker.kt`
- `TagPicker.kt`
- `CategoryPickerDialog.kt`
- `DualTimePickerComponent.kt`
- `TimeAdjustmentComponent.kt`
- `BehaviorNatureSelector.kt`
- `NoteInput.kt`
- `ActivityNoteComponent.kt`
- `ActivityGridComponent.kt`
- `AddActivityDialog.kt`
- `AddTagDialog.kt`

#### `feature/behavior_management/` — 行为管理页面

```
feature/behavior_management/
  └── src/main/java/com/nltimer/feature/behavior_management/
      ├── viewmodel/
      │   └── BehaviorManagementViewModel.kt
      ├── model/
      │   └── BehaviorManagementUiState.kt
      ├── ui/
      │   ├── BehaviorManagementScreen.kt
      │   ├── BehaviorManagementRoute.kt
      │   ├── BehaviorListItem.kt
      │   ├── BehaviorTimelineItem.kt
      │   ├── FilterBar.kt
      │   ├── TimeRangeSelector.kt
      │   └── ImportExportDialog.kt
      └── export/
          ├── JsonExporter.kt
          └── JsonImporter.kt
```

### 依赖关系变化

```
feature:home → core:behaviorui, core:data, core:designsystem
feature:behavior_management → core:behaviorui, core:data, core:designsystem
```

## 导航入口

- `NLtimerRoutes` 新增 `BEHAVIOR_MANAGEMENT = "behavior_management"`
- 侧边抽屉菜单 `drawerMenuItems` 在"标签管理"与"设置"之间插入：
  `DrawerMenuItem(NLtimerRoutes.BEHAVIOR_MANAGEMENT, "行为管理", Icons.Default.EventNote)`
- `NLtimerNavHost` 新增 composable 路由映射
- 页面样式：次级页面，带返回箭头 TopAppBar，隐藏底部导航栏

## 页面布局

```
┌─────────────────────────────────────┐
│  ← 行为管理              [导入][导出] │
├─────────────────────────────────────┤
│  [1日 ▼]    ◀  2026-05-08  ▶       │
├─────────────────────────────────────┤
│  [活动分组▼] [标签分类▼] [状态▼]     │
│  [🔍 关键词搜索]                    │
├─────────────────────────────────────┤
│  视图: [列表📖] [时间轴📈]          │
├─────────────────────────────────────┤
│  行为列表 / 时间轴内容区             │
│  (紧凑排列，虚拟滚动)               │
│  ┌─────────────────────────────┐   │
│  │ ● 写代码       08:00-09:30 │   │
│  │   专注·深度  ✓已完成  备注… │   │
│  └─────────────────────────────┘   │
├─────────────────────────────────────┤
│  统计摘要: 共xx项 · 已完成xx · xh   │
└─────────────────────────────────────┘
```

### 交互

1. **点击行为项** → 弹出 AddBehaviorSheet（编辑模式，传入 editBehaviorId）
2. **长按行为项** → 选中进入多选模式，顶部出现批量操作栏（删除、导出选中项）
3. **下拉时间范围** → 选项：4小时 / 8小时 / 1日(默认) / 3日 / 7日 / 1月 / 1年
4. **日期导航** → 左右箭头按当前范围步进，点击日期文字弹出 DatePicker
5. **过滤栏** → 三个下拉过滤 + 搜索框，所有过滤条件 AND 组合
6. **导出** → 按当前过滤条件导出 JSON 文件（通过系统文件选择器 SAF）
7. **导入** → 选择 JSON 文件，解析后展示预览列表（标注重复项），用户确认后写入

## 列表模式（BehaviorListItem）

```
┌──────────────────────────────────────┐
│ ● 写代码            08:00 - 09:30    │  第1行：活动色点+名称(粗) + 时间
│   专注·深度     ✓已完成    继续优化…  │  第2行：标签(淡色) + 状态徽标 + 备注(截断)
└──────────────────────────────────────┘
```

- 行高约 56dp，轻微背景色交替区分，无分隔线
- 活动名称前用 4dp 圆点显示活动颜色
- 标签用文字 + 中点分隔，不使用芯片
- 状态图标：✓已完成 / ▶进行中 / ○待定
- 备注截断为单行，灰色

## 时间轴模式（BehaviorTimelineItem）

```
08:00 ──●── 写代码 (1.5h)
         │  专注·深度 ✓
09:30 ──●── 开会 (0.5h)
         │  沟通 ▶
10:00 ──●── 阅读 (1h)
            学习 ○
```

- 左侧时间列 48dp 固定宽度，显示起止时间
- 右侧内容区：活动名+时长 + 折行标签+状态
- 时间轴竖线 2dp，连接节点用活动颜色圆点
- 无行为的时间段不显示，紧凑排列
- 超长范围（7日/1月/1年）按日分组，带日期分组头

## AddBehaviorSheet 复用

- `core:behaviorui` 模块对外暴露的 API 与现有 AddBehaviorSheet 签名一致
- BehaviorManagementViewModel 持有编辑状态，弹出 sheet 时传入 editBehaviorId
- sheet 的 onConfirm 回调调用 BehaviorRepository.updateBehavior()
- 行为管理页只需 AddBehaviorSheet（COMPLETED 模式）

## 数据层

### BehaviorDao 新增查询

```kotlin
fun getBehaviorsWithDetailsByTimeRange(startTime: Long, endTime: Long): Flow<List<BehaviorWithDetails>>
fun getBehaviorsByTimeRangeSync(startTime: Long, endTime: Long): List<BehaviorWithDetails>
```

### ViewModel 状态

```kotlin
data class BehaviorManagementUiState(
    val timeRange: TimeRangePreset = TimeRangePreset.ONE_DAY,
    val rangeStartDate: LocalDate = LocalDate.now(),
    val selectedActivityGroup: String? = null,
    val selectedTagCategory: String? = null,
    val selectedStatus: BehaviorNature? = null,
    val searchQuery: String = "",
    val viewMode: ViewMode = ViewMode.LIST,
    val behaviors: ImmutableList<BehaviorWithDetails> = persistentListOf(),
    val isImporting: Boolean = false,
    val importPreview: ImportPreview? = null,
    val selectedBehaviorIds: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val editBehaviorId: Long? = null,
)

enum class TimeRangePreset(val label: String, val hours: Long) {
    FOUR_HOURS("4小时", 4),
    EIGHT_HOURS("8小时", 8),
    ONE_DAY("1日", 24),
    THREE_DAYS("3日", 72),
    SEVEN_DAYS("7日", 168),
    ONE_MONTH("1月", 720),
    ONE_YEAR("1年", 8760),
}

enum class ViewMode { LIST, TIMELINE }
```

### 过滤逻辑

前端过滤（在 ViewModel 中对 Flow 结果进行）：
- 活动分组：按 activity.groupId 匹配选中分组的 ActivityGroup
- 标签分类：按 tags 中任一 tag.category 匹配
- 状态：按 behavior.status 匹配
- 关键词：匹配 activity.name / tag.name / note

## 导出格式（JSON）

```json
{
  "version": 1,
  "exportedAt": 1715000000000,
  "timeRange": { "start": "...", "end": "...", "label": "1日" },
  "filters": { "activityGroup": "工作", "tagCategory": null, "status": "completed" },
  "behaviors": [
    {
      "id": 0,
      "startTime": 1715000000000,
      "endTime": 1715003600000,
      "status": "completed",
      "note": "...",
      "pomodoroCount": 0,
      "sequence": 0,
      "activity": { "name": "写代码", "iconKey": "code", "color": 4280391411 },
      "tags": [ { "name": "专注", "color": 4280391411 } ]
    }
  ]
}
```

**要点：**
- id 不导出，导入时重新分配
- 保留活动名+图标+颜色、标签名+颜色，导入时按名称匹配本地记录
- version 用于后续格式升级兼容

## 导入查重

### 判定规则

同一 activityId + startTime 时间重叠 → 重复

### 查重流程

```
对每条导入行为:
  1. 按 activity.name 匹配本地活动 → 找到则用本地 activityId
  2. 按 startTime 查找同一时间范围的已有行为
  3. 若同一 activityId + 时间重叠 → 标记为「重复」
  4. 未匹配的活动/标签 → 标记为「新建」
```

### 导入预览弹窗

```
┌─────────────────────────────────┐
│  导入预览               [×]     │
├─────────────────────────────────┤
│  共 25 条 · 重复 3 条 · 新建 2  │
├─────────────────────────────────┤
│  ⚠ 重复项:                      │
│  · 写代码 08:00-09:00           │
│  · 开会 10:00-10:30             │
│  · 阅读 14:00-15:00             │
├─────────────────────────────────┤
│  ✚ 需新建:                      │
│  · 活动: "冥想"                  │
│  · 标签: "正念"                  │
├─────────────────────────────────┤
│  处理方式:                       │
│  ○ 跳过重复项（推荐）            │
│  ○ 覆盖重复项                    │
│  ○ 全部导入（允许重复）          │
├─────────────────────────────────┤
│  [取消]         [确认导入]       │
└─────────────────────────────────┘
```

## 错误处理

- 导入文件格式错误：提示"文件格式不正确，请选择有效的导出文件"
- 导入文件版本不兼容：提示"文件版本不兼容"
- 导出无数据：提示"当前筛选条件下无可导出的行为"
- 导入时无匹配活动：导入预览中标为"新建"，确认后自动创建
