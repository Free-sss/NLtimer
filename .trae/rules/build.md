构建测试 使用这个命令

```
.\gradlew.bat :app:assembleDebug; if ($LASTEXITCODE -eq 0) { adb -s ebc3de22 install -r "D:\2026Code\Group_android\NLtimer\app\build\outputs\apk\debug\app-debug.apk"; adb -s ebc3de22 shell am start -n "com.nltimer.app/com.nltimer.feature.debug.ui.DebugActivity" }
```
