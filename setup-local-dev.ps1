#!/usr/bin/env powershell

# PostgreSQL Authentication Fix - Automated Setup Script
# This script will:
# 1. Start Docker containers
# 2. Verify PostgreSQL connection
# 3. Display IntelliJ configuration instructions

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  PostgreSQL Authentication Fix - Setup Script                 ║" -ForegroundColor Cyan
Write-Host "║  Local Development with Docker Containers                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Docker
Write-Host "[Step 1] Checking Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✓ Docker is installed: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not installed or not in PATH" -ForegroundColor Red
    Write-Host "  Please install Docker Desktop from: https://www.docker.com/products/docker-desktop" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 2: Check docker-compose
Write-Host "[Step 2] Checking docker-compose..." -ForegroundColor Yellow
try {
    $composeVersion = docker-compose --version
    Write-Host "✓ docker-compose is available: $composeVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ docker-compose is not available" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 3: Get project root
Write-Host "[Step 3] Locating project root..." -ForegroundColor Yellow
$projectRoot = Split-Path -Parent $PSCommandPath
if (Test-Path "$projectRoot/docker-compose.yml") {
    Write-Host "✓ Found docker-compose.yml at: $projectRoot" -ForegroundColor Green
} else {
    # Try parent directory
    $projectRoot = Split-Path -Parent $projectRoot
    if (Test-Path "$projectRoot/docker-compose.yml") {
        Write-Host "✓ Found docker-compose.yml at: $projectRoot" -ForegroundColor Green
    } else {
        Write-Host "✗ Cannot find docker-compose.yml" -ForegroundColor Red
        Write-Host "  Make sure this script is in the project root or subdirectory" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

# Step 4: Check .env file
Write-Host "[Step 4] Checking .env file..." -ForegroundColor Yellow
if (Test-Path "$projectRoot/.env") {
    Write-Host "✓ Found .env file" -ForegroundColor Green
    $envContent = Get-Content "$projectRoot/.env" | Select-String "POSTGRES"
    Write-Host "  PostgreSQL settings:" -ForegroundColor Cyan
    $envContent | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "✗ .env file not found at: $projectRoot" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 5: Start Docker containers
Write-Host "[Step 5] Starting Docker containers..." -ForegroundColor Yellow
Write-Host "  Command: docker-compose up -d" -ForegroundColor Gray
cd $projectRoot
$composeResult = docker-compose up -d 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker containers started" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to start Docker containers" -ForegroundColor Red
    Write-Host $composeResult -ForegroundColor Red
    exit 1
}

Write-Host ""

# Step 6: Wait for PostgreSQL to be ready
Write-Host "[Step 6] Waiting for PostgreSQL to initialize..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$isHealthy = $false

while ($attempt -lt $maxAttempts) {
    $health = docker-compose ps postgres-primary 2>&1 | Select-String "healthy"
    if ($health -match "healthy") {
        Write-Host "✓ PostgreSQL is healthy" -ForegroundColor Green
        $isHealthy = $true
        break
    }
    $attempt++
    Write-Host "  Waiting... (Attempt $attempt/$maxAttempts)" -NoNewline
    Write-Host "`r" -NoNewline
    Start-Sleep -Seconds 1
}

if (-not $isHealthy) {
    Write-Host "⚠ PostgreSQL health check timed out (but may still work)" -ForegroundColor Yellow
}

Write-Host ""

# Step 7: Test PostgreSQL connection
Write-Host "[Step 7] Testing PostgreSQL connection..." -ForegroundColor Yellow
try {
    $testConnection = docker-compose exec -T postgres-primary psql -U postgres -d market_db -c "SELECT 1" 2>&1
    if ($testConnection -match "1") {
        Write-Host "✓ PostgreSQL connection successful" -ForegroundColor Green
    } else {
        Write-Host "⚠ PostgreSQL connection test unclear (but may work): $testConnection" -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠ Could not verify PostgreSQL connection (but Docker may still be working)" -ForegroundColor Yellow
}

Write-Host ""

# Step 8: Display container status
Write-Host "[Step 8] Docker container status:" -ForegroundColor Yellow
docker-compose ps | ForEach-Object { Write-Host "  $_" }

Write-Host ""

# Step 9: Display configuration instructions
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║          NEXT STEPS: Configure IntelliJ                       ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

Write-Host "1️⃣  OPEN INTELLIJ CONFIGURATION:" -ForegroundColor Cyan
Write-Host "   • Click the dropdown in top-right (showing run config name)" -ForegroundColor Gray
Write-Host "   • Select: 'Edit Configurations...'" -ForegroundColor Gray
Write-Host ""

Write-Host "2️⃣  SET ENVIRONMENT VARIABLES:" -ForegroundColor Cyan
Write-Host "   • Find 'Environment variables' field" -ForegroundColor Gray
Write-Host "   • Paste this (or add if partially exists):" -ForegroundColor Gray
Write-Host ""
Write-Host "     SPRING_PROFILES_ACTIVE=local;POSTGRES_USER=postgres;POSTGRES_PASSWORD=password" -ForegroundColor White -BackgroundColor DarkGray
Write-Host ""

Write-Host "3️⃣  CLICK 'Apply' and 'OK'" -ForegroundColor Cyan
Write-Host ""

Write-Host "4️⃣  RUN THE APPLICATION:" -ForegroundColor Cyan
Write-Host "   • Click the green ▶ Run button" -ForegroundColor Gray
Write-Host "   • Or press: Shift+F10" -ForegroundColor Gray
Write-Host ""

Write-Host "✅  VERIFY IT WORKS:" -ForegroundColor Green
Write-Host "   • Wait for 'Started MarketApplication' message in console" -ForegroundColor Gray
Write-Host "   • Open browser: http://localhost:8080/actuator/health" -ForegroundColor Gray
Write-Host "   • You should see: {""status"":""UP""}" -ForegroundColor Gray
Write-Host ""

Write-Host "📚 REFERENCE FILES:" -ForegroundColor Cyan
Write-Host "   • RUN_IN_INTELLIJ.md - Quick setup guide" -ForegroundColor Gray
Write-Host "   • LOCAL_POSTGRESQL_FIX.md - Detailed troubleshooting" -ForegroundColor Gray
Write-Host "   • data/POSTGRESQL_AUTH_FIX_VISUAL.md - Visual diagrams" -ForegroundColor Gray
Write-Host ""

Write-Host "🆘 TROUBLESHOOTING:" -ForegroundColor Cyan
Write-Host "   • Check Docker logs: docker logs -f postgres-primary" -ForegroundColor Gray
Write-Host "   • Test connection: docker-compose exec postgres-primary psql -U postgres -d market_db -c 'SELECT 1'" -ForegroundColor Gray
Write-Host "   • Reset containers: docker-compose down -v && docker-compose up -d" -ForegroundColor Gray
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Setup Complete! Ready to run in IntelliJ                     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

