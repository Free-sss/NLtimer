$file = "D:\2026Code\Group_android\NLtimer\app\src\main\java\com\nltimer\app\component\RouteSettingsPopup.kt"
$content = Get-Content $file -Raw

# Add missing imports
$old = @"
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
"@

$new = @"
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
"@

$content = $content.Replace($old, $new)

# Add MOMENT branch to when expression
$old2 = @"
                                    HomeLayout.LOG -> "行为日志"
                                },
"@

$new2 = @"
                                    HomeLayout.LOG -> "行为日志"
                                    HomeLayout.MOMENT -> "瞬间"
                                },
"@

$content = $content.Replace($old2, $new2)

[System.IO.File]::WriteAllText($file, $content, [System.Text.Encoding]::UTF8)
Write-Host "Done"
