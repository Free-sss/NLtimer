---
alwaysApply: false
description: 构建安装安卓应用时
---
构建测试 使用这个命令

```
.\gradlew.bat :app:assembleDebug; if ($LASTEXITCODE -eq 0) { adb -s ebc3de22 install -r "D:\2026Code\Group_android\NLtimer\app\build\outputs\apk\debug\app-debug.apk"; adb -s ebc3de22 shell am start -n "com.nltimer.app/com.nltimer.feature.debug.ui.DebugActivity" }
```

**禁止使用 `--no-daemon` 参数** — 会导致每次构建都冷启动 Gradle JVM，严重拖慢编译速度。始终使用默认的 daemon 模式。
