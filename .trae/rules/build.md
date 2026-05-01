执行

```
cd "D:\2026Code\Group_android\NLtimer"; .\gradlew.bat :app:assembleDebug --no-daemon; if ($LASTEXITCODE -eq 0) { adb -s ebc3de22 install -r "D:\2026Code\Group_android\NLtimer\app\build\outputs\apk\debug\app-debug.apk"; adb -s ebc3de22 shell am start -n "com.nltimer.app/com.nltimer.app.MainActivity" }
```
