param([switch]$Api)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$portable = Join-Path $root '.tools\node-v22.16.0-win-x64'
if (Test-Path (Join-Path $portable 'node.exe')) { $env:PATH = "$portable;$env:PATH" }

try {
    Push-Location (Join-Path $root 'backend')
    & mvn '-Dmaven.repo.local=../.m2' test
    if ($LASTEXITCODE -ne 0) { throw 'Las pruebas del backend fallaron.' }
} finally { Pop-Location }

try {
    Push-Location (Join-Path $root 'frontend')
    if (!(Test-Path node_modules)) {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw 'No se pudieron instalar las dependencias React.' }
    }
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion React fallo.' }
} finally { Pop-Location }

if ($Api) {
    try { Invoke-WebRequest 'http://localhost:8080/api/equipos' -UseBasicParsing | Out-Null }
    catch {
        if (!$_.Exception.Response -or [int]$_.Exception.Response.StatusCode -ne 401) {
            throw 'Inicia el backend en otra terminal con scripts/start-backend.ps1 antes de usar -Api.'
        }
    }
    if (!(Test-Path (Join-Path $root '.tools/qa/node_modules/newman'))) {
        & npm.cmd install --prefix (Join-Path $root '.tools/qa') newman
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo instalar Newman.' }
    }
    & node (Join-Path $PSScriptRoot 'verify-postman.cjs')
    if ($LASTEXITCODE -ne 0) { throw 'La coleccion Postman fallo.' }
}
Write-Host 'Verificacion completada correctamente.'
