# 数据库调试工具设计

## 1. 目标

在调试页面中新增「数据库工具」组件，支持：
1. 清除所有数据库数据
2. 批量为所有表插入测试数据
3. 为单个表插入测试数据
4. 为单个表清除数据
5. 查询所有数据库数据（分组卡片式展示）

## 2. 架构

```
feature/debug/
├── data/
│   └── DebugDatabaseHelper.kt         ← Hilt 单例，封装所有调试数据库操作
├── ui/preview/
│   └── DatabaseToolsPreview.kt        ← UI 组件
└── FeatureDebugComponents.kt          ← 修改：注册 DatabaseTools 组件

core/data/
└── database/dao/
    ├── ActivityDao.kt                  ← 修改：+ deleteAll()
    ├── ActivityGroupDao.kt             ← 修改：+ deleteAll()
    ├── TagDao.kt                       ← 修改：+ deleteAll()
    └── BehaviorDao.kt                  ← 修改：+ deleteAll()、关联表操作
```

## 3. 数据访问层

### 3.1 DebugDatabaseHelper

```kotlin
@Singleton
class DebugDatabaseHelper @Inject constructor(
    private val activityDao: ActivityDao,
    private val activityGroupDao: ActivityGroupDao,
    private val tagDao: TagDao,
    private val behaviorDao: BehaviorDao,
    private val database: NLtimerDatabase,
)
```

| 方法 | 说明 |
|------|------|
| `clearAllTables()` | 按外键依赖顺序清除 6 张表 |
| `insertAllTestData()` | 为所有表批量插入测试数据，建立关联关系 |
| `clearTable(tableName: String)` | 清除指定表数据 |
| `insertTestData(tableName: String)` | 为指定表插入测试数据 |
| `queryAllData(): Map<String, List<Map<String, Any?>>>` | 查询所有表数据，key 为表名 |

### 3.2 清除顺序

外键依赖顺序：
1. `behavior_tag_cross_ref`
2. `activity_tag_binding`
3. `behaviors`
4. `activities`
5. `tags`
6. `activity_groups`

### 3.3 新增 DAO 方法

| DAO | 方法 |
|-----|------|
| `ActivityDao` | `@Query("DELETE FROM activities") suspend fun deleteAll()` |
| `ActivityGroupDao` | `@Query("DELETE FROM activity_groups") suspend fun deleteAll()` |
| `TagDao` | `@Query("DELETE FROM tags") suspend fun deleteAll()` |
| `BehaviorDao` | `@Query("DELETE FROM behaviors") suspend fun deleteAll()` |
| `BehaviorDao` | `@Query("DELETE FROM behavior_tag_cross_ref") suspend fun deleteAllTagCrossRefs()` |
| `BehaviorDao` | `@Query("DELETE FROM activity_tag_binding") suspend fun deleteAllActivityTagBindings()` |

关联表查询方法（用于 queryAllData）：

| DAO | 方法 |
|-----|------|
| `BehaviorDao` | `@Query("SELECT * FROM behavior_tag_cross_ref") suspend fun getAllCrossRefsSync(): List<BehaviorTagCrossRefEntity>` |
| `BehaviorDao` | `@Query("SELECT * FROM activity_tag_binding") suspend fun getAllActivityTagBindingsSync(): List<ActivityTagBindingEntity>` |

### 3.4 测试数据

| 表 | 数据 |
|---|---|
| `activity_groups` | 工作、生活、运动 |
| `activities` | 跑步(🏃,运动组)、阅读(📖,生活组)、编程(💻,工作组)、会议(📋,工作组)、冥想(🧘,生活组) |
| `tags` | 重要(红)、紧急(橙)、日常(蓝)、专注(绿) |
| `behaviors` | 3 条：跑步-completed、阅读-active、编程-pending |
| `activity_tag_binding` | 跑步-日常、编程-专注、会议-紧急 |
| `behavior_tag_cross_ref` | 行为1-日常、行为2-专注 |

## 4. UI 层

### 4.1 组件注册

在 `FeatureDebugComponents.registerAll()` 中新增：

```kotlin
DebugComponent(
    id = "DatabaseTools",
    name = "数据库工具",
    group = "Database",
    description = "清除/插入/查询数据库数据",
    implemented = true,
) {
    DatabaseToolsPreview()
}
```

### 4.2 UI 布局

```
┌─────────────────────────────────────┐
│ 全局操作                             │
│ [全部清除]  [批量插入全部]            │
├─────────────────────────────────────┤
│ activities (5条)                     │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ activity_groups (3条)                │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ tags (4条)                           │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ behaviors (3条)                      │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ activity_tag_binding (3条)           │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ behavior_tag_cross_ref (2条)         │
│ [插入测试数据]  [清除数据]            │
├─────────────────────────────────────┤
│ [查询全部数据]                        │
├─────────────────────────────────────┤
│ ▼ activities (5条)                   │
│   {id=1, name=跑步, emoji=🏃, ...}   │
│   {id=2, name=阅读, emoji=📖, ...}   │
│ ▼ tags (4条)                         │
│   {id=1, name=重要, color=..., ...}   │
└─────────────────────────────────────┘
```

### 4.3 交互细节

- 每张表卡片右侧显示当前记录数
- 操作按钮点击后执行对应操作，数据自动刷新
- 「查询全部数据」点击后，下方渲染各表数据卡片
- 每张数据卡片默认收起（显示表名+记录数），点击展开查看详细键值对
- 危险操作（全部清除、单表清除）点击后弹出确认对话框

## 5. 文件清单

### 新建文件

| 文件 | 路径 | 说明 |
|---|---|---|
| DebugDatabaseHelper.kt | feature/debug/.../data/ | 数据库操作辅助类 |
| DatabaseToolsPreview.kt | feature/debug/.../ui/preview/ | UI 组件 |

### 修改文件

| 文件 | 修改内容 |
|---|---|
| ActivityDao.kt | + deleteAll() |
| ActivityGroupDao.kt | + deleteAll() |
| TagDao.kt | + deleteAll() |
| BehaviorDao.kt | + deleteAll()、deleteAllTagCrossRefs()、deleteAllActivityTagBindings()、getAllCrossRefsSync()、getAllActivityTagBindingsSync() |
| FeatureDebugComponents.kt | + DatabaseTools 组件注册 |

## 6. 边界情况

- **关联表清空**：清除主表前必须先清除关联表，否则外键约束会报错
- **插入重复数据**：使用 `OnConflictStrategy.IGNORE`，避免重复插入
- **查询空表**：返回空列表，UI 显示 "(0条)"
- **操作反馈**：每个操作完成后显示 Snackbar 提示结果
