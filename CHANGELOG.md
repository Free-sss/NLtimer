# 更新日志

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
