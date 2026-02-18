#!/bin/bash
set -e

echo "Starting PostgreSQL initialization..."

# Try to create database - ignore error if it already exists
psql -U "$POSTGRES_USER" -d "postgres" -c "CREATE DATABASE $POSTGRES_DB;" 2>/dev/null || true

echo "Database $POSTGRES_DB ready"

# Install UUID extension
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

echo "PostgreSQL initialization completed successfully"

