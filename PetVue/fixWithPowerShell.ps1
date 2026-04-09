# Read the file
$filePath = "src/views/institution/DashboardView.vue"
$content = Get-Content -Path $filePath -Raw -Encoding UTF8

# Replace the three stat-card divs
$content = $content -replace '(?s)<div class="stat-card">\s*<div class="stat-icon revenue">', '<div class="stat-card" @click="router.push('/institution/reports')">`n        <div class="stat-icon revenue">'
$content = $content -replace '(?s)<div class="stat-card">\s*<div class="stat-icon occupancy">', '<div class="stat-card" @click="router.push('/institution/orders')">`n        <div class="stat-icon occupancy">'
$content = $content -replace '(?s)<div class="stat-card">\s*<div class="stat-icon rating">', '<div class="stat-card" @click="router.push('/institution/reviews')">`n        <div class="stat-icon rating">'

# Write back with UTF8 without BOM
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Resolve-Path $filePath), $content, $utf8NoBom)

Write-Host "Fixed successfully!"
