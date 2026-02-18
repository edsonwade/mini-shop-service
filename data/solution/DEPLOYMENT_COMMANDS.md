# ⚡ Docker Deployment - Copy & Paste Commands

## 🚀 ONE-COMMAND DEPLOYMENT

```bash
docker-compose down -v && docker-compose up -d && echo "Waiting 60 seconds for startup..." && sleep 60 && docker logs mini-shop-service | grep -E "Started|Error|failed"
```

---

## 📋 STEP-BY-STEP DEPLOYMENT

### Step 1: Navigate to Project
```bash
cd ~/mini-market-system
# or
cd C:\Users\HP\Documents\development\Java\Projects\mini-market-system
```

### Step 2: Clean Previous Deployment
```bash
# Stop and remove all containers
docker-compose down

# Remove volumes (CAUTION: Data loss!)
docker-compose down -v

# Alternative - complete cleanup
docker-compose down -v && docker volume prune -f && docker image prune -f
```

### Step 3: Build Application Image
```bash
# Build the Docker image
docker build -t mini-shop-service:latest .

# Build and show output
docker build -t mini-shop-service:latest . --progress=plain
```

### Step 4: Start All Services
```bash
# Start in detached mode
docker-compose up -d

# Start and show logs (good for first-time)
docker-compose up -d && docker-compose logs -f
```

### Step 5: Wait for Services
```bash
# Wait 30-60 seconds, then check status
sleep 60

# Then check:
docker-compose ps
```

---

## ✅ VERIFICATION COMMANDS

### Check All Containers
```bash
docker-compose ps

# Expected output:
# NAME                    STATUS
# mini-shop-service       Up (healthy)
# postgres-primary        Up (healthy)
# postgres-replica        Up (healthy)
# mongo-primary          Up (healthy)
# redis                  Up (healthy)
# kafka                  Up (healthy)
# mailhog                Up (healthy)
```

### Check Application Startup
```bash
docker logs mini-shop-service | grep -E "Started|Error|failed"

# Expected: "Started MarketApplication in X seconds"
```

### Check Database Created
```bash
docker exec -it postgres-primary psql -U postgres -l | grep market_db

# Expected: Shows market_db row
```

### Verify UUID Extension
```bash
docker exec -it postgres-primary psql -U postgres -d market_db \
  -c "SELECT extname FROM pg_extension WHERE extname = 'uuid-ossp';"

# Expected: uuid-ossp
```

### Test API Health
```bash
curl http://localhost:8081/actuator/health

# Expected: {"status":"UP"}
```

### Test Application Metrics
```bash
curl http://localhost:8081/actuator/prometheus | head -20

# Expected: Prometheus metrics output
```

---

## 📊 COMMON MONITORING COMMANDS

### Follow Application Logs (Real-time)
```bash
docker-compose logs -f mini-shop-service

# Follow all services
docker-compose logs -f

# Follow last 100 lines
docker-compose logs --tail=100 mini-shop-service

# Follow and filter errors
docker-compose logs -f mini-shop-service | grep -i error
```

### Check Container Status
```bash
# Quick status
docker-compose ps

# Detailed status
docker-compose ps --format "table {{.Service}}\t{{.Status}}\t{{.Ports}}"

# Watch in real-time
watch -n 1 docker-compose ps
```

### Check Specific Service Logs
```bash
# PostgreSQL
docker logs postgres-primary | tail -50

# MongoDB
docker logs mongo-primary | tail -50

# Kafka
docker logs kafka | tail -50

# Redis
docker logs redis | tail -50
```

---

## 🗄️ DATABASE COMMANDS

### Access PostgreSQL
```bash
# Connect to database
docker exec -it postgres-primary psql -U postgres -d market_db

# Once in psql:
\dt                    # List tables
\l                     # List databases
SELECT * FROM users;   # Query example
\q                     # Exit
```

### Access MongoDB
```bash
docker exec -it mongo-primary mongosh

# Once in mongosh:
show dbs               # List databases
use market_db          # Switch to database
show collections       # List collections
db.audit_events.count() # Count documents
exit                   # Exit
```

### Access Redis
```bash
docker exec -it redis redis-cli -a redis_password

# Once in redis-cli:
PING                   # Test connection
KEYS *                 # Show all keys
GET key_name           # Get value
FLUSHDB                # Clear all keys
EXIT                   # Exit
```

---

## 🧹 CLEANUP COMMANDS

### Stop Services (Keep Data)
```bash
docker-compose stop
```

### Stop and Remove Containers (Keep Data)
```bash
docker-compose down
```

### Remove Everything (Delete Data)
```bash
docker-compose down -v
```

### Full Cleanup
```bash
# Remove containers, volumes, networks
docker-compose down -v

# Remove unused volumes
docker volume prune -f

# Remove unused images
docker image prune -f

# Remove dangling images
docker image prune -a -f

# Remove all mini-shop containers
docker container rm $(docker container ls -q -f "label=com.docker.compose.service=mini-shop-service")
```

### Clean Specific Resources
```bash
# Remove only volumes
docker volume rm $(docker volume ls -q)

# Remove only mini-shop volumes
docker volume rm $(docker volume ls -q | grep mini)

# Remove only stopped containers
docker container prune -f
```

---

## 🔧 TROUBLESHOOTING COMMANDS

### If Database Connection Fails
```bash
# Check PostgreSQL is running
docker exec postgres-primary pg_isready -U postgres

# Check if database exists
docker exec -it postgres-primary psql -U postgres -l

# Manually create database if missing
docker exec -it postgres-primary psql -U postgres \
  -c "CREATE DATABASE IF NOT EXISTS market_db;"

# Check container logs
docker logs postgres-primary | tail -100
```

### If UUID Extension Missing
```bash
# Install manually
docker exec -it postgres-primary psql -U postgres -d market_db \
  -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"

# Verify installation
docker exec -it postgres-primary psql -U postgres -d market_db \
  -c "SELECT * FROM pg_extension WHERE extname = 'uuid-ossp';"
```

### If Application Won't Start
```bash
# Check full logs
docker logs mini-shop-service

# Check error details (last 200 lines)
docker logs mini-shop-service | tail -200

# Check specific errors
docker logs mini-shop-service | grep -i error

# Check Flyway migrations
docker logs mini-shop-service | grep -i flyway
```

### If Port is Already in Use
```bash
# Find what's using port 8081
lsof -i :8081

# Find what's using port 5433
lsof -i :5433

# Kill the process
kill -9 <PID>

# Or change port in docker-compose.yml
```

---

## 📈 MONITORING & OBSERVABILITY

### Access Monitoring Tools
```bash
# Prometheus (metrics)
open http://localhost:9090
# or
curl http://localhost:9090

# Grafana (dashboards)
open http://localhost:3000
# Username: admin
# Password: admin

# Swagger API Documentation
open http://localhost:8081/swagger-ui.html

# Redis Commander
open http://localhost:8081

# Mailhog (email debugging)
open http://localhost:8025
```

### Get Application Metrics
```bash
# All metrics
curl http://localhost:8081/actuator/prometheus

# Health information
curl http://localhost:8081/actuator/health | jq .

# Application info
curl http://localhost:8081/actuator/info | jq .

# Active endpoints
curl http://localhost:8081/actuator | jq .
```

---

## 🔄 RESTART COMMANDS

### Restart All Services
```bash
docker-compose restart

# Wait for startup
sleep 30

# Check status
docker-compose ps
```

### Restart Specific Service
```bash
# Restart application
docker-compose restart mini-shop-service

# Restart PostgreSQL
docker-compose restart postgres-primary

# Restart with logs
docker-compose restart mini-shop-service && docker-compose logs -f mini-shop-service
```

---

## 🎯 COMPLETE DEPLOYMENT WORKFLOW

### Fresh Start (Recommended for First-Time)
```bash
# Navigate to project
cd ~/mini-market-system

# Complete cleanup
docker-compose down -v && docker volume prune -f && docker image prune -f

# Build image
docker build -t mini-shop-service:latest .

# Start services
docker-compose up -d

# Watch startup (Ctrl+C after ~60 seconds)
docker-compose logs -f

# Verify deployment
docker-compose ps
curl http://localhost:8081/actuator/health
```

### Quick Redeploy (After Code Changes)
```bash
# Stop current deployment
docker-compose down -v

# Rebuild image
docker build -t mini-shop-service:latest .

# Deploy
docker-compose up -d && sleep 60 && docker logs mini-shop-service | grep "Started"
```

### Production Deployment
```bash
# Full cleanup
docker-compose down -v && docker volume prune -f

# Build with optimizations
docker build -t mini-shop-service:latest --progress=plain .

# Start with monitoring
docker-compose up -d

# Verify all services
docker-compose ps

# Show startup time
docker logs mini-shop-service | grep "Started"

# Test API
curl http://localhost:8081/swagger-ui.html
```

---

## 💾 BACKUP & RESTORE

### Backup Database
```bash
# Backup PostgreSQL
docker exec postgres-primary pg_dump -U postgres market_db > backup.sql

# Backup with compression
docker exec postgres-primary pg_dump -U postgres market_db | gzip > backup.sql.gz

# Backup MongoDB
docker exec mongo-primary mongodump --db market_db --archive=backup.archive
```

### Restore Database
```bash
# Restore PostgreSQL
docker exec -i postgres-primary psql -U postgres market_db < backup.sql

# Restore from compressed file
gunzip -c backup.sql.gz | docker exec -i postgres-primary psql -U postgres market_db

# Restore MongoDB
docker exec -i mongo-primary mongorestore --archive < backup.archive
```

---

## 📝 USEFUL ALIASES (Add to ~/.bashrc or ~/.zshrc)

```bash
# Docker compose shortcuts
alias dc='docker-compose'
alias dcu='docker-compose up -d'
alias dcd='docker-compose down'
alias dcl='docker-compose logs -f'
alias dcs='docker-compose ps'
alias dcr='docker-compose restart'

# Mini-shop specific
alias mm-start='docker-compose up -d && sleep 60 && docker logs mini-shop-service | grep "Started"'
alias mm-stop='docker-compose down'
alias mm-logs='docker-compose logs -f mini-shop-service'
alias mm-health='curl http://localhost:8081/actuator/health'
alias mm-swagger='open http://localhost:8081/swagger-ui.html'
alias mm-db='docker exec -it postgres-primary psql -U postgres -d market_db'
```

---

**Print this page and keep it handy during deployment!** 📋

Last Updated: 2026-02-16
Status: ✅ Ready to Deploy

