#!/bin/bash
# Quick Setup Guide - Bash Alternative (for Mac/Linux users)
# For Windows: Use setup-local-dev.ps1 or follow manual steps below

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  PostgreSQL Auth Fix - Setup Guide                            ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Start Docker
echo "[Step 1] Starting Docker containers..."
docker-compose up -d

echo "[Step 2] Waiting for PostgreSQL to be healthy..."
sleep 10

echo "[Step 3] Verifying PostgreSQL connection..."
docker-compose exec -T postgres-primary psql -U postgres -d market_db -c "SELECT 1"

echo ""
echo "✅ Docker setup complete!"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "NEXT: Configure IntelliJ"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "1. In IntelliJ: Click the dropdown in top-right (run config name)"
echo "2. Select: 'Edit Configurations...'"
echo "3. Find your Spring Boot configuration"
echo "4. Go to 'Environment variables' field"
echo "5. Add:"
echo ""
echo "   SPRING_PROFILES_ACTIVE=local;POSTGRES_USER=postgres;POSTGRES_PASSWORD=password"
echo ""
echo "6. Click Apply → OK"
echo "7. Click Run ▶ button"
echo ""
echo "✅ Application should start without authentication errors!"
echo ""

