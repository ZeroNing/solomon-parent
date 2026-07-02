param(
    [Parameter(Position = 0)]
    [ValidateSet("list", "validate", "config", "up", "down", "restart", "logs", "pull")]
    [string]$Action = "list",

    [Parameter(Position = 1)]
    [string]$Service
)

$ErrorActionPreference = "Stop"
$DockerRoot = $PSScriptRoot

function Get-DeploymentUnits {
    Get-ChildItem -LiteralPath $DockerRoot -Recurse -Filter "docker-compose.yml" |
        Sort-Object FullName
}

if ($Action -eq "list") {
    Get-DeploymentUnits | ForEach-Object {
        $_.Directory.FullName.Substring($DockerRoot.Length + 1)
    }
    exit 0
}

if ($Action -eq "validate") {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "Docker command not found. Install Docker Desktop or Docker Engine first."
    }

    $Failures = @()
    foreach ($Unit in Get-DeploymentUnits) {
        & docker compose `
            --project-directory $Unit.DirectoryName `
            -f $Unit.FullName `
            config --quiet
        if ($LASTEXITCODE -ne 0) {
            $Failures += $Unit.FullName
        }
    }

    if ($Failures.Count -gt 0) {
        $FailureText = $Failures -join [Environment]::NewLine
        throw "Compose validation failed:$([Environment]::NewLine)$FailureText"
    }

    Write-Host "All deployment units passed validation. Total: $((Get-DeploymentUnits).Count)."
    exit 0
}

if ([string]::IsNullOrWhiteSpace($Service)) {
    throw "Specify a deployment unit, for example: .\manage.ps1 up mysql"
}

$ComposeFile = Join-Path (Join-Path $DockerRoot $Service) "docker-compose.yml"
if (-not (Test-Path -LiteralPath $ComposeFile -PathType Leaf)) {
    throw "Deployment unit not found: $Service"
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker command not found. Install Docker Desktop or Docker Engine first."
}

$ServiceDirectory = Split-Path $ComposeFile
$CommonArgs = @("compose", "--project-directory", $ServiceDirectory, "-f", $ComposeFile)

switch ($Action) {
    "config" {
        & docker @CommonArgs config
    }
    "up" {
        & docker @CommonArgs up -d
    }
    "down" {
        & docker @CommonArgs down
    }
    "restart" {
        & docker @CommonArgs restart
    }
    "logs" {
        & docker @CommonArgs logs --tail 200 -f
    }
    "pull" {
        & docker @CommonArgs pull
    }
}

if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose failed with exit code $LASTEXITCODE."
}
