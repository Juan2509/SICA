$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$envFile = Join-Path $projectRoot ".env"
$localConfigDirectory = Join-Path $env:USERPROFILE ".sica"
$localConfigFile = Join-Path $localConfigDirectory "conexion.properties"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker no esta instalado o no esta disponible en PATH. Instala y abre Docker Desktop."
}

docker info 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Docker Desktop esta instalado, pero no esta iniciado. Abre Docker Desktop e intenta otra vez."
}

if (-not (Test-Path -LiteralPath $envFile)) {
    $postgresPassword = "pg_" + [Guid]::NewGuid().ToString("N")
    $appPassword = "app_" + [Guid]::NewGuid().ToString("N")
    @(
        "POSTGRES_PASSWORD=$postgresPassword"
        "SICA_APP_PASSWORD=$appPassword"
    ) | Set-Content -LiteralPath $envFile -Encoding ASCII
} else {
    $values = @{}
    Get-Content -LiteralPath $envFile | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') { $values[$matches[1]] = $matches[2] }
    }
    $appPassword = $values["SICA_APP_PASSWORD"]
    if ([string]::IsNullOrWhiteSpace($appPassword)) {
        throw "El archivo .env no contiene SICA_APP_PASSWORD. Eliminalo y ejecuta este script otra vez."
    }
}

New-Item -ItemType Directory -Path $localConfigDirectory -Force | Out-Null
@(
    "# Configuracion local generada por iniciar-sica.ps1"
    "url=jdbc\:postgresql\://localhost\:5433/sica_db"
    "usuario=sica_app"
    "password=$appPassword"
) | Set-Content -LiteralPath $localConfigFile -Encoding ASCII

Push-Location $projectRoot
try {
    docker compose up -d --wait
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "SICA PostgreSQL esta listo en localhost:5433." -ForegroundColor Green
Write-Host "Ahora ejecuta la configuracion 'Ejecutar SICA' de VS Code." -ForegroundColor Cyan
