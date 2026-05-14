# 数据管理功能设计规格

## 概述

在设置页面新增二级页面"数据管理"，提供导出和导入功能，支持按数据类型（活动、标签、分类）独立操作，也支持全部数据的统一导出/导入。导入支持两种模式：智能处理和直接覆盖。页面同时提供行为记录管理的路由入口。

## 页面结构

### 设置页变更

在现有"主题配置"和"弹窗配置"卡片之后，新增第三个入口卡片：

- 图标：`Icons.Default.Storage`（或 `Icons.Default.CloudSync`）
- 标题：数据管理
- 副标题：导出、导入与迁移应用数据
- 点击导航至 `DATA_MANAGEMENT` 路由

### 数据管理页面（折叠式）

```
数据管理
├── [卡片] 导出到文件           → 导出全部数据为一个 JSON
├── [卡片] 从文件导入            → 选择文件 → 弹出导入模式对话框
├── [可展开] 活动数据
│   ├── 导出活动
│   └── 导入活动
├── [可展开] 标签数据
│   ├── 导出标签
│   └── 导入标签
├── [可展开] 分类数据
│   ├── 导出分类（活动分组 + 标签分类）
│   └── 导入分类
└── [卡片] 行为记录管理          → 路由到现有行为管理页
```

可展开区域使用 Material3 `ExposedDropdownMenu` 或自定义折叠组件，默认收起，点击展开后显示"导出"和"导入"两个操作按钮。

## 导入模式

点击"导入"后先选择文件，然后弹出导入模式选择对话框：

### 智能处理

按名称匹配现有数据：

| 数据类型 | 同名存在时 | 不同名时 |
|---------|-----------|---------|
| Activity | 补全空字段（color、iconKey 等当前为空则用导入值填充），不影响已有值 | 新增 |
| Tag | 补全空字段，逻辑同 Activity | 新增 |
| ActivityGroup | 跳过（已存在同名分组） | 新增 |
| TagCategory | 跳过 | 新增 |
| 关联表 | 按名称重映射 ID 后 IGNORE 插入（已有则跳过） | 新增 |

适用场景：日常合并、AI 处理数据后回导。

### 直接覆盖

执行顺序（在 Room `@Transaction` 中）：

1. 删除关联表数据（activity_tag_binding、behavior_tag_cross_ref）
2. 删除 Behavior 记录（如果有）
3. 删除 Tag
4. 删除 Activity
5. 删除 ActivityGroup
6. 插入导入数据
7. 重建关联表

适用场景：设备迁移、全新开始。

### 对话框 UI

```
选择导入模式

  ○ 智能处理
    同名数据补全空字段，不同名新增，保留所有现有数据。
    适合合并数据或回导 AI 处理后的字段。

  ● 直接覆盖
    清空对应类型的现有数据后写入导入数据。
    适合设备迁移或全新开始。

            [取消]  [确认导入]
```

## JSON 数据格式

### 完整导出

```json
{
  "version": 1,
  "exportedAt": 1715299200000,
  "activities": [
    {
      "name": "阅读",
      "iconKey": "book",
      "keywords": null,
      "groupName": "学习",
      "isPreset": false,
      "isArchived": false,
      "archivedAt": null,
      "color": 4280391411,
      "usageCount": 15,
      "tagNames": ["专注", "深度"]
    }
  ],
  "activityGroups": [
    {
      "name": "学习",
      "sortOrder": 0,
      "isArchived": false,
      "archivedAt": null
    }
  ],
  "tags": [
    {
      "name": "专注",
      "color": null,
      "iconKey": null,
      "category": "状态",
      "priority": 1,
      "usageCount": 42,
      "sortOrder": 0,
      "keywords": null,
      "isArchived": false,
      "archivedAt": null
    }
  ],
  "tagCategories": ["状态", "场景"]
}
```

### 设计要点

- `version`：格式版本号，用于未来兼容性。当前为 1。
- 外键用名称替代 ID：`groupName` 替代 `groupId`，`tagNames` 替代 `tagIds`。导入时通过名称查找并重映射 ID。
- 单独导出某类型时，只包含对应字段。例如仅导出标签时，JSON 只含 `version`、`exportedAt`、`tags`、`tagCategories`。
- 文件名格式：`nltimer_export_20260510_143022.json`（全部导出）、`nltimer_activities_20260510_143022.json`（按类型导出）。

## 技术架构

### 新增文件

```
feature/settings/
  ui/
    DataManagementScreen.kt        # 数据管理页面 Composable
    DataManagementViewModel.kt     # 状态管理，调度用例

core/data/
  model/
    ExportData.kt                  # JSON 数据结构 @Serializable 模型
  repository/
    DataExportImportRepository.kt  # 导出导入仓库接口
  repository/impl/
    DataExportImportRepositoryImpl.kt  # 实现
  usecase/
    ExportDataUseCase.kt           # 按类型/全部导出
    ImportDataUseCase.kt           # 按类型/全部导入（智能/覆盖）
  di/
    DataModule.kt                  # 注册新仓库绑定（已有文件，追加）
```

### 修改文件

```
feature/settings/ui/SettingsScreen.kt           # 新增"数据管理"入口卡片 + 导航回调
app/navigation/NLtimerRoutes.kt                 # 新增 DATA_MANAGEMENT 路由
app/navigation/NLtimerNavHost.kt                # 注册数据管理路由
```

### 关键技术选型

- **序列化**：`kotlinx.serialization`，与项目现有 JSON 使用习惯一致
- **文件选择**：`ActivityResultContracts.CreateDocument`（导出）、`ActivityResultContracts.OpenDocument`（导入）
- **线程**：导入/导出在 `Dispatchers.IO` 执行
- **状态**：ViewModel 通过 `StateFlow<DataManagementUiState>` 暴露状态
- **事务**：直接覆盖模式使用 Room `withTransaction` 保证原子性

### 导航

- 新增路由：`DATA_MANAGEMENT`，归入 `SETTINGS_FULLSCREEN_ROUTES`
- 使用与 `THEME_SETTINGS`、`DIALOG_CONFIG` 一致的滑入/滑出动画
- 从数据管理页可导航至 `BEHAVIOR_MANAGEMENT`

## 错误处理

| 场景 | 处理 |
|------|------|
| 文件读取失败 | Snackbar 提示"文件读取失败" |
| JSON 解析失败 / version 不兼容 | Snackbar 提示"文件格式无效" |
| 导入过程中异常 | 回滚事务，Snackbar 提示"导入失败" |
| 导出写入失败 | Snackbar 提示"导出失败" |
| 导入成功 | Snackbar 提示"已导入 X 条活动、Y 条标签、Z 个分类" |
| 导出成功 | Snackbar 提示"数据已导出" |
| 空文件或无数据 | Snackbar 提示"文件中没有可导入的数据" |

## 开发分支

使用 git worktree 创建隔离分支 `feature/data-management`，目录 `.worktrees/data-management`。
