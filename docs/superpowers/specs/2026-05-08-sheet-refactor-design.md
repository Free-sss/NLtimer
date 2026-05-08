# 底部弹窗重构 + 标签/活动关联设计

## 概述
将标签管理和活动管理的弹窗改为底部 Sheet 形式，完善分类选择、颜色选择、多对多关联（标签-活动）以及 keywords 字段适配。

## 一、数据层改动

### 1.1 Tag 模型增加 keywords 字段
- `TagEntity` 增加 `keywords: String? = null`
- `Tag` 增加 `keywords: String?`
- 数据库迁移 v10→v11：`ALTER TABLE tags ADD COLUMN keywords TEXT DEFAULT NULL`

### 1.2 activity_tag_binding 读写
- **TagDao 新增：**
  - `getActivityIdsForTagSync(tagId: Long): List<Long>` — 查询标签关联的活动 ID
  - `setActivityTagBindings(tagId: Long, activityIds: List<Long>)` — 事务删除旧绑定 + 插入新绑定
- **ActivityDao 新增：**
  - `getTagIdsForActivitySync(activityId: Long): List<Long>` — 查询活动关联的标签 ID
  - `setActivityTagBindings(activityId: Long, tagIds: List<Long>)` — 事务删除旧绑定 + 插入新绑定

### 1.3 Repository 接口扩展
- `TagRepository` 新增：
  - `getActivityIdsForTag(tagId: Long): List<Long>`
  - `setActivityTagBindings(tagId: Long, activityIds: List<Long>)`
- `ActivityManagementRepository` 新增：
  - `getTagIdsForActivity(activityId: Long): List<Long>`
  - `setActivityTagBindings(activityId: Long, tagIds: List<Long>)`
  - `getAllActivitiesSync(): List<Activity>` (供标签关联选择时使用)

## 二、UI 组件新建

### 2.1 CategoryPickerPopup（core/designsystem）
- 底部 Popup 单选，复用 GroupPickerPopup 样式
- 参数：`categories: List<String>`, `selected: String?`, `onSelected: (String?) -> Unit`, `onDismiss: () -> Unit`
- 含"未分类"选项

### 2.2 TagPickerPopup（core/designsystem）
- 底部 Popup 多选，复用 GroupPickerPopup 样式
- 参数：`tags: List<Tag>`, `selectedIds: Set<Long>`, `onSelectionChanged: (Set<Long>) -> Unit`, `onDismiss: () -> Unit`
- 选中项打勾，支持多选

### 2.3 ActivityPickerPopup（core/designsystem）
- 底部 Popup 多选，复用 GroupPickerPopup 样式
- 参数：`activities: List<Activity>`, `selectedIds: Set<Long>`, `onSelectionChanged: (Set<Long>) -> Unit`, `onDismiss: () -> Unit`

## 三、标签管理改造

### 3.1 表单规格
- `ActivityFormSpecs.createTag` 增加：
  - `FormRow.TextInput(key="keywords", label="关键词", placeholder="多个关键词用逗号分隔")`
  - `FormRow.LabelAction(key="activities", label="关联活动", actionText="未关联")`
  - "所属分类"行增加 `onClick` 回调
- `ActivityFormSpecs.editTag()` 同理

### 3.2 AddTagDialog → AddTagFormSheet
- 改用 `GenericFormSheet` 替代 `GenericFormDialog`
- 增加 `overlay` 参数渲染 `CategoryPickerPopup` 和 `ActivityPickerPopup`
- 需要从外部传入 `allActivities: List<Activity>` 和 `categories: List<String>`
- `onConfirm` 回调增加 `keywords: String?` 和 `activityIds: List<Long>` 参数

### 3.3 EditTagDialog → EditTagFormSheet
- 改用 `GenericFormSheet` 替代 `GenericFormDialog`
- 增加 overlay 渲染 Picker
- 编辑时需先查询该标签关联的活动 ID 列表
- 底部增加删除按钮
- `onConfirm` 回调使用完整 `Tag` 对象 + `activityIds: List<Long>`

### 3.4 TagManagementScreen 适配
- `DialogState.AddTag` 增加 `categories: List<String>` 和 `allActivities: List<Activity>`
- `DialogState.EditTag` 增加 `allActivities: List<Activity>` 和 `categories: List<String>`
- TagManagementViewModel 增加：
  - `allActivities: StateFlow<List<Activity>>` — 从 ActivityRepository 获取
  - `addTag` 方法增加 keywords 和 activityIds 参数
  - `updateTag` 方法增加 activityIds 参数

## 四、活动管理改造

### 4.1 表单规格
- `ActivityFormSpecs.createActivity` 修改：
  - note 改为 keywords：`FormRow.TextInput(key="keywords", label="关键词", placeholder="多个关键词用逗号分隔")`
  - "关联标签"行增加 `onClick` 回调，actionText 动态显示已选标签数
- `ActivityFormSpecs.editActivity()` 同理

### 4.2 编辑活动复用增加活动表单
- `EditActivityFormSheet` 标题改为"编辑活动"
- 增加删除按钮（放在表单底部或 overlay 层）
- 需要从外部传入 `allTags: List<Tag>` 和已关联的标签 ID 列表

### 4.3 ActivityManagementScreen 适配
- ViewModel 需要获取所有标签列表
- `DialogState.EditActivity` 增加 `allTags: List<Tag>`
- `addActivity` 和 `updateActivity` 方法增加 `tagIds: List<Long>` 参数

### 4.4 ActivityDetailSheet
- 编辑按钮改为调用 `showEditActivityDialog(activity)` 复用 EditActivityFormSheet

## 五、FormRow 扩展

无需扩展 FormRow，关联选择通过 LabelAction 的 `onClick` + overlay 实现。
