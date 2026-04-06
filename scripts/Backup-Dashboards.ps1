<#
.SYNOPSIS
    Backs up all Grafana dashboards to a timestamped directory.

.DESCRIPTION
    Calls the Grafana HTTP API to export every dashboard as a JSON file.
    Output is written to  backups/grafana/<YYYY-MM-DD>/  relative to the
    project root.  Run from the project root:

        .\scripts\Backup-Dashboards.ps1

    Optionally pass credentials / URL overrides:

        .\scripts\Backup-Dashboards.ps1 -GrafanaUrl http://localhost:3030 `
            -User admin -Password secret

.PARAMETER GrafanaUrl
    Base URL of the Grafana instance.  Default: http://localhost:3030

.PARAMETER User
    Grafana admin username.  Default: admin

.PARAMETER Password
    Grafana admin password.  Default: admin123

.PARAMETER OutputDir
    Directory under which a dated sub-folder is created.
    Default: <project-root>/backups/grafana
#>
param(
    [string]$GrafanaUrl = "http://localhost:3030",
    [string]$User       = "admin",
    [string]$Password   = "admin123",
    [string]$OutputDir  = "$PSScriptRoot\..\backups\grafana"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$date      = Get-Date -Format "yyyy-MM-dd_HH-mm"
$backupDir = Join-Path $OutputDir $date
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

$authHeader = @{
    Authorization = "Basic " + [Convert]::ToBase64String(
        [Text.Encoding]::ASCII.GetBytes("${User}:${Password}"))
}

Write-Host "🔍  Discovering dashboards at $GrafanaUrl ..."
try {
    $searchUrl = "$GrafanaUrl/api/search?type=dash-db&limit=500"
    $dashboards = Invoke-RestMethod -Uri $searchUrl -Headers $authHeader -Method Get
} catch {
    Write-Error "Failed to reach Grafana at $GrafanaUrl — is the container running?`n$_"
    exit 1
}

if ($dashboards.Count -eq 0) {
    Write-Warning "No dashboards found. Nothing to back up."
    exit 0
}

Write-Host "📦  Backing up $($dashboards.Count) dashboard(s) → $backupDir"
$success = 0
$failed  = 0

foreach ($db in $dashboards) {
    $uid   = $db.uid
    $title = $db.title -replace '[\\/:*?"<>|]', '_'   # sanitise filename
    try {
        $detail   = Invoke-RestMethod -Uri "$GrafanaUrl/api/dashboards/uid/$uid" `
                        -Headers $authHeader -Method Get
        $filePath = Join-Path $backupDir "$uid-$title.json"
        $detail | ConvertTo-Json -Depth 20 | Set-Content $filePath -Encoding UTF8
        Write-Host "  ✅  $title  →  $uid-$title.json"
        $success++
    } catch {
        Write-Warning "  ❌  Failed to export '$title' (uid=$uid): $_"
        $failed++
    }
}

# Write a manifest
$manifest = [PSCustomObject]@{
    exported_at   = (Get-Date -Format "o")
    grafana_url   = $GrafanaUrl
    total         = $dashboards.Count
    success       = $success
    failed        = $failed
    dashboards    = $dashboards | Select-Object uid, title, folderTitle
}
$manifest | ConvertTo-Json -Depth 5 |
    Set-Content (Join-Path $backupDir "manifest.json") -Encoding UTF8

Write-Host ""
Write-Host "✔  Backup complete: $success exported, $failed failed → $backupDir"
if ($failed -gt 0) { exit 1 }
