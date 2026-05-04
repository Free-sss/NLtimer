# 数据模型

## 核心实体关系

```
ActivityGroup (1) ──┐
                    ├──→ Activity (N) ←──┐ ActivityTagBinding (M:N)
Tag (N) ────────────┘                    ┘
  ↑
  │ BehaviorTagCrossRef (M:N)
  │
Behavior (N) ──→ Activity (1)
```

## 实体详情

### Activity（活动）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| name | String | 活动名（唯一索引） |
| emoji | String? | 显示 emoji |
| iconKey | String? | 图标键 |
| groupId | Long? | 外键 → ActivityGroup.id |
| isPreset | Boolean | 是否预设 |
| isArchived | Boolean | 是否归档 |
| color | Long? | ARGB 颜色 |

### ActivityGroup（活动分组）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| name | String | 分组名（唯一索引） |
| sortOrder | Int | 排序权重 |

### Tag（标签）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| name | String | 标签名（唯一索引） |
| color | Long? | ARGB 颜色 |
| textColor | Long? | 文字颜色 |
| icon | String? | 图标 |
| category | String? | 分类 |
| priority | Int | 优先级 |
| usageCount | Int | 使用次数 |
| sortOrder | Int | 排序权重 |
| isArchived | Boolean | 是否归档 |

### Behavior（行为记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| activityId | Long | 外键 → Activity.id |
| startTime | Long | 开始时间戳（ms） |
| endTime | Long? | 结束时间戳（ms），null=未结束 |
| status | BehaviorNature | PENDING / ACTIVE / COMPLETED |
| note | String? | 备注 |
| pomodoroCount | Int | 番茄钟数 |
| sequence | Int | 排序序号 |
| estimatedDuration | Long? | 预估时长（ms） |
| actualDuration | Long? | 实际时长（ms） |
| achievementLevel | Int? | 完成度评级 |
| wasPlanned | Boolean | 是否计划行为 |

### BehaviorNature（行为状态枚举）

| 值 | key | 含义 |
|----|-----|------|
| PENDING | "pending" | 待开始 |
| ACTIVE | "active" | 进行中（同一时刻仅一个） |
| COMPLETED | "completed" | 已完成 |

### 关联表

| 表 | 字段 | 说明 |
|----|------|------|
| ActivityTagBinding | activityId, tagId | 活动-标签 M:N |
| BehaviorTagCrossRef | behaviorId, tagId | 行为-标签 M:N |

### 聚合模型

| 类 | 组合 | 用途 |
|----|------|------|
| BehaviorWithDetails | Behavior + Activity + List\<Tag\> | 行为完整详情 |
| ActivityStats | usageCount + totalDurationMinutes + lastUsedTimestamp | 活动统计 |

## 数据库

- 名称：`nltimer-database`
- 当前版本：**6**
- 迁移策略：`fallbackToDestructiveMigration(false)` + 显式迁移
- 迁移链：
  - 3→4：category 字段迁移到 activity_groups 表
  - 4→5：activities 新增 color 列
  - 5→6：activities/groups/tags 的 name 列添加唯一索引（去重后创建）

## Repository 接口速查

| Repository | 核心能力 |
|-----------|---------|
| ActivityRepository | 活动+分组查询、搜索、归档 |
| ActivityManagementRepository | 活动 CRUD、分组 CRUD、统计、预设初始化 |
| TagRepository | 标签 CRUD、搜索、按分类筛选、分类管理 |
| CategoryRepository | 活动分类/标签分类的增删改名 |
| BehaviorRepository | 行为 CRUD、计时控制、标签关联、日结 |

## 用户偏好（SettingsPrefs → DataStore）

| 方法 | 类型 | 说明 |
|------|------|------|
| getThemeFlow / updateTheme | Flow\<Theme\> | 主题配置持久化 |
| getSavedTagCategories / saveTagCategories | Flow\<Set\<String\>\> | 标签分类持久化 |
| getDialogConfigFlow / updateDialogConfig | Flow\<DialogGridConfig\> | 弹窗配置持久化 |

## DialogGridConfig（弹窗网格配置）

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| activityDisplayMode | ChipDisplayMode | Filled | 活动 Chip 样式 |
| activityLayoutMode | GridLayoutMode | Horizontal | 活动布局方向 |
| activityColumnLines | Int | 2 | 列数 |
| activityHorizontalLines | Int | 2 | 行数 |
| activityUseColorForText | Boolean | true | 用活动颜色做文字色 |
| tag* | 同上 | 同上 | 标签对应配置 |
| showBehaviorNature | Boolean | true | 显示行为状态 |
| pathDrawMode | PathDrawMode | StartToEnd | 路径绘制模式 |
