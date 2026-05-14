$ErrorActionPreference = "Continue"

$null = [Console]::In.ReadToEnd()

$repoRoot = (& git rev-parse --show-toplevel 2>$null).Trim()
if ([string]::IsNullOrWhiteSpace($repoRoot)) {
    $repoRoot = (Get-Location).Path
}

$logDir = Join-Path $repoRoot ".codex\logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$logPath = Join-Path $logDir "installDebug-$timestamp.log"

$gradlew = Join-Path $repoRoot "gradlew.bat"
if (-not (Test-Path -LiteralPath $gradlew)) {
    @{
        continue = $true
        systemMessage = "Stop hook skipped: gradlew.bat was not found at $gradlew"
    } | ConvertTo-Json -Compress
    exit 0
}

Push-Location $repoRoot
try {
    & $gradlew installDebug *> $logPath
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

if ($exitCode -eq 0) {
    @{ continue = $true } | ConvertTo-Json -Compress
}
else {
    @{
        continue = $true
        systemMessage = "Stop hook: gradlew.bat installDebug exited with $exitCode. See $logPath"
    } | ConvertTo-Json -Compress
}
