# Maven Commands Reference

## Run Unit Tests Only (Recommended for Development)
```bash
mvn clean test -DskipITs
```
- Runs only `*Test.java` files
- Skips all integration tests (`*IT.java`)
- Fast - no Testcontainers startup
- Time: ~5-10 minutes

## Run All Tests (Unit + Integration)
```bash
mvn clean verify
```
- Runs both unit tests and integration tests
- Starts all Testcontainers (PostgreSQL, MongoDB, Redis, Kafka)
- Comprehensive but slow
- Time: ~15-20 minutes

## Run Integration Tests Only
```bash
mvn clean verify -DskipTests
```
- Skips unit tests, runs only integration tests
- Useful for testing with real containers
- Time: ~10-15 minutes

## Run Specific Test Class
```bash
mvn clean test -Dtest=AuthServiceTest
```

## Run Specific Test Method
```bash
mvn clean test -Dtest=AuthServiceTest#testLogin_Success
```

## Install Without Running Tests
```bash
mvn clean install -DskipTests
```

## Build Docker Image
```bash
mvn clean install -DskipTests docker:build
```

## Run with More Memory (if tests fail due to memory)
```bash
mvn clean test -DskipITs -Xmx2048m
```

---

## About Virtual Threads for This Project

### Current Answer: NO, not needed yet

**Why?**
1. Virtual threads help with **many concurrent I/O operations**, not test performance
2. Your main issue is **Testcontainers startup time**, not threading
3. Kafka listeners are not the bottleneck in tests

**When to Consider Virtual Threads:**
- When you have 10,000+ concurrent connections
- High I/O load on database queries
- Many simultaneous Kafka consumers
- Measured performance issues with thread pools

**Current Setup is Better Suited For:**
1. Proper test structure (unit vs integration)
2. Container resource limits
3. Kafka auto-scaling configuration
4. Connection pooling tuning

**Recommendations:**
1. ✅ Use `mvn clean test -DskipITs` for development
2. ✅ Use `mvn clean verify` only for CI/CD pipeline
3. ✅ Mock Kafka in unit tests (don't start real containers)
4. ✅ Tune your thread pool sizes in `application-test.yml`

---

## Troubleshooting

**If tests timeout (30+ seconds):**
```bash
# Increase JVM memory
mvn clean test -DskipITs -Xmx2048m

# Or skip specific slow tests
mvn clean test -DskipITs -Dskip.slow.tests=true
```

**If containers won't start:**
```bash
# Clean Docker containers
docker-compose down
docker system prune -a

# Then run tests
mvn clean verify
```

