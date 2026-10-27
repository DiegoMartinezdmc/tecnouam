$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$running = $false
try {
    Invoke-WebRequest 'http://localhost:8080/api/equipos' -UseBasicParsing -TimeoutSec 2 | Out-Null
    $running = $true
} catch {
    if ($_.Exception.Response -and [int]$_.Exception.Response.StatusCode -eq 401) { $running = $true }
}
if ($running) {
    Write-Host 'El backend ya esta iniciado en http://localhost:8080. No se abrira otra instancia.'
    Write-Host 'Abre http://localhost:5173 para usar React. Para reiniciar, detiene primero el backend anterior con Ctrl+C.'
    exit 0
}
$envFile = Join-Path $root '.env'
if (!(Test-Path $envFile)) {
    $bytes = New-Object byte[] 48
    [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    $secret = [Convert]::ToBase64String($bytes)
    function New-Password {
        $buffer = New-Object byte[] 18
        [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
        [Convert]::ToBase64String($buffer)
    }
    @("JWT_SECRET=$secret", "ADMIN_PASSWORD=$(New-Password)", "USER_PASSWORD=$(New-Password)") | Set-Content -LiteralPath $envFile -Encoding ASCII
    Write-Host 'Configuracion local creada. Consulta las claves en .env (no se versiona).'
}
Get-Content -LiteralPath $envFile | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') }
}
Set-Location (Join-Path $root 'backend')
& mvn '-Dmaven.repo.local=../.m2' spring-boot:run
exit $LASTEXITCODE
