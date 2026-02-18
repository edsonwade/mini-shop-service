# 🐳 Docker Database Fix - market_db Not Found

## Problem
```
FlywaySqlException: Unable to obtain connection from database: 
FATAL: database "market_db" does not exist
```

## Root Causes
1. **Database Name Mismatch**: docker-compose.yml used `orders_db` but application expects `market_db`
2. **MongoDB Database Name**: Also used `orders_db` instead of `market_db`

## Solutions Applied

### Fix #1: Update docker-compose.yml
Changed all PostgreSQL and MongoDB database names from `orders_db`/`orders_test` to `market_db`/`market_test`:

```yaml
# PostgreSQL Primary
postgres-primary:
  environment:
    POSTGRES_DB: market_db  # ✅ Changed from orders_db

# PostgreSQL Replica
postgres-replica:
  environment:
    POSTGRES_DB: market_db  # ✅ Changed from orders_db

# PostgreSQL Test
postgres-test:
  environment:
    POSTGRES_DB: market_test  # ✅ Changed from orders_test

# MongoDB
mongo-primary:
  environment:
    MONGO_INITDB_DATABASE: market_db  # ✅ Changed from orders_db
```

### Fix #2: Add Database Initialization Script
Created `init-db.sh` to ensure database exists and UUID extension is installed:

```bash
#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE IF NOT EXISTS $POSTGRES_DB;
    \c $POSTGRES_DB
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL
```

Added to docker-compose.yml:
```yaml
postgres-primary:
  volumes:
    - ./init-db.sh:/docker-entrypoint-initdb.d/init-db.sh:ro
```

### Fix #3: Verify .env File
The `.env` file already has correct values:
```env
POSTGRES_DB=market_db  # ✅ Correct
MONGO_DB=market_db     # ✅ Correct
```

## What Changed

| Component | Before | After | Status |
|-----------|--------|-------|--------|
| PostgreSQL Primary DB | orders_db | market_db | ✅ Fixed |
| PostgreSQL Replica DB | orders_db | market_db | ✅ Fixed |
| PostgreSQL Test DB | orders_test | market_test | ✅ Fixed |
| MongoDB DB | orders_db | market_db | ✅ Fixed |
| Init Script | (missing) | init-db.sh | ✅ Added |

## How to Deploy

### Step 1: Clean up old containers and volumes
```bash
docker-compose down -v
docker volume rm $(docker volume ls -q)
```

### Step 2: Rebuild and start containers
```bash
docker-compose up -d
```

### Step 3: Verify database creation
```bash
# Connect to PostgreSQL
docker exec -it postgres-primary psql -U postgres -d market_db

# List databases
\l

# Exit
\q
```

### Step 4: Check application logs
```bash
docker logs mini-shop-service
```

## Expected Behavior

✅ PostgreSQL container starts
✅ `init-db.sh` runs automatically
✅ `market_db` database is created
✅ UUID extension is installed
✅ Flyway migrations run successfully
✅ Application connects to database

## Troubleshooting

### If database still doesn't exist:
```bash
# Manually create it
docker exec -it postgres-primary psql -U postgres -d postgres \
  -c "CREATE DATABASE market_db;"
```

### If UUID extension fails:
```bash
# Manually install extension
docker exec -it postgres-primary psql -U postgres -d market_db \
  -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"
```

### Check PostgreSQL container logs:
```bash
docker logs postgres-primary
```

### Check application startup logs:
```bash
docker logs mini-shop-service | tail -100
```

## Files Modified
- ✅ `docker-compose.yml` - Database names updated
- ✅ `init-db.sh` - Database initialization script created
- ✅ `.env` - Already correct (no changes needed)

## Next Steps
1. Run: `docker-compose down -v`
2. Run: `docker-compose up -d`
3. Monitor: `docker logs mini-shop-service`
4. Verify: Application should start without database errors

---

**Status**: ✅ Ready to Deploy
**Date**: 2026-02-16

