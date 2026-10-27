$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$portable = Join-Path $root '.tools\node-v22.16.0-win-x64'
if (Test-Path (Join-Path $portable 'node.exe')) { $env:PATH = "$portable;$env:PATH" }
$env:PLAYWRIGHT_BROWSERS_PATH = Join-Path $root '.tools/browsers'
try {
    Push-Location (Join-Path $root 'frontend')
    if (!(Test-Path node_modules)) { & npm.cmd ci; if ($LASTEXITCODE -ne 0) { throw 'Error instalando dependencias.' } }
    & npm.cmd exec playwright install chromium
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo preparar el navegador de pruebas.' }
    & npm.cmd exec playwright test
    if ($LASTEXITCODE -ne 0) { throw 'Hay pruebas de interfaz fallidas.' }
    Write-Host 'Interfaz verificada. Capturas en docs/evidencias e informe en frontend/playwright-report/index.html.'
} finally { Pop-Location }
