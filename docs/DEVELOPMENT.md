# Development Guide

A comprehensive guide for developers working on the Proje Pazarı Backend.

## Prerequisites

- Java 21 or higher
- Docker & Docker Compose
- Git
- IDE (IntelliJ IDEA recommended)

---

## IDE Setup

### IntelliJ IDEA (Recommended)

1. **Install Plugins:**
   - Lombok (for annotation processing)
   - MapStruct Support (optional, for mapper navigation)

2. **Enable Annotation Processing:**
   - Go to `Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
   - Check "Enable annotation processing"

3. **Import Project:**
   - Open IntelliJ → `File → Open`
   - Select the project directory
   - Choose "Import as Gradle Project"

4. **Configure JDK:**
   - Go to `File → Project Structure → Project`
   - Set SDK to Java 21

### VS Code

1. Install extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. Open the project folder

---

## Code Style

This project uses **Google Java Format** with AOSP style (4-space indentation).

### Format Code

```bash
# Auto-format all code
./gradlew spotlessApply

# Check formatting without changing files
./gradlew spotlessCheck
```

### IntelliJ Configuration

1. Install "google-java-format" plugin
2. Go to `Settings → google-java-format Settings`
3. Enable the plugin
4. Set style to "AOSP style"

### Key Style Rules

- 4-space indentation (AOSP style)
- 100 character line limit
- K&R brace style
- No wildcard imports

---

## Project Structure

```
src/
├── main/
│   ├── java/com/iyte_yazilim/proje_pazari/
│   │   ├── ProjePazariApplication.java    # Main entry point
│   │   │
│   │   ├── presentation/                   # Controllers & Security
│   │   │   ├── controllers/               # REST endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ProjectController.java
│   │   │   │   └── ...
│   │   │   ├── security/                  # JWT & Security
│   │   │   │   ├── JwtUtil.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── config/                    # Configuration
│   │   │
│   │   ├── application/                    # Use Cases (CQRS)
│   │   │   ├── commands/                  # Write operations
│   │   │   │   ├── createProject/
│   │   │   │   │   ├── CreateProjectCommand.java
│   │   │   │   │   └── CreateProjectHandler.java
│   │   │   │   └── ...
│   │   │   ├── queries/                   # Read operations
│   │   │   ├── dtos/                      # Data Transfer Objects
│   │   │   └── mappers/                   # DTO mappers
│   │   │
│   │   ├── domain/                         # Business Logic
│   │   │   ├── entities/                  # Domain entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Project.java
│   │   │   │   └── ProjectApplication.java
│   │   │   ├── interfaces/                # Repository contracts
│   │   │   ├── enums/                     # Enumerations
│   │   │   ├── events/                    # Domain events
│   │   │   └── models/                    # ApiResponse, etc.
│   │   │
│   │   └── infrastructure/                 # External Implementations
│   │       └── persistence/
│   │           ├── repositories/          # JPA repositories
│   │           ├── models/                # JPA entities
│   │           ├── mappers/               # Entity mappers
│   │           └── converters/            # Type converters
│   │
│   └── resources/
│       ├── application.properties          # Configuration
│       └── templates/                      # Email templates (future)
│
└── test/
    └── java/                               # Unit & Integration tests
```

---

## Adding a New Feature

Follow these steps to add a new feature:

### 1. Define Domain Entity (if needed)

```java
// domain/entities/NewEntity.java
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewEntity extends BaseEntity<Ulid> {
    private String name;
    private String description;
}
```

### 2. Create Command/Query

```java
// application/commands/createEntity/CreateEntityCommand.java
public record CreateEntityCommand(
    @NotBlank String name,
    @Size(max = 500) String description
) {}
```

### 3. Implement Handler

```java
// application/commands/createEntity/CreateEntityHandler.java
@Service
@RequiredArgsConstructor
public class CreateEntityHandler 
    implements IRequestHandler<CreateEntityCommand, ApiResponse<CreateEntityResult>> {

    private final EntityRepository repository;

    @Override
    @Transactional
    public ApiResponse<CreateEntityResult> handle(CreateEntityCommand command) {
        // Business logic here
    }
}
```

### 4. Create Controller Endpoint

```java
// presentation/controllers/EntityController.java
@RestController
@RequestMapping("/api/v1/entities")
@Tag(name = "Entities", description = "Entity management APIs")
public class EntityController {

    @PostMapping
    @Operation(summary = "Create a new entity")
    public ResponseEntity<ApiResponse<CreateEntityResult>> create(
        @Valid @RequestBody CreateEntityCommand command
    ) {
        // Implementation
    }
}
```

### 5. Add Persistence (if needed)

```java
// infrastructure/persistence/models/EntityModel.java
@Entity
@Table(name = "entities")
public class EntityModel extends AuditableEntity {
    private String name;
    private String description;
}

// infrastructure/persistence/repositories/EntityRepository.java
public interface EntityRepository extends JpaRepository<EntityModel, String> {
}
```

### 6. Write Tests

See [Testing](#testing) section below.

---

## Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test

```bash
./gradlew test --tests "*.CreateProjectHandlerTest"
```

### Test Coverage

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html
```

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class CreateProjectHandlerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CreateProjectHandler handler;

    @Test
    @DisplayName("Should create project when valid command provided")
    void shouldCreateProject_whenValidCommand() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand(
            "Test Project",
            "Description",
            "Summary",
            5,
            List.of("Java", "Spring"),
            "Web Development",
            LocalDateTime.now().plusMonths(3)
        );

        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        ApiResponse<CreateProjectResult> response = handler.handle(command);

        // Then
        assertNotNull(response);
        assertEquals(ResponseCode.CREATED, response.getCode());
        verify(projectRepository, times(1)).save(any());
    }
}
```

### Integration Test Example

```java
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterUser() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "test@std.iyte.edu.tr",
                        "password": "SecurePass123!",
                        "firstName": "John",
                        "lastName": "Doe"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"));
    }
}
```

---

## Debugging

### Enable Debug Logging

Add to `application.properties`:

```properties
# Application debug
logging.level.com.iyte_yazilim.proje_pazari=DEBUG

# Security debug
logging.level.org.springframework.security=DEBUG

# SQL debug
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### IntelliJ Debug

1. Set breakpoints by clicking on the line number
2. Run debug configuration (Shift + F9)
3. Use debug console for inspection

---

## Common Tasks

### Database Schema Update

Schema is auto-updated in development. For manual changes:

```bash
# Connect to database
docker exec -it proje-pazari-db psql -U yazilim -d proje_pazari_db

# Run SQL
ALTER TABLE users ADD COLUMN new_field VARCHAR(100);
```

### Add New Dependency

Edit `build.gradle`:

```gradle
dependencies {
    implementation 'new.dependency:name:version'
}
```

Then run:
```bash
./gradlew build --refresh-dependencies
```

### Clear Build Cache

```bash
./gradlew clean
rm -rf .gradle
./gradlew build
```

---

## Makefile Commands

```bash
make help          # Show all commands
make run           # Run application
make test          # Run tests
make coverage      # Generate coverage report
make format        # Format code
make lint          # Check formatting
make quality       # Run all quality checks
```

---

## Git Workflow

1. **Create feature branch:**
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feature/issue-X-description
   ```

2. **Make changes and commit:**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

3. **Push and create PR:**
   ```bash
   git push origin feature/issue-X-description
   ```

See [CONTRIBUTING.md](../CONTRIBUTING.md) for detailed guidelines.
