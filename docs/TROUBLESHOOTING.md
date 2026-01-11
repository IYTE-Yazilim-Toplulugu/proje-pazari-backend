# Troubleshooting Guide

Common issues and their solutions for the Proje Pazarı Backend.

---

## Application Startup Issues

### Port Already in Use

**Error:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solutions:**

1. Find and kill the process:
   ```bash
   # Windows
   netstat -ano | findstr :8080
   taskkill /PID <PID> /F

   # Linux/macOS
   lsof -i :8080
   kill -9 <PID>
   ```

2. Change the port in `application.properties`:
   ```properties
   server.port=8081
   ```

---

### Java Version Mismatch

**Error:**
```
UnsupportedClassVersionError: class was compiled with Java version 65
```

**Solution:**

Ensure Java 21 is installed and configured:

```bash
# Check Java version
java -version

# Should output: openjdk version "21.x.x"
```

Set JAVA_HOME:
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Linux/macOS
export JAVA_HOME=/usr/lib/jvm/java-21
```

---

## Database Connection Issues

### Cannot Connect to PostgreSQL

**Error:**
```
Unable to acquire JDBC Connection
org.postgresql.util.PSQLException: Connection refused
```

**Solutions:**

1. Verify PostgreSQL is running:
   ```bash
   docker-compose ps
   # Should show postgres as "running"
   ```

2. Start the database:
   ```bash
   docker-compose up -d postgres
   ```

3. Check connection settings in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/proje_pazari_db
   spring.datasource.username=yazilim
   spring.datasource.password=yazilim123
   ```

4. If using Docker, ensure network connectivity:
   ```bash
   docker network ls
   docker network inspect proje-pazari-network
   ```

---

### Authentication Failed for User

**Error:**
```
FATAL: password authentication failed for user "yazilim"
```

**Solutions:**

1. Verify credentials match `docker-compose.yml`:
   ```yaml
   environment:
     POSTGRES_USER: yazilim
     POSTGRES_PASSWORD: yazilim123
   ```

2. Reset the database:
   ```bash
   docker-compose down -v
   docker-compose up -d postgres
   ```

---

### Database Does Not Exist

**Error:**
```
FATAL: database "proje_pazari_db" does not exist
```

**Solution:**

The database is created automatically by Docker. Restart:
```bash
docker-compose down
docker-compose up -d postgres
```

---

## JWT Token Issues

### Invalid JWT Token

**Error:**
```json
{
  "code": "UNAUTHORIZED",
  "message": "Invalid or expired token"
}
```

**Causes & Solutions:**

1. **Token expired**: Login again to get a new token

2. **Wrong secret**: Ensure `JWT_SECRET` is consistent
   ```bash
   # Check environment variable
   echo $JWT_SECRET
   ```

3. **Malformed token**: Verify the token format
   - Must be 3 parts separated by dots
   - Include full token without "Bearer " prefix in testing

---

### Token Not Recognized

**Error:**
```
401 Unauthorized
```

**Solutions:**

1. Check Authorization header format:
   ```http
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   ```

2. Ensure no extra spaces or newlines in the token

3. Verify token hasn't expired

---

## Build Issues

### Gradle Build Fails

**Error:**
```
> Task :compileJava FAILED
```

**Solutions:**

1. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

2. Clear Gradle cache:
   ```bash
   rm -rf .gradle
   rm -rf build
   ./gradlew build
   ```

3. Refresh dependencies:
   ```bash
   ./gradlew build --refresh-dependencies
   ```

---

### MapStruct Not Generating Mappers

**Error:**
```
Cannot find implementation for mapper
```

**Solutions:**

1. Rebuild the project:
   ```bash
   ./gradlew clean build
   ```

2. In IntelliJ:
   - `Build → Rebuild Project`
   - Ensure annotation processing is enabled

3. Check `build.gradle` has correct order:
   ```gradle
   annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
   annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
   annotationProcessor 'org.projectlombok:lombok'
   ```

---

### Lombok Annotations Not Working

**Error:**
```
cannot find symbol: method getEmail()
```

**Solutions:**

1. Install Lombok plugin in IDE

2. Enable annotation processing:
   - IntelliJ: `Settings → Build → Compiler → Annotation Processors → Enable`

3. Rebuild project

---

## Testing Issues

### Tests Fail with Database Errors

**Error:**
```
Failed to load ApplicationContext
DataSource URL must be set
```

**Solution:**

Ensure test configuration uses H2:
```properties
# src/test/resources/application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

---

### Security Tests Failing

**Error:**
```
401 Unauthorized in tests
```

**Solution:**

Use `@WithMockUser` for authenticated tests:
```java
@Test
@WithMockUser(username = "test@example.com")
void shouldAccessProtectedEndpoint() {
    // test code
}
```

---

## Docker Issues

### Docker Compose Not Starting

**Error:**
```
ERROR: Couldn't connect to Docker daemon
```

**Solutions:**

1. Start Docker Desktop

2. On Linux, start Docker service:
   ```bash
   sudo systemctl start docker
   ```

3. Add user to docker group:
   ```bash
   sudo usermod -aG docker $USER
   ```

---

### Container Exits Immediately

**Solution:**

Check container logs:
```bash
docker-compose logs app
docker-compose logs postgres
```

Common causes:
- Database not ready (wait for healthcheck)
- Missing environment variables
- Port conflicts

---

## API Issues

### CORS Errors

**Error:**
```
Access to XMLHttpRequest blocked by CORS policy
```

**Solution:**

Configure CORS in `SecurityConfig`:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    // ...
}
```

---

### Validation Errors

**Error:**
```json
{
  "code": "BAD_REQUEST",
  "message": "Validation failed"
}
```

**Solution:**

Check request body matches expected format. Common issues:
- Missing required fields
- Invalid email format
- Password too short

---

## Performance Issues

### Slow Startup

**Solutions:**

1. Disable DevTools in production
2. Use lazy initialization:
   ```properties
   spring.main.lazy-initialization=true
   ```
3. Reduce logging level

---

### Slow Database Queries

**Solutions:**

1. Enable query logging to identify slow queries:
   ```properties
   logging.level.org.hibernate.SQL=DEBUG
   ```

2. Add appropriate indexes

3. Check for N+1 query problems

---

## Getting Help

If you can't resolve an issue:

1. Check existing [GitHub Issues](https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend/issues)
2. Search project documentation
3. Ask in team chat
4. Open a new issue with:
   - Error message (full stack trace)
   - Steps to reproduce
   - Environment details (OS, Java version)
   - What you've already tried
