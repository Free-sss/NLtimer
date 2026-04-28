# 标签管理功能设计文档

**日期：** 2026-04-29
**状态：** ✅ 已批准
**架构方案：** 独立 Feature 模块（feature:tag_management）

---

## 1. 目标与范围

### 目标
在 NLtimer 应用中新增标签管理功能，支持对现有 Tag 实例的集中管理（查看、新建、编辑、删除、移动、分类管理），采用紧凑型卡片式 UI 设计。

### 范围
- ✅ **标签 CRUD**：新建、编辑、删除、归档、移动（跨分类）
- ✅ **分类管理**：新建、重命名、删除、排序标签分类
- ✅ **数据源**：复用现有 `tags` 表（按 `category` 字段分组）
- ✅ **UI 风格**：紧凑型卡片布局（参考原型截图）
- ❌ **不在范围内**：批量操作、导入/导出、拖拽排序（后续迭代）

---

## 2. 架构设计

### 2.1 模块结构

```
feature/tag_management/                          ★ 新建模块
├── build.gradle.kts                             ★ Gradle 配置
├── src/main/
│   ├── AndroidManifest.xml                      ★ 模块清单
│   └── java/com/nltimer/feature/tag_management/
│       ├── model/
│       │   └── TagManagementUiState.kt          ★ UI 状态数据类
│       ├── viewmodel/
│       │   └── TagManagementViewModel.kt        ★ 状态管理
│       └── ui/
│           ├── TagManagementRoute.kt            ★ 路由入口
│           ├── TagManagementScreen.kt           ★ 主界面
│           └── components/
│               ├── TagChip.kt                  ★ 标签组件
│               ├── CategoryCard.kt             ★ 分类卡片
│               └── dialogs/
│                   ├── AddTagDialog.kt         ★ 新建标签对话框
│                   ├── EditTagDialog.kt        ★ 编辑标签对话框
│                   ├── AddCategoryDialog.kt    ★ 新建分类对话框
│                   ├── RenameCategoryDialog.kt ★ 重命名分类对话框
│                   └── ConfirmDialog.kt        ★ 确认对话框

core/data/                                       ☆ 复用现有代码
├── repository/
│   └── TagRepository.kt                         ☆ 已有接口（可能需要扩展）
└── database/dao/
    └── TagDao.kt                                ☆ 已有方法（getDistinctCategories 等）

app/                                             ☆ 修改
├── build.gradle.kts                             ☆ 追加依赖
├── src/main/java/com/nltimer/app/
│   ├── navigation/NLtimerNavHost.kt             ☆ 追加路由 "tag_management"
│   └── component/AppDrawer.kt                   ☆ 追加菜单项

settings.gradle.kts                              ☆ 追加 include("feature:tag_management")
```

### 2.2 数据流

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  ┌──────────────────┐    ┌────────────────────────────────┐ │
│  │ TagManagement     │◄───│ TagManagementViewModel        │ │
│  │ Screen           │    │ (StateFlow<UiState>)          │ │
│  └──────────────────┘    └──────────────┬─────────────────┘ │
└─────────────────────────────────────────┼───────────────────┘
                                          │ observe
                                          ▼
┌─────────────────────────────────────────────────────────────┐
│                       Data Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              TagRepository (已有)                     │   │
│  │  - getAllActive(): Flow<List<Tag>>                   │   │
│  │  - insert(tag): Long                                 │   │
│  │  - update(tag)                                       │   │
│  │  - delete(tag) / setArchived(id, boolean)            │   │
│  │  - getByCategory(category): Flow<List<Tag>>          │   │
│  └──────────────────────┬───────────────────────────────┘   │
│                         │                                   │
│  ┌──────────────────────▼───────────────────────────────┐   │
│  │                    TagDao (已有)                       │   │
│  │  - getDistinctCategories(): Flow<List<String>>        │   │
│  │  - renameCategory(old, new)                           │   │
│  │  - resetCategory(category) → SET NULL                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 核心数据模型

```kotlin
// UI State
data class TagManagementUiState(
    val uncategorizedTags: List<Tag> = emptyList(),
    val categories: List<CategoryWithTags> = emptyList(),
    val isLoading: Boolean = true,
    val dialogState: DialogState? = null,
)

data class CategoryWithTags(
    val categoryName: String,
    val tags: List<Tag>,
)

// Dialog States
sealed interface DialogState {
    // 标签操作
    object AddTag : DialogState
    data class EditTag(val tag: Tag) : DialogState
    data class DeleteTag(val tag: Tag) : DialogState
    data class MoveTag(val tag: Tag, val currentCategory: String?) : DialogState

    // 分类操作
    object AddCategory : DialogState
    data class RenameCategory(val name: String) : DialogState
    data class DeleteCategory(val name: String, val tagCount: Int) : DialogState
}
```

---

## 3. UI 设计规范

### 3.1 整体布局

```
┌─────────────────────────────────────┐
│  ← 返回    标签管理            🗑️  │  TopAppBar
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 默认                        │    │  CategoryCard
│  │ [#xjdj] [#工作]      [+]   │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 优先级                  ⋮  │    │  CategoryCard
│  │ [#紧急] [#重要] [#普通] [+] │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ 状态                    ⋮  │    │  CategoryCard
│  │ [#进行中] [#已完成] [...] [+]│   │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │     + 增加标签分类           │    │  虚线边框按钮
│  └─────────────────────────────┘    │
│                                     │
│                          [⋯]        │  FAB
└─────────────────────────────────────┘
```

### 3.2 组件详细说明

#### **TopAppBar**
- 左侧：返回箭头 + 标题"标签管理"
- 右侧：🗑️ 图标（可选：批量删除模式切换）

#### **CategoryCard（分类卡片）**
- 背景：白色，圆角 12dp，轻微阴影
- 内边距：16dp
- 卡片间距：12dp（垂直）
- 结构：
  - 标题行：分类名称（15px/medium）+ ⋮ 菜单（非默认分类显示）
  - 标签区：FlowRow 横向排列，间距 8dp/4dp
  - 添加按钮：圆形蓝色 "+" 按钮（30dp）

#### **TagChip（标签芯片）**
- 形状：圆角胶囊（border-radius: 16px）
- 内边距：6dp 14dp
- 字体：13px/regular
- 颜色：
  - 背景：`tag.color` 或默认灰色 #E8E8E8
  - 文字：`tag.textColor` 或自动对比色
- 前缀：# 符号
- 交互：
  - 单击 → 编辑对话框
  - 长按 → 操作菜单（移动/删除）

#### **添加分类按钮**
- 样式：虚线边框（dashed border）
- 内边距：20dp
- 文字："+ 增加标签分类"
- 颜色：灰色 #999

#### **FAB（浮动按钮）**
- 位置：右下角（bottom: 24dp, right: 24dp）
- 尺寸：56dp 圆形
- 颜色：Material Blue (#2196F3)
- 图标：⋯ （更多操作菜单）

### 3.3 对话框系统

| 对话框 | 触发条件 | 输入字段 | 确认操作 |
|--------|---------|---------|---------|
| **AddTagDialog** | 点击"+"按钮 | 名称、颜色、图标(可选)、分类(预填) | `viewModel.addTag()` |
| **EditTagDialog** | 点击 TagChip | 名称、颜色、图标、所属分类 | `viewModel.updateTag()` |
| **DeleteConfirmDialog** | 长按→删除 | 确认提示文字 | `viewModel.deleteTag()` / `archiveTag()` |
| **MoveTagDialog** | 长按→移动 | 分类列表选择器 | `viewModel.moveTagToCategory()` |
| **AddCategoryDialog** | 点击"增加标签分类" | 分类名称 | `viewModel.addCategory()` |
| **RenameCategoryDialog** | ⋮→重命名 | 新分类名 | `viewModel.renameCategory()` |
| **DeleteCategoryDialog** | ⋮→删除 | 确认提示+标签数量警告 | `viewModel.deleteCategory()` |

---

## 4. 交互流程

### 4.1 新建标签流程
```
用户点击某分类卡片的 [+]
    ↓
弹出 AddTagDialog（预填 category=当前分类名）
    ↓
输入标签信息（名称必填，颜色/图标可选）
    ↓
点击确认
    ↓
ViewModel 调用 Repository.insert()
    ↓
数据库 INSERT → Flow 更新
    ↓
UI 自动刷新：新标签出现在对应分类下
```

### 4.2 移动标签流程
```
长按目标 TagChip
    ↓
显示 DropdownMenu：[编辑] [移动] [删除]
    ↓
点击 [移动]
    ↓
弹出 MoveTagDialog（列表展示所有分类）
    ↓
选择目标分类（或"未分类"）
    ↓
ViewModel 调用 update(tag.copy(category=newCategory))
    ↓
数据库 UPDATE category 字段
    ↓
Flow 更新：标签从原分类消失，出现在新分类
```

### 4.3 删除分类流程
```
点击分类卡片的 [⋮] 菜单
    ↓
选择 [删除分类]
    ↓
弹出 DeleteCategoryDialog：
    "删除「状态」分类？该分类下的 3 个标签将变为未分类。"
    ↓
确认删除
    ↓
ViewModel 调用 Repository.resetCategory("状态")
    ↓
数据库批量 UPDATE SET category=NULL
    ↓
UI 更新：分类卡片移除，原标签移至"默认"分类
```

---

## 5. 导航集成

### 5.1 AppDrawer 入口
```kotlin
// AppDrawer.kt
DrawerMenuItem(
    route = "tag_management",
    label = "标签管理",
    icon = Icons.Default.Label,  // 或自定义图标
)
```

### 5.2 NavHost 路由
```kotlin
// NLtimerNavHost.kt
composable("tag_management") {
    TagManagementRoute()
}
```

### 5.3 Route 实现
```kotlin
@Composable
fun TagManagementRoute(
    viewModel: TagManagementViewModel = hiltViewModel(),
) {
    TagManagementScreen(viewModel = viewModel)
}
```

---

## 6. Gradle 依赖配置

### 6.1 settings.gradle.kts
```kotlin
include("feature:tag_management")
```

### 6.2 feature/tag_management/build.gradle.kts
```kotlin
plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.tag_management"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    
    implementation(libs.material3)
}
```

### 6.3 app/build.gradle.kts
```kotlin
implementation(projects.feature.tag_management)
```

---

## 7. 测试策略

| 层级 | 测试内容 | 方式 |
|------|---------|------|
| **Repository** | 分类查询、重命名原子性、resetCategory 行为 | Room in-memory + JUnit |
| **ViewModel** | 状态变更、CRUD 操作、边界条件处理 | StateFlow + JUnit + Turbine |
| **UI 组件** | TagChip 点击/长按、CategoryCard 展开、对话框交互 | Compose UI Test（后续补充） |

### 关键测试场景
1. 新建标签后出现在正确分类下
2. 移动标签后原分类不再显示
3. 删除分类后标签变为未分类
4. 重命名分类后所有关联标签更新
5. 空状态显示（无标签、无分类时）

---

## 8. 技术约束与决策记录

### 8.1 技术选型

| 决策点 | 选择 | 理由 |
|-------|------|------|
| UI框架 | Jetpack Compose + Material3 | 项目统一标准 |
| 架构模式 | MVVM + Repository | 与活动管理一致 |
| 依赖注入 | Hilt | 项目统一标准 |
| 异步处理 | Kotlin Flow + Coroutines | 响应式编程 |

### 8.2 设计决策

1. **为什么复用现有 Tag 表而非创建新表？**
   - 避免数据冗余和同步问题
   - 标签管理本质是对 Tag 实例的管理
   - 与现有功能（行为打标签、活动打标签）无缝集成

2. **为什么使用独立模块而非扩展现有 categories？**
   - categories 模块职责是"分类名管理"，tag_management 是"标签实例管理"
   - 职责分离符合单一职责原则
   - 可独立演进和测试

3. **为什么使用紧凑型 UI？**
   - 参考用户提供的截图设计
   - 在有限屏幕空间内展示更多信息
   - 减少滚动操作，提升效率

4. **为什么 FAB 使用"更多操作"而非"添加"？**
   - 添加操作已内联到每个分类卡片中
   - FAB 用于全局性操作（排序、视图切换等）
   - 避免 UI 冗余

---

## 9. 后续优化方向（不在本次实现范围）

- 🔲 批量选择和批量操作
- 🔲 拖拽排序标签位置
- 🔲 拖拽移动标签到其他分类
- 🔲 导入/导出标签配置
- 🔲 标签使用统计和分析
- 🔲 标签搜索和筛选功能
- 🔲 自定义主题色方案

---

## 10. 验收标准

### 功能完整性
- [ ] 可以查看所有标签（按分类分组显示）
- [ ] 可以在指定分类下新建标签
- [ ] 可以编辑标签属性（名称、颜色、图标、分类）
- [ ] 可以删除/归档标签
- [ ] 可以将标签移动到其他分类
- [ ] 可以新建标签分类
- [ ] 可以重命名标签分类
- [ ] 可以删除标签分类（标签变为未分类）

### UI/UX 质量
- [ ] 界面布局与设计原型一致
- [ ] 交互流畅，无卡顿
- [ ] 空状态友好提示
- [ ] 错误状态合理处理
- [ ] 对话框易用且美观

### 代码质量
- [ ] 遵循项目架构规范
- [ ] 代码风格统一
- [ ] 关键逻辑有单元测试
- [ ] 无明显性能问题

---

**文档版本：** v1.0
**最后更新：** 2026-04-29
**批准状态：** ✅ 已获用户批准
