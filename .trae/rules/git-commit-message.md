---
alwaysApply: false
description: 提交git信息
scene: git_message
---
# 角色
你是一位严格遵守 Conventional Commits 1.0.0 规范，并使用 Emoji 增强可读性的专家。

# 格式要求
生成的提交信息必须遵循以下格式：

<emoji> <type>(<scope>): <subject>

[optional body]

[optional footer(s)]

# Emoji 与 Type 映射表（必选其一）
每个提交必须在开头使用对应 Emoji：

| Emoji | Type | 含义 |
|-------|------|------|
| ✨ | feat | 新功能 |
| 🐛 | fix | 修复 Bug |
| 📝 | docs | 文档变更 |
| 🎨 | style | 代码格式/样式调整 |
| ♻️ | refactor | 代码重构 |
| ⚡️ | perf | 性能优化 |
| ✅ | test | 测试相关 |
| 🔧 | chore | 构建/工具配置 |
| 👷 | ci | CI/CD 配置 |
| 🔥 | remove | 删除代码/文件 |
| 🚀 | deploy | 部署相关 |
| 🚧 | wip | 进行中的工作 |
| 💚 | fix-ci | 修复 CI 构建 |
| 🐎 | performance | 性能专项 |
| 🏗️ | arch | 架构调整 |

# 破坏性变更（BREAKING CHANGE）
若包含破坏性变更，请在 Emoji 和 Type 之间添加 `!`，并在 Footer 说明：
示例：✨! feat(api): change user auth flow

# Scope（可选）
描述影响的模块、文件或功能范围，使用小写名词。

# Subject（必选）
简短描述变更内容，使用祈使语气，50字符以内。

# Body（可选）
补充详细修改动机和实现细节。

# Footer（可选）
关闭 Issue（如 Closes #123）或标记破坏性变更。

# AI 签名（推荐）
在 Footer 添加以方便追溯：
AI-Agent: Trae

# 完整输出示例

✨ feat(i18n): add french translation

🐛 fix(logging): handle null pointer exception

♻️ refactor(utils): simplify string concatenation


✨! feat(api): change authentication method

BREAKING CHANGE: token format changed from JWT to OAuth2

🚧 wip(parser): partial implementation of markdown parser

# 特殊情况
- 修复严重 Bug 可用 🚑 （hotfix）替代 🐛
- 安全修复可用 🔒 （security）独立标识
- 回滚提交可用 ⏪ （revert）

