import type { Plugin } from "@opencode-ai/plugin"

const SUPER_CTX = `## 可用 Superpowers（技能）

以下是项目中可用的技能，在开始任何任务前必须先调用 Skill 工具检查是否有适用的技能：

### 流程技能（决定如何执行任务）
- **brainstorming** — 实现前必须探索需求与设计
- **test-driven-development** — 先写测试再写实现代码
- **systematic-debugging** — 遇到任何 bug 先系统化排查
- **writing-plans** — 多步骤任务先写书面实现计划
- **executing-plans** — 按审查检查点执行计划
- **verification-before-completion** — 完成前验证构建/测试
- **requesting-code-review** — 重要功能完成后请求审查
- **receiving-code-review** — 收到审查反馈后技术评估再实施
- **finishing-a-development-branch** — 分支收尾提供合并/PR选项
- **using-git-worktrees** — 隔离 git 工作树开发
- **dispatching-parallel-agents** — 并行子智能体执行独立任务
- **subagent-driven-development** — 执行包含独立任务的实现计划

### 领域技能（指导执行方式）
- **chinese-code-review** — 中文团队代码审查规范
- **chinese-commit-conventions** — 中文 Git 提交规范
- **chinese-documentation** — 中文技术文档写作规范
- **chinese-git-workflow** — 国内 Git 平台工作流

### 关键规则
1. 任何响应前先问自己：有没有技能适用？哪怕只有 1% 可能也要调用 Skill 工具检查
2. 不要把"简单问题"当作不检查技能的理由——问题就是任务
3. 不要凭记忆——技能会迭代更新，必须读取当前版本
4. 用户指令 > Superpowers 技能 > 默认系统提示`

export const SuperpowersAwareness: Plugin = async () => {
  return {
    "experimental.chat.system.transform": async (_input, output) => {
      output.system.push(SUPER_CTX)
    },
  }
}
