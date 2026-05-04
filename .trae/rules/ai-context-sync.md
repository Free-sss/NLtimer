---
alwaysApply: false
description: 第一次的对话窗口以及修改代码后同步更新 AI 上下文文档
scene: code_change
---
# AI 上下文文档同步规则

修改代码后，必须检查并同步更新 `docs/ai-context/` 下的文档，确保后续 AI agent 获取的信息始终准确。

## 流程

1. 读取 `docs/ai-context/MAINTENANCE.md` 获取完整触发条件表
2. 对照本次修改，判断哪些文档受影响
3. 只更新受影响的部分，不重写整个文档

## 触发条件速查

| 修改类型 | 更新文档 |
|---------|---------|
| 新增/删除/重命名模块 | 00-index、01-architecture、04-module-detail |
| 新增/修改数据表或字段 | 02-data-model |
| 新增/升级依赖 | 03-tech-stack |
| 新增/修改枚举、配置类 | 02-data-model 或 04-module-detail |
| 新增/修改 Route/Screen/ViewModel | 04-module-detail |
| 新增/修改数据库迁移 | 02-data-model |
| 修改命名/架构/代码规范 | 05-conventions |
| 修改导航路由 | 01-architecture |

## 不需要更新的情况

- 纯 UI 样式调整（颜色值、间距、动画参数）
- Bug 修复（不改变接口/结构）
- 资源文件修改（strings.xml、字体、图片）
- 注释修改
- 测试代码修改

## 更新原则

- 只改受影响的部分，不重写整个文档
- 新增条目追加到表格末尾
- 文档编号 00~05 固定不变
