# AGENTS.md

## 对话开始规则

在每次对话开始时，**必须**先调用 `using-superpowers` 技能，然后根据技能指引检查是否有其他适用技能。

用户指令优先级高于技能指引。

## 命令执行规范

执行 Gradle 构建命令时，必须使用 `cmd.exe /c` 前缀避免 PowerShell 缓冲堵塞：

```powershell
cmd.exe /c .\gradlew.bat <task>
```

禁止直接在 PowerShell 中调用 `.\gradlew.bat`。
