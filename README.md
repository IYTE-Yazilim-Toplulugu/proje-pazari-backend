# 🎓 IYTE Proje Pazarı - Backend

A Spring Boot backend application for IYTE Project Marketplace, where students collaborate on projects.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Contributing](#contributing)

## 🛠 Tech Stack

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Build Tool**: Gradle
- **Authentication**: JWT (JJWT 0.12.6)
- **Validation**: Jakarta Bean Validation
- **ORM**: Spring Data JPA / Hibernate
- **Documentation**: Swagger/OpenAPI 3.0
- **ID Generation**: ULID
- **Mapping**: MapStruct 1.5.5
- **Code Quality**: Spotless (Google Java Format), JaCoCo (Coverage)

## ⚙️ Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/get-started))
- **Gradle 8.x** (or use the wrapper `./gradlew`)
- **Git** ([Download](https://git-scm.com/))

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend.git
cd proje-pazari-backend
```

### 2. Set Up Environment Variables

Create a `.env` file in the root directory (optional, for production):

```env
JWT_SECRET=your-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/proje_pazari_db
SPRING_DATASOURCE_USERNAME=yazilim
SPRING_DATASOURCE_PASSWORD=yazilim123
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password
```

### 3. Start PostgreSQL with Docker

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- PgAdmin on port 5050 (http://localhost:5050)

### 4. Run the Application

Using Gradle wrapper:

```bash
./gradlew bootRun
```

Or if you have Gradle installed:

```bash
gradle bootRun
```

The application will start on **http://localhost:8080**

### 5. Access API Documentation

Open your browser and navigate to:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs

## 📁 Project Structure

```
src/main/java/com/iyte_yazilim/proje_pazari/
├── presentation/          # Controllers, Security, Configuration
│   ├── controllers/       # REST API endpoints
│   ├── security/          # JWT, filters, security config
│   └── config/            # Application configuration
├── application/           # Application layer (CQRS)
│   ├── commands/          # Write operations
│   ├── queries/           # Read operations
│   ├── handlers/          # Command/Query handlers
│   ├── dtos/              # Data Transfer Objects
│   └── mappers/           # DTO <-> Domain mapping
├── domain/                # Domain layer (Business logic)
│   ├── entities/          # Domain entities
│   ├── interfaces/        # Repository interfaces
│   ├── events/            # Domain events
│   └── enums/             # Domain enumerations
└── infrastructure/        # Infrastructure layer
    └── persistence/       # Database implementation
        ├── entities/      # JPA entities
        ├── repositories/  # JPA repositories
        └── mappers/       # Entity <-> Domain mapping
```

### Architecture Pattern

This project follows **Clean Architecture** with **CQRS** (Command Query Responsibility Segregation):

- **Commands**: Handle write operations (Create, Update, Delete)
- **Queries**: Handle read operations (Get, List, Search)
- **Handlers**: Process commands and queries
- **Mappers**: Transform data between layers

## 👥 Development Workflow

### Working on an Issue

1. **Find your assigned issue** on the [Project Board](https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23)

2. **Checkout the feature branch**:
   ```bash
   git fetch origin
   git checkout feature/issue-X-task-name
   ```

3. **Make your changes** following the coding standards

4. **Run tests**:
   ```bash
   ./gradlew test
   ```

5. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add user authentication endpoint"
   ```

   Follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `docs:` - Documentation
   - `refactor:` - Code refactoring
   - `test:` - Adding tests
   - `chore:` - Maintenance tasks

6. **Push to remote**:
   ```bash
   git push origin feature/issue-X-task-name
   ```

7. **Create a Pull Request**:
   - Base branch: `dev`
   - Compare branch: `feature/issue-X-task-name`
   - Link to the issue: `Closes #X`
   - Request reviews from team members

### Branch Strategy

- `main` - Production-ready code
- `dev` - Development branch (default)
- `feature/issue-X-*` - Feature branches for each issue

## 📚 API Documentation

### Authentication Endpoints

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/refresh
GET  /api/v1/auth/verify-email?token={token}
POST /api/v1/auth/resend-verification
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### User Endpoints

```http
GET    /api/v1/users/{userId}
GET    /api/v1/users
GET    /api/v1/users/me
PUT    /api/v1/users/me
POST   /api/v1/users/me/profile-picture
PUT    /api/v1/users/me/password
DELETE /api/v1/users/me
```

### Project Endpoints

```http
GET    /api/v1/projects
GET    /api/v1/projects/{id}
POST   /api/v1/projects
PUT    /api/v1/projects/{id}
DELETE /api/v1/projects/{id}
PATCH  /api/v1/projects/{id}/status
GET    /api/v1/search/projects?q={keyword}
```

### Application Endpoints

```http
POST   /api/v1/projects/{id}/applications
GET    /api/v1/projects/{id}/applications
PUT    /api/v1/applications/{id}/review
DELETE /api/v1/applications/{id}
GET    /api/v1/users/me/applications
```

For detailed API documentation, visit the **Swagger UI** when the app is running.

## 📖 Javadoc Documentation

Generate API documentation:

```bash
./gradlew javadoc
```

View documentation at: `build/docs/javadoc/index.html`

### Generate Javadoc JAR

```bash
./gradlew javadocJar
```

The JAR file will be created at: `build/libs/proje-pazari-0.0.1-SNAPSHOT-javadoc.jar`

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
# or
make test
```

### Run Tests with Coverage

```bash
./gradlew test jacocoTestReport
# or
make coverage
```

View coverage report: `build/reports/jacoco/test/html/index.html`

### Verify Coverage Threshold

```bash
./gradlew jacocoTestCoverageVerification
# or
make coverage-verify
```

### Run Specific Test Class

```bash
./gradlew test --tests "com.iyte_yazilim.proje_pazari.application.handlers.RegisterUserHandlerTest"
```

### Writing Tests

Example test structure:

```java
@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserHandler handler;

    @Test
    void shouldRegisterUser_whenValidCommand() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            "student@std.iyte.edu.tr",
            "SecurePass123!",
            "John",
            "Doe"
        );

        // When
        RegisterUserResult result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("student@std.iyte.edu.tr", result.email());
    }
}
```

## 🎨 Coding Standards

### Java Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write self-documenting code
- Add comments only where logic isn't self-evident

### Code Formatting

Format your code with Google Java Format:

```bash
# Auto-format all code
make format
# or
./gradlew spotlessApply

# Check formatting without changing files
make format-check
# or
./gradlew spotlessCheck
```

### Code Quality

Run all quality checks before committing:

```bash
# Run format check, tests, and coverage
make quality

# Individual checks
make lint           # Check code formatting
make test           # Run all tests
make coverage       # Generate coverage report
make coverage-verify # Verify coverage meets threshold (70%)
```

### Database

- Use ULID for IDs (sortable UUIDs)
- Always use `@Transactional` for write operations
- Use Flyway or Liquibase for migrations (recommended for production)

### Security

- Never log sensitive data (passwords, tokens)
- Validate all user inputs
- Use parameterized queries (JPA does this automatically)
- Follow OWASP Top 10 guidelines

## 🐛 Common Issues

### Port Already in Use

If port 8080 is already in use:

```bash
# Find the process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

Or change the port in `application.properties`:

```properties
server.port=8081
```

### Database Connection Issues

1. Ensure PostgreSQL is running: `docker-compose ps`
2. Check database credentials in `application.properties`
3. Restart the database: `docker-compose restart postgres`

### Build Issues

Clean and rebuild:

```bash
./gradlew clean build
```

## 📖 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

## 🤝 Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📝 License

This project is developed by IYTE Yazılım Topluluğu for educational purposes.

## 👨‍💻 Team

- **Web Team**: DrHalley, UlasGokkaya, neonid0, Xerkara, AliKemalMiloglu, bdurgut06, ygt-ernsy, ErkanArikan

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Contact: yazilim@iyte.edu.tr
- Project Board: https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23

---

Made with ❤️ by IYTE Yazılım Topluluğu
