# 数据存储模型字段说明

本文档列出 NLtimer 项目中所有存入数据的模型，包括 Room 数据库实体、领域模型及 DataStore 偏好配置。

---

## 一、Room 数据库实体

数据库名称：`nltimer-database`，当前版本：**9**

### 1. ActivityEntity（活动实体）

**表名：** `activities` | **源文件：** `core/data/.../entity/ActivityEntity.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 自动生成 | 主键，活动唯一标识 |
| name | String | — | 活动名称（唯一索引） |
| iconKey | String? | null | 活动图标键名 |
| keywords | String? | null | 关键词，与正则工具联动 |
| groupId | Long? | null | 所属分组 ID，外键关联 activity_groups 表 |
| isPreset | Boolean | false | 是否为预设活动 |
| isArchived | Boolean | false | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |
| color | Long? | null | ARGB 颜色值 |
| usageCount | Int | 0 | 使用次数统计 |
| createdAt | Long | 当前时间戳 | 创建时间（毫秒） |
| updatedAt | Long | 当前时间戳 | 更新时间（毫秒） |

### 2. ActivityGroupEntity（活动分组实体）

**表名：** `activity_groups` | **源文件：** `core/data/.../entity/ActivityGroupEntity.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 自动生成 | 主键，分组唯一标识 |
| name | String | — | 分组名称（唯一索引） |
| sortOrder | Int | 0 | 排序序号 |
| isArchived | Boolean | false | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |
| createdAt | Long | 当前时间戳 | 创建时间（毫秒） |

### 3. TagEntity（标签实体）

**表名：** `tags` | **源文件：** `core/data/.../entity/TagEntity.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 自动生成 | 主键，标签唯一标识 |
| name | String | — | 标签名称（唯一索引） |
| color | Long? | null | 标签背景颜色值（ARGB） |
| iconKey | String? | null | 标签图标键名 |
| category | String? | null | 标签所属分类 |
| priority | Int | 0 | 标签优先级，数值越大越靠前 |
| usageCount | Int | 0 | 使用次数统计 |
| sortOrder | Int | 0 | 排序序号 |
| isArchived | Boolean | false | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |

### 4. BehaviorEntity（行为记录实体）

**表名：** `behaviors` | **源文件：** `core/data/.../entity/BehaviorEntity.kt`

**外键：** `activityId` → `activities.id`（RESTRICT，删除活动时拒绝）

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 自动生成 | 主键，行为记录唯一标识 |
| activityId | Long | — | 关联的活动 ID |
| startTime | Long | — | 开始时间戳（毫秒） |
| endTime | Long? | null | 结束时间戳（毫秒），null 表示未结束 |
| status | String | "pending" | 状态标识：pending / active / completed |
| note | String? | null | 备注信息 |
| pomodoroCount | Int | 0 | 番茄钟计数 |
| sequence | Int | 0 | 同日内排序序列号 |
| estimatedDuration | Long? | null | 预估时长（毫秒） |
| actualDuration | Long? | null | 实际时长（毫秒） |
| achievementLevel | Int? | null | 完成度等级 |
| wasPlanned | Boolean | false | 是否为计划内任务 |

**索引：**
- `activityId`
- `(startTime, sequence)`
- `status`

### 5. ActivityTagBindingEntity（活动-标签关联实体）

**表名：** `activity_tag_binding` | **源文件：** `core/data/.../entity/ActivityTagBindingEntity.kt`

**复合主键：** `(activityId, tagId)`

**外键：**
- `activityId` → `activities.id`（CASCADE，级联删除）
- `tagId` → `tags.id`（CASCADE，级联删除）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| activityId | Long | 活动 ID |
| tagId | Long | 标签 ID |

### 6. BehaviorTagCrossRefEntity（行为-标签交叉引用实体）

**表名：** `behavior_tag_cross_ref` | **源文件：** `core/data/.../entity/BehaviorTagCrossRefEntity.kt`

**复合主键：** `(behaviorId, tagId)`

**外键：**
- `behaviorId` → `behaviors.id`（CASCADE，级联删除）
- `tagId` → `tags.id`（RESTRICT，拒绝删除）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| behaviorId | Long | 行为记录 ID |
| tagId | Long | 标签 ID |

---

## 二、领域模型

领域模型用于业务逻辑层，与 Room 实体通过 `toEntity()` / `fromEntity()` 互转。

### 1. Activity（活动）

**源文件：** `core/data/.../model/Activity.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 0 | 活动唯一标识 |
| name | String | — | 活动名称 |
| iconKey | String? | null | 活动图标键名 |
| keywords | String? | null | 关键词 |
| groupId | Long? | null | 所属分组 ID |
| isPreset | Boolean | false | 是否为预设活动 |
| isArchived | Boolean | false | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |
| color | Long? | null | ARGB 颜色值 |
| usageCount | Int | 0 | 使用次数统计 |

### 2. ActivityGroup（活动分组）

**源文件：** `core/data/.../model/ActivityGroup.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | 0 | 分组唯一标识 |
| name | String | — | 分组名称 |
| sortOrder | Int | 0 | 排序序号 |
| isArchived | Boolean | false | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |

### 3. Tag（标签）

**源文件：** `core/data/.../model/Tag.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | — | 标签唯一标识 |
| name | String | — | 标签名称 |
| color | Long? | — | 背景颜色值（ARGB） |
| iconKey | String? | — | 图标键名 |
| category | String? | — | 所属分类 |
| priority | Int | — | 优先级 |
| usageCount | Int | — | 使用次数 |
| sortOrder | Int | — | 排序序号 |
| isArchived | Boolean | — | 是否已归档 |
| archivedAt | Long? | null | 归档时间（毫秒） |

### 4. Behavior（行为记录）

**源文件：** `core/data/.../model/Behavior.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| id | Long | — | 行为记录唯一标识 |
| activityId | Long | — | 关联的活动 ID |
| startTime | Long | — | 开始时间戳（毫秒） |
| endTime | Long? | — | 结束时间戳（毫秒），null 表示未结束 |
| status | BehaviorNature | — | 行为状态：PENDING / ACTIVE / COMPLETED |
| note | String? | — | 备注信息 |
| pomodoroCount | Int | — | 番茄钟计数 |
| sequence | Int | — | 同日内排序序列号 |
| estimatedDuration | Long? | — | 预估时长（毫秒） |
| actualDuration | Long? | — | 实际时长（毫秒） |
| achievementLevel | Int? | — | 完成度等级 |
| wasPlanned | Boolean | — | 是否为计划内任务 |

### 5. BehaviorNature（行为状态枚举）

**源文件：** `core/data/.../model/BehaviorNature.kt`

| 枚举值 | key（数据库存储值） | 含义 |
|--------|---------------------|------|
| PENDING | "pending" | 待开始 |
| ACTIVE | "active" | 进行中（同一时刻仅一个） |
| COMPLETED | "completed" | 已完成 |

### 6. BehaviorWithDetails（行为详情聚合模型）

**源文件：** `core/data/.../model/BehaviorWithDetails.kt`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| behavior | Behavior | 行为记录 |
| activity | Activity | 关联的活动信息 |
| tags | List\<Tag\> | 关联的标签列表 |

### 7. ActivityStats（活动统计模型）

**源文件：** `core/data/.../model/ActivityStats.kt`

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| usageCount | Int | 0 | 活动被使用次数 |
| totalDurationMinutes | Long | 0 | 总计时长（分钟） |
| lastUsedTimestamp | Long? | null | 最后使用时间戳（毫秒） |

---

## 三、DataStore 偏好配置

通过 `SettingsPrefs` 接口读写，底层使用 DataStore Preferences 持久化。

### 1. Theme（主题配置）

**源文件：** `core/designsystem/.../theme/ThemeConfig.kt` | **DataStore 键：** 见下表

| 字段名 | 类型 | 默认值 | DataStore 键名 | 说明 |
|--------|------|--------|----------------|------|
| appTheme | AppTheme | SYSTEM | `app_theme` | 应用主题模式：LIGHT / DARK / SYSTEM |
| isAmoled | Boolean | false | `is_amoled` | 是否启用 AMOLED 纯黑优化 |
| paletteStyle | PaletteStyle | TONALSPOT | `palette_style` | 调色板风格（9 种可选） |
| isMaterialYou | Boolean | false | `is_material_you` | 是否启用 Material You 动态取色 |
| seedColor | Color | 0xFF1565C0 | `seed_color` | 种子颜色（Int 存储） |
| font | Fonts | FIGTREE | `font` | 字体选择：FIGTREE / SYSTEM_DEFAULT |
| showBorders | Boolean | true | `show_borders` | 是否显示边框 |
| homeLayout | HomeLayout | GRID | `home_layout` | 主页布局：GRID / TIMELINE_REVERSE / LOG |
| showTimeSideBar | Boolean | true | `show_time_side_bar` | 是否显示时间侧边栏 |

### 2. DialogGridConfig（弹窗网格配置）

**源文件：** `core/data/.../model/DialogGridConfig.kt` | **DataStore 键：** 见下表

| 字段名 | 类型 | 默认值 | DataStore 键名 | 说明 |
|--------|------|--------|----------------|------|
| activityDisplayMode | ChipDisplayMode | Filled | `act_display_mode` | 活动 Chip 显示样式 |
| activityLayoutMode | GridLayoutMode | Horizontal | `act_layout_mode` | 活动网格布局方向 |
| activityColumnLines | Int | 2 | `act_column_lines` | 活动列数 |
| activityHorizontalLines | Int | 2 | `act_horizontal_lines` | 活动最大行数 |
| activityUseColorForText | Boolean | true | `act_use_color` | 活动是否用颜色做文字色 |
| tagDisplayMode | ChipDisplayMode | Filled | `tag_display_mode` | 标签 Chip 显示样式 |
| tagLayoutMode | GridLayoutMode | Horizontal | `tag_layout_mode` | 标签网格布局方向 |
| tagColumnLines | Int | 2 | `tag_column_lines` | 标签列数 |
| tagHorizontalLines | Int | 2 | `tag_horizontal_lines` | 标签最大行数 |
| tagUseColorForText | Boolean | true | `tag_use_color` | 标签是否用颜色做文字色 |
| showBehaviorNature | Boolean | true | `show_nature_selector` | 是否显示行为状态选择器 |
| pathDrawMode | PathDrawMode | StartToEnd | `path_draw_mode` | 路径绘制模式 |

### 3. TimeLabelConfig（时间标签配置）

**源文件：** `core/designsystem/.../theme/TimeLabelConfig.kt` | **DataStore 键：** `time_label_config`（序列化存储）

| 字段名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| visible | Boolean | true | 是否显示时间标签 |
| style | TimeLabelStyle | PILL | 标签样式：PILL / PLAIN / UNDERLINE / DOT |
| format | TimeLabelFormat | HH_MM | 时间格式：HH_MM / H_MM / H_MM_A |

### 4. 标签分类持久化

**DataStore 键：** `saved_tag_categories`

| 存储键 | 类型 | 说明 |
|--------|------|------|
| saved_tag_categories | String | 用户自定义的标签分类列表，逗号分隔存储 |

---

## 四、枚举类型速查

### ChipDisplayMode（Chip 显示模式）

| 枚举值 | 说明 |
|--------|------|
| None | 不显示 |
| Filled | 填充样式 |
| Underline | 下划线样式 |
| Capsules | 胶囊样式 |
| RoundedCorners | 圆角样式 |
| Squares | 方形样式 |
| SquareBorder | 方形边框样式 |
| HandDrawn | 手绘样式 |
| DashedLines | 虚线样式 |

### GridLayoutMode（网格布局模式）

| 枚举值 | 说明 |
|--------|------|
| Horizontal | 水平排列 |
| Vertical | 垂直排列 |

### PathDrawMode（路径绘制模式）

| 枚举值 | 说明 |
|--------|------|
| StartToEnd | 从左到右 |
| BothSidesToMiddle | 从两侧到中间 |
| Random | 随机 |
| None | 不绘制 |
| WrigglingMaggot | 蠕动样式 |

### TimeLabelStyle（时间标签样式）

| 枚举值 | 说明 |
|--------|------|
| PILL | 药丸样式（圆角背景） |
| PLAIN | 纯文字（无背景） |
| UNDERLINE | 下划线样式 |
| DOT | 圆点样式 |

### TimeLabelFormat（时间标签格式）

| 枚举值 | 示例 | 说明 |
|--------|------|------|
| HH_MM | 09:00 | 24 小时制补零 |
| H_MM | 9:00 | 24 小时制不补零 |
| H_MM_A | 9:00 AM | 12 小时制 |

### AppTheme（应用主题）

| 枚举值 | 说明 |
|--------|------|
| LIGHT | 亮色模式 |
| DARK | 暗色模式 |
| SYSTEM | 跟随系统 |

### Fonts（字体）

| 枚举值 | 说明 |
|--------|------|
| FIGTREE | Figtree 字体 |
| SYSTEM_DEFAULT | 系统默认字体 |

### PaletteStyle（调色板风格）

| 枚举值 | 说明 |
|--------|------|
| TONALSPOT | 色调斑点 |
| NEUTRAL | 中性 |
| VIBRANT | 鲜艳 |
| EXPRESSIVE | 表现力 |
| RAINBOW | 彩虹 |
| FRUITSALAD | 水果沙拉 |
| MONOCHROME | 单色 |
| FIDELITY | 忠实 |
| CONTENT | 内容 |

### HomeLayout（主页布局）

| 枚举值 | 说明 |
|--------|------|
| GRID | 网格视图 |
| TIMELINE_REVERSE | 时间线倒序视图 |
| LOG | 日志视图 |

---

## 五、实体关系图

```
ActivityGroup (1) ──┐
                    ├──▶ Activity (N) ◀──┐ ActivityTagBinding (M:N)
Tag (N) ───────────┘                     ┘
  ▲
  │ BehaviorTagCrossRef (M:N)
  │
Behavior (N) ──▶ Activity (1)
```
