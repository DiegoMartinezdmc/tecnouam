$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$portable = Join-Path $root '.tools\node-v22.16.0-win-x64'
if (Test-Path (Join-Path $portable 'node.exe')) { $env:PATH = "$portable;$env:PATH" }
Set-Location (Join-Path $root 'frontend')
if (!(Get-Command npm.cmd -ErrorAction SilentlyContinue)) { throw 'Instala Node.js 22.12 o superior y vuelve a abrir Visual Studio Code.' }
if (!(Test-Path node_modules)) { & npm.cmd install; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE } }
& npm.cmd run dev
