# NLtimer 活动分类管理功能设计规格

## 概述

在 NLtimer 中新增活动分类管理功能，允许用户对活动(Activity)和标签(Tag)的分类进行集中管理（新建、重命名、删除、搜索）。分类数据复用现有 `Activity.category` 和 `Tag.category` 字符串字段，不新增数据库表。活动和标签分类作为两个独立维度展示。

## 决策记录

| 决策项                  | 选择                       | 理由                                    |
| ----------------------- | -------------------------- | --------------------------------------- |
| 数据存储                | 方案 A：字符串分类         | 复用现有字段，轻量、无迁移成本          |
| CategoryRepository 位置 | core:data                  | 数据层统一管理，@Transaction 保证原子性 |
| 活动/标签分类隔离       | 独立接口 + 独立 UI section | 两个维度语义不同，不应混杂              |
| 多级分类预留            | `parent: String? = null`   | 零额外成本，接口级预埋                  |
| 路由名                  | `"categories"`             | 简洁，不暗示只管理单一类型              |

## 模块结构

```
core/data/
├── repository/
│   └── CategoryRepository.kt                 ★新增 接口
│   └── impl/
│       └── CategoryRepositoryImpl.kt         ★新增 实现
├── database/dao/
│   ├── ActivityDao.kt                        ☆追加 getDistinctCategories
│   └── TagDao.kt                             ☆追加 getDistinctCategories
├── di/
│   └── DataModule.kt                         ☆追加 bindCategoryRepository

feature/categories/                           ★全新模块
├── build.gradle.kts                          ★新增
├── src/main/
│   ├── AndroidManifest.xml                   ★新增
│   └── java/.../feature/categories/
│       ├── model/
│       │   └── CategoriesUiState.kt          ★新增
│       ├── ui/
│       │   ├── CategoriesRoute.kt            ★新增
│       │   └── CategoriesScreen.kt           ★新增
│       └── viewmodel/
│           └── CategoriesViewModel.kt        ★新增

app/                                          ☆修改
├── build.gradle.kts                          ☆追加依赖
├── src/main/java/.../app/
│   ├── navigation/NLtimerNavHost.kt          ☆追加路由
│   └── component/AppDrawer.kt                ☆修改导航入口

settings.gradle.kts                           ☆追加模块声明
```

## CategoryRepository 接口

```kotlin
interface CategoryRepository {
    fun getDistinctActivityCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameActivityCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetActivityCategory(category: String)

    fun getDistinctTagCategories(parent: String? = null): Flow<List<String>>
    suspend fun renameTagCategory(oldName: String, newName: String, parent: String? = null)
    suspend fun resetTagCategory(category: String)
}
```

- `getDistinct*`：SQL 层过滤 `WHERE category IS NOT NULL AND category != ''`，不返回空值
- `rename*`：`@Transaction` + 批量 `UPDATE`，原子性保证
- `reset*`：将该分类下所有条目的 `category` 置 `NULL`
- `parent` 参数为未来多级分类预留，当前传 `null`

## DAO 追加查询

```sql
-- ActivityDao
@Query("SELECT DISTINCT category FROM activities WHERE category IS NOT NULL AND category != '' ORDER BY category")
fun getDistinctCategories(): Flow<List<String>>

-- 用于 rename
@Query("UPDATE activities SET category = :newName WHERE category = :oldName")
suspend fun renameCategory(oldName: String, newName: String)

@Query("UPDATE activities SET category = NULL WHERE category = :category")
suspend fun resetCategory(category: String)
```

TagDao 同理。

> **注**：当前 DAO 查询不支持 `parent` 过滤。未来启用多级分类时，需扩展 DAO 增加 `WHERE parent = :parent` 条件（若走路径模式则用 `LIKE`），CategoryRepository 接口无需变更。

## 数据流（搜索实现）

```
ActivityDao.getDistinctCategories()        TagDao.getDistinctCategories()
        ↓ Flow<List<String>>                      ↓ Flow<List<String>>
        └──────────────┬──────────────────────────┘
                       ↓
              CategoryRepositoryImpl
                       ↓ Flow<List<String>> × 2
              CategoriesViewModel
                       ↓ combine(activityCats, tagCats, searchQuery)
                       ↓ 内存过滤 → StateFlow<CategoriesUiState>
              CategoriesScreen (Compose)
```

搜索在 ViewModel 层通过 `combine` 组合分类列表流与搜索词 StateFlow，在内存中 `.filter { it.contains(query) }`，避免每次输入触发数据库查询。

## UI 设计

```
┌─────────────────────────────────┐
│  ← 活动分类管理                  │  TopAppBar
├─────────────────────────────────┤
│  [搜索分类...]                   │  SearchBar
├─────────────────────────────────┤
│  活动分类                        │  Section Header
│  ┌──────┐ ┌──────┐ ┌──────┐   │
│  │ 🏷工作│ │ 🏷学习│ │ 🏷运动│   │  Chip 流式布局
│  └──────┘ └──────┘ └──────┘   │
│  + 新建分类                      │  Add chip
├─────────────────────────────────┤
│  标签分类                        │  Section Header
│  ┌──────┐ ┌──────┐             │
│  │ 🏷优先级│ │ 🏷状态 │             │  Chip 流式布局
│  └──────┘ └──────┘             │
│  + 新建分类                      │  Add chip
└─────────────────────────────────┘
```

### 交互

| 操作       | 触发方式          | 行为                                                                                                                 |
| ---------- | ----------------- | -------------------------------------------------------------------------------------------------------------------- |
| 查看分类   | 进入页面          | 两个 section 各展示 chip 流式布局                                                                                    |
| 新建分类   | 点击 `+ 新建分类` | 弹出对话框输入分类名。**新建分类仅作为 UI 预置，不产生数据库写入**——分类在首次被 Activity/Tag 引用时自动出现在列表中 |
| 重命名分类 | 长按 chip         | 弹出重命名对话框。**若目标分类名已存在则提示用户禁止重名**，避免意外合并                                             |
| 删除分类   | 长按 chip → 删除  | 二次确认弹窗，确认后调用 resetCategory，该分类下所有 Activity/Tag 的 category 字段置 NULL                            |
| 搜索       | 搜索框输入        | **在 ViewModel 层通过组合分类列表流与搜索词流，在内存中过滤**，避免重复数据库查询                                    |

### 边界规则

- **重命名冲突**：目标分类名已存在时，弹出 Toast 提示"该分类已存在"，操作被拒绝
- **空分类**：`getDistinct*` 在 SQL 层过滤 `NULL` 和空字符串，UI 不展示空分类 chip
- **删除后**：被删除分类下的 Activity/Tag 变为"未分类"状态（category=NULL），后续可在其他功能中补充"未分类"筛选入口
- **单表事务**：`renameActivityCategory` 仅操作 Activity 表，`renameTagCategory` 仅操作 Tag 表，各自独立事务

## 导航

```kotlin
// AppDrawer.kt
DrawerMenuItem("categories", "活动分类管理", Icons.Default.Category)

// NLtimerNavHost.kt
composable("categories") { CategoriesRoute() }
```

## Gradle 依赖

```
feature:categories
  ├── core:designsystem  (已有)
  └── core:data          (已有，含 CategoryRepository)

settings.gradle.kts:  include("feature:categories")
app/build.gradle.kts:  implementation(projects.feature.categories)
```

## 测试策略

| 层级                | 测试内容                                         | 方式                                       |
| ------------------- | ------------------------------------------------ | ------------------------------------------ |
| CategoryRepository  | rename/reset 原子性、getDistinct 过滤空值和 NULL | Room in-memory + JUnit（DAO 查询一并验证） |
| CategoriesViewModel | 状态变更、搜索过滤逻辑、重命名冲突检测           | StateFlow + JUnit + Turbine                |
| CategoriesScreen    | UI 交互 (Compose UI Test)                        | 后续补充（AndroidTest）                    |
