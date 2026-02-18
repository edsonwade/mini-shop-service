# 🚀 Quick Start Guide - Docker Deployment

## Prerequisites
- Docker Engine 20.10+
- Docker Compose 2.0+
- 4GB+ RAM available
- Ports 5432, 5433, 5435, 6379, 8080, 8081, 27017, 9092, 3000 free

## Deployment Steps

### 1. Clean Previous Deployment
```bash
cd /path/to/mini-market-system

# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove unused volumes
docker volume prune -f
```

### 2. Build Application Image
```bash
# Build the Docker image
docker build -t mini-shop-service:latest .
```

### 3. Start All Services
```bash
# Start all containers in detached mode
docker-compose up -d

# Watch startup progress
docker-compose logs -f

# Wait 60-90 seconds for all services to be healthy
```

### 4. Verify Deployment

#### Check Container Health
```bash
docker-compose ps

# Should show:
# shop-management-system    Up (healthy)
# postgres-primary          Up (healthy)
# postgres-replica          Up (healthy)
# redis                     Up (healthy)
# mongo-primary             Up (healthy)
# kafka                     Up (healthy)
# mailhog                   Up (healthy)
```

#### Check Application Startup
```bash
docker logs mini-shop-service | grep -E "Started|Application|Error"

# Expected output:
# [...] Started MarketApplication in X seconds
```

#### Verify Database
```bash
docker exec -it postgres-primary psql -U postgres -d market_db -c "\l"

# Should list market_db in databases
```

#### Check API Health
```bash
curl -s http://localhost:8081/actuator/health | jq .

# Should return: {"status":"UP"}
```

## Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Application API** | http://localhost:8081 | - |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | - |
| **PostgreSQL** | localhost:5433 | postgres / postgres |
| **PostgreSQL Replica** | localhost:5435 | postgres / postgres |
| **MongoDB** | mongodb://localhost:27017 | - |
| **Redis** | localhost:6379 | (requirepass: redis_password) |
| **Redis Commander** | http://localhost:8081 | - |
| **Kafka** | localhost:9092 | - |
| **Mailhog** | http://localhost:8025 | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin / admin |

## Common Operations

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f mini-shop-service

# Last 100 lines
docker-compose logs --tail=100 mini-shop-service
```

### Stop Services
```bash
# Stop all containers (keep volumes)
docker-compose stop

# Stop and remove containers
docker-compose down
```

### Restart Services
```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart mini-shop-service
```

### Database Access
```bash
# Connect to primary PostgreSQL
docker exec -it postgres-primary psql -U postgres -d market_db

# Connect to MongoDB
docker exec -it mongo-primary mongosh

# Access Redis CLI
docker exec -it redis redis-cli -a redis_password
```

### Monitor Application
```bash
# Real-time metrics
curl -s http://localhost:8081/actuator/prometheus

# Health details
curl -s http://localhost:8081/actuator/health | jq .

# Application info
curl -s http://localhost:8081/actuator/info | jq .
```

## Troubleshooting

### Application won't start
```bash
# Check logs for errors
docker logs mini-shop-service

# Verify database is ready
docker logs postgres-primary | grep "database system is ready"

# Verify all dependencies are healthy
docker-compose ps
```

### Database connection error
```bash
# Verify PostgreSQL is running
docker exec -it postgres-primary pg_isready -U postgres

# Check if market_db exists
docker exec -it postgres-primary psql -U postgres -c "\l"

# If missing, create it
docker exec -it postgres-primary psql -U postgres -d postgres \
  -c "CREATE DATABASE market_db;"
```

### Port already in use
```bash
# Find process using the port (e.g., 8081)
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or use different port in docker-compose.yml
```

### Redis connection issues
```bash
# Test Redis connection
docker exec -it redis redis-cli -a redis_password ping

# Should return: PONG
```

### Kafka broker issues
```bash
# Check Kafka logs
docker logs kafka

# Verify broker is ready
docker exec -it kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

## Performance Tuning

### Increase PostgreSQL resources
```yaml
# In docker-compose.yml
postgres-primary:
  deploy:
    resources:
      limits:
        memory: 2G
```

### Increase Redis resources
```yaml
redis:
  command: >
    redis-server 
    --maxmemory 512mb 
    --maxmemory-policy allkeys-lru
```

### Increase Kafka resources
```yaml
kafka:
  environment:
    KAFKA_HEAP_OPTS: "-Xmx1G -Xms1G"
```

## Cleanup

### Remove stopped containers
```bash
docker container prune -f
```

### Remove dangling images
```bash
docker image prune -f
```

### Remove all mini-shop resources
```bash
docker-compose down -v
docker image rm mini-shop-service:latest
```

## Next Steps

1. ✅ Verify all services are healthy: `docker-compose ps`
2. ✅ Check application logs: `docker logs mini-shop-service`
3. ✅ Test API: `curl http://localhost:8081/actuator/health`
4. ✅ Access Swagger UI: http://localhost:8081/swagger-ui.html
5. ✅ Run unit tests: `mvn clean test`

---

**Last Updated**: 2026-02-16
**Status**: Production Ready ✅

