# 代码优化：提取重复逻辑为复用模块 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。

**目标：** 全面扫描代码库，识别重复模式，提取到共享模块，保持向后兼容且不改业务逻辑。

**架构：** 按"数据层 → 设计系统层 → 特性层"自底向上重构。先提取 Flow 工具扩展和 Model 转换，再提取共享 UI 组件，最后清理死代码。

**技术栈：** Kotlin, Jetpack Compose, Room, Hilt, StateFlow

---

## 重复代码清单总览

| # | 重复模式 | 严重度 | 出现次数 | 估计可消除行数 |
|---|---------|--------|---------|---------------|
| 1 | GenericFormSheet/Dialog 行渲染器完全重复 | CRITICAL | 3副本 | ~300行 |
| 2 | FormSpec 类层次结构三重复制 | CRITICAL | 3副本 | ~50行 |
| 3 | ConfirmDialog 三处重复 | HIGH | 3处 | ~90行 |
| 4 | GroupPickerPopup 精确重复 | HIGH | 2副本 | ~120行 |
| 5 | 表单Spec变更+颜色解析逻辑六处重复 | HIGH | 6处 | ~90行 |
| 6 | addTag()/addActivity() ViewModel方法重复 | HIGH | 2对 | ~35行 |
| 7 | 标签分类CRUD跨ViewModel重复 | HIGH | 2处 | ~30行 |
| 8 | Behavior转换逻辑放错位置 | MAJOR | 1处 | ~30行 |
| 9 | 手动Tag构造替代Tag.fromEntity() | MAJOR | 1处 | ~14行 |
| 10 | Flow列表转换16处重复 | HIGH | 16处 | ~16行 |
| 11 | 7个空DebugComponents死代码 | MEDIUM | 7文件 | ~70行 |
| 12 | tag_management旧版Dialog死代码 | HIGH | 2文件 | ~154行 |
| 13 | management_activities旧版Dialog死代码 | HIGH | 2文件 | ~303行 |
| 14 | RenameDialog三处重复 | MEDIUM | 3处 | ~100行 |
| 15 | Activity/Tag回调签名14处重复 | MINOR | 14处 | 类型别名 |

**估计总消除行数：~1550行**

---

## 文件结构

### 新建文件
- `core/data/src/main/java/com/nltimer/core/data/util/FlowExt.kt` — Flow.mapList
- `core/designsystem/src/main/java/com/nltimer/core/designsystem/component/ConfirmDialog.kt`
- `core/designsystem/src/main/java/com/nltimer/core/designsystem/component/GroupPickerPopup.kt`
- `core/designsystem/src/main/java/com/nltimer/core/designsystem/form/FormRowRenderers.kt`
- `core/data/src/main/java/com/nltimer/core/data/usecase/AddTagUseCase.kt`
- `core/data/src/main/java/com/nltimer/core/data/usecase/AddActivityUseCase.kt`

### 修改文件
- Behavior.kt — 添加 toEntity()/fromEntity()
- BehaviorRepositoryImpl.kt — 移除私有转换，用mapList
- TagRepositoryImpl.kt — 用mapList
- ActivityRepositoryImpl.kt — 用mapList
- ActivityManagementRepositoryImpl.kt — 用mapList
- GenericFormSheet.kt — 提取共享渲染器
- GenericFormDialog.kt — 用共享渲染器
- FormSpec.kt — 添加parseColorHex/withUpdatedLabelAction
- HomeViewModel.kt — 用共享UseCase
- TagManagementViewModel.kt — 用共享UseCase
- ActivityManagementViewModel.kt — 用共享UseCase
- TagManagementScreen.kt — 更新import
- ActivityManagementScreen.kt — 更新import
- CategoriesScreen.kt — 用共享ConfirmDialog
- ActivityFormSheets.kt — 用parseColorHex/withUpdatedLabelAction
- AddTagFormSheet.kt — 同上
- EditTagFormSheet.kt — 同上
- AddActivityDialog(home) — 同上
- AddTagDialog(home) — 同上
- DebugInitializer.kt — 清理空注册
- PickerSheets.kt — 用共享GroupPickerPopup
- HomeUiState.kt — 添加类型别名

### 删除文件
- tag_management ConfirmDialog.kt
- management_activities ConfirmDialog.kt
- management_activities GroupPickerPopup.kt
- management_activities AddActivityDialog.kt (死代码)
- management_activities EditActivityDialog.kt (死代码)
- tag_management AddTagDialog.kt (死代码)
- tag_management EditTagDialog.kt (死代码)
- feature/debug model/FormSpec.kt
- feature/debug ui/GenericFormSheet.kt
- 7个空DebugComponents文件

---

## 任务 1：创建 Flow.mapList 扩展

- [ ] 在 core/data/util/FlowExt.kt 创建扩展函数
- [ ] 替换16处 .map{list->list.map{}} 为 mapList
- [ ] Commit

## 任务 2：将 Behavior 转换逻辑移至 Model

- [ ] 在 Behavior.kt 添加 toEntity()/fromEntity()
- [ ] 删除 BehaviorRepositoryImpl 中私有转换函数
- [ ] 替换 BehaviorRepositoryImpl 中手动Tag构造为 Tag.fromEntity()
- [ ] Commit

## 任务 3：提取共享行渲染器消除 GenericForm 三重复制

- [ ] 创建 FormRowRenderers.kt，从 GenericFormSheet.kt 提取渲染器
- [ ] 修复 GenericFormDialog 颜色hex bug (toULong→toLong and 0xFFFFFFFFL)
- [ ] GenericFormSheet.kt 和 GenericFormDialog.kt 使用共享渲染器
- [ ] 删除 feature/debug 的 FormSpec 和 GenericFormSheet 副本
- [ ] 验证编译
- [ ] Commit

## 任务 4：提取共享 ConfirmDialog

- [ ] 创建 core/designsystem/component/ConfirmDialog.kt（含 confirmTextColor 参数）
- [ ] 更新 tag_management/management_activities Screen import
- [ ] 替换 categories CategoriesScreen 私有 DeleteConfirmDialog
- [ ] 删除两处 feature-local ConfirmDialog.kt
- [ ] 验证编译
- [ ] Commit

## 任务 5：提取共享 GroupPickerPopup

- [ ] 移动 management_activities GroupPickerPopup 到 core/designsystem
- [ ] 更新 ActivityFormSheets.kt import
- [ ] 更新 feature/debug PickerSheets.kt
- [ ] 删除 feature-local GroupPickerPopup.kt
- [ ] Commit

## 任务 6-7：删除死代码

- [ ] 确认 tag_management AddTagDialog/EditTagDialog 无引用后删除
- [ ] 确认 management_activities AddActivityDialog/EditActivityDialog 无引用后删除
- [ ] Commit

## 任务 8：清理空 DebugComponents

- [ ] 更新 DebugInitializer.kt 删除7行空注册
- [ ] 删除7个空 DebugComponents 文件
- [ ] Commit

## 任务 9：提取共享颜色解析和Spec变更逻辑

- [ ] 在 FormSpec.kt 添加 parseColorHex() 和 withUpdatedLabelAction()
- [ ] 在6个表单文件中替换重复代码
- [ ] Commit

## 任务 10：提取 addTag/addActivity 到共享 UseCase

- [ ] 创建 AddTagUseCase 和 AddActivityUseCase
- [ ] 在 DataModule 注册
- [ ] 替换3个 ViewModel 中的重复方法
- [ ] 验证测试通过
- [ ] Commit

## 任务 11：添加类型别名

- [ ] 在共享位置添加 AddTagCallback/AddActivityCallback typealias
- [ ] 在7个文件中替换回调签名
- [ ] Commit

## 任务 12：最终验证

- [ ] ./gradlew assembleDebug
- [ ] ./gradlew testDebugUnitTest
- [ ] ./gradlew lintDebug

---

## 无法直接复用的差异点标记

| 位置 | 差异点 | 原因 |
|------|--------|------|
| BehaviorRepositoryImpl.getTagsForBehaviors() 手动Tag构造 | row类型不是TagEntity | 联表查询返回类型不同 |
| CategoriesViewModel vs TagManagementViewModel 分类CRUD | 各维护独立状态 | 不同Feature的独立状态管理 |
| EditTagFormSheet vs AddTagFormSheet | 编辑模式有删除按钮和调试覆盖 | 需进一步参数化设计 |
| home/AddTagDialog vs tag_management/AddTagDialog | home版有CategoryPicker | home版功能更全 |
| AddSheetMode vs BehaviorNature 1:1映射 | AddSheetMode增加UI层语义 | 消除需改大量UI代码 |
