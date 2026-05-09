# 更新日志

## v0.1.0 (2026-05-09)

### 新功能

- **行为管理模块**：全新 feature:behavior_management 模块，支持行为记录筛选栏与时间范围选择器 UI、列表项/时间轴项 UI 组件、四维过滤逻辑、导入预览/导出确认弹窗、JSON 导入导出（含时间+活动查重）、导航路由与侧边抽屉入口
- **图标库系统**：统一图标渲染体系，包含 IconRenderer、IconPickerSheet（双 Tab 面板，含 Material Icons 和 Emoji 分类）、IconKeyResolver、MaterialIconCatalog/EmojiCatalog 数据源、i18n 字符串资源
- **起始秒数策略**：新增 SecondsStrategy 枚举与 DialogGridConfig 字段，DataStore 持久化，弹窗配置页选择 UI，行为编辑流程保留精确秒数
- **行为 UI 组件抽取**：创建 core:behaviorui 模块，将 Sheet 组件从 feature:home 移入，提供 AddBehaviorState 保留秒数、userAdjustedTime 标记、DualTimePicker 秒数回传
- **数据库迁移 v11→v12**：behaviors 外键 onDelete 从 RESTRICT 改为 CASCADE，级联删除
- **调试功能增强**：FieldDetailDialog（渲染/原生双 Tab）、FieldInfo 数据映射、标签/活动内容详情入口
- **新增查询接口**：数据层新增按时间范围查询行为详情方法

### 缺陷修复

- 修复删除活动崩溃及时间线实时更新问题
- 修复 IconPickerSheet 分类行重复"全部"标签，改为再次点击取消选中
- 修复 ConfirmDialog import 路径及包名引用，适配 core:designsystem
- 修复 .gitignore Icon 规则误忽略 icon 目录
- 修复 IconRenderer 替代 Text 拼接显示 iconKey
- 修复 GroupPickerPopup 解耦 ActivityGroup 编译问题
- 修复 ViewModelTest 构造参数适配 UseCase 注入
- 修复 debug 模块 sheet 组件 import 路径错误
- 修复合并后编译错误、参数签名、toModel 转换
- 修复行为管理模块多选删除逻辑及列表紧凑度

### 重构

- 全面 UI 重构 — 提取组件、拆分大文件、清理死代码
- 提取 AddTagUseCase/AddActivityUseCase 消除 ViewModel 间 CRUD 重复
- 提取共享 FormRowRenderers，消除 GenericForm 三重复制
- 提取共享 ConfirmDialog、GroupPickerPopup 到 core:designsystem
- 提取 Flow.mapList 扩展 + Behavior 转换逻辑移至 Model
- IconPickerSheet 改为居中弹窗、调换 Tab 顺序、默认选中分类、自动聚焦键盘
- 将所有 emoji 渲染替换为 IconRenderer
- 将调试组件 JSON 序列化改用 toJsonString()
- 调整 MomentView 排序逻辑

### 性能优化

- 性能优化 + 测试加固（b4ad03b）

### 测试

- 补充秒数策略默认值和持久化的单元测试
- 补充 FakeBehaviorDao / FakeBehaviorRepository 缺失的接口方法实现

### 文档

- 行为管理页面设计规格与实现计划
- 图标库集成实现计划
- 计时起始秒数保留设计规格
- TopBar V2 设计文档 — TopBarStyle 可配置化（STANDARD/NONE）

## v0.0.9 (2026-05-08)

### 新功能

- **当前时刻布局模式**：新增 MOMENT 布局模式，包含 MomentView、MomentFocusCard 三态聚焦卡片、SlideActionPill 滑动操作拉条组件，并完成 HomeScreen 集成
- **样式风格可配置化**：新增 StyleConfig 数据模型、ShapeTokens、StyleExt、Theme 扩展、DataStore 持久化、ViewModel；ThemeSettingsScreen 新增样式风格分区 UI 与实时预览区
- **标签活动关联**：重构底部弹窗，支持标签与活动关联
- **行为详情**：长按行为单元格显示详情，增强详情对话框（含时间戳字段）
- **匹配工具模块**：新增搜索/选择两种匹配模式、正则匹配工具、Tag keywords 字段、调试控制台
- **数据模型归档**：重构数据模型并添加归档功能
- **设置页面重构**：卡片入口页替代旧列表，导航壳按路由切换全局栏显示，二级页统一骨架与右侧滑入动画

### 缺陷修复

- 修复行为时间重叠检测与前端校验逻辑及 DAO 查询条件
- 修复首页多个时间显示和编辑模式相关 bug
- 修复 SlideActionPill 类型推断编译错误
- 修复 ALL_MIGRATIONS 声明顺序和缺失 import
- 修复高级自定义数值截取两位小数
- 修复 BehaviorLogView 遗漏的 8dp 圆角替换
- 修复设置模块二级页屏幕边距及闭合括号问题
- 移除合并冲突导致的重复 keywords 字段

### 重构

- 58 处硬编码圆角/alpha/边框改用 styledXxx 辅助函数
- 提取 BehaviorCalculator 纯函数，分离完成度计算逻辑
- 引入 ClockService 替换 System.currentTimeMillis() 硬编码
- 提取 TimeSnapService，分离时间吸附和冲突检测逻辑
- 提取 AddBehaviorUseCase，简化 ViewModel 业务逻辑
- 拆分 DI 模块为 DatabaseModule / ServiceModule / DataModule
- 统一实体-模型转换为 fromEntity / toEntity
- 统一路由常量 NLtimerRoutes，消除硬编码字符串
- 简化设置页面导航逻辑，统一二级页页面骨架

### 性能优化

- 全项目性能剖析：修复内存泄漏、过度重组、冷启动优化
- Slider 控件本地状态响应，松手后再写入 DataStore 消除卡顿

### 测试

- 新增 137+ 单元测试，覆盖核心仓库、工具函数、ViewModel、模型转换、迁移验证器、防御性分支和弹窗状态
- 新增设置模块导航层级、页面骨架和 UI 测试
- 新增时间冲突行为测试用例

### 文档

- 新增样式风格可配置化设计规格与实现计划
- 新增当前时刻布局模式设计规格
- 新增图标库集成设计规格 v2
- 新增架构与代码审查报告
- 新增时间轴/日志模式点击长按交互设计规格
