# Contributing to IYTE Proje Pazarı Backend

Thank you for contributing to IYTE Proje Pazarı! This document provides guidelines for contributing to the backend project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)

## 🤝 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them learn
- Focus on constructive feedback
- Collaborate openly and transparently
- Follow the team's communication guidelines

## 🚀 Getting Started

### 1. Fork and Clone (if external contributor)

For team members, directly clone the repository:

```bash
git clone https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend.git
cd proje-pazari-backend
```

### 2. Set Up Development Environment

Follow the [README.md](README.md) for setup instructions.

### 3. Find an Issue

- Check the [Project Board](https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23)
- Look for issues assigned to you or marked as "Ready"
- Comment on the issue to let others know you're working on it

## 💻 Development Workflow

### Branch Naming Convention

Use the format: `feature/issue-{number}-{short-description}`

Examples:
- `feature/issue-10-jwt-auth-enhancement`
- `feature/issue-12-project-crud-commands`
- `fix/issue-25-null-pointer-fix`

### Workflow Steps

1. **Create or checkout branch**:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout -b feature/issue-X-description
   ```

2. **Make changes** following coding standards

3. **Test your changes**:
   ```bash
   ./gradlew test
   ```

4. **Commit** with conventional commit messages

5. **Push** to remote:
   ```bash
   git push origin feature/issue-X-description
   ```

6. **Create Pull Request** on GitHub

## 🎨 Coding Standards

### Java Style Guide

Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html):

- **Indentation**: 2 spaces
- **Line length**: 100 characters max
- **Braces**: K&R style
- **Naming**:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

### Clean Architecture Principles

- **Separation of concerns**: Keep layers independent
- **Dependency rule**: Dependencies point inward
- **Domain layer**: No external dependencies
- **Use cases**: One class per use case

### Example Code Structure

```java
// Domain Entity (domain/entities/)
public class User extends BaseEntity<String> {
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    // Business logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

// Command (application/commands/)
public record RegisterUserCommand(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String firstName,
    @NotBlank String lastName
) {}

// Handler (application/handlers/)
@Service
public class RegisterUserHandler implements CommandHandler<RegisterUserCommand, RegisterUserResult> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterUserResult handle(RegisterUserCommand command) {
        // Implementation
    }
}

// Controller (presentation/controllers/)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserHandler registerUserHandler;

    @PostMapping("/register")
    public ApiResponse<RegisterUserResult> register(
        @RequestBody @Valid RegisterUserCommand command
    ) {
        RegisterUserResult result = registerUserHandler.handle(command);
        return ApiResponse.success(result);
    }
}
```

### Error Handling

- Use custom exceptions for domain errors
- Return `ApiResponse<T>` from controllers
- Include meaningful error messages
- Log errors appropriately

```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
    }
}
```

### Security Best Practices

- Never log sensitive data (passwords, tokens)
- Use `@Transactional` for database operations
- Validate all inputs with Jakarta Validation
- Use `@PreAuthorize` for authorization checks
- Encode passwords with BCrypt
- Sanitize error messages sent to clients

## 📝 Commit Guidelines

### Conventional Commits

We follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer]
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements

### Examples

```bash
# Feature
git commit -m "feat: add user registration endpoint"

# Bug fix
git commit -m "fix: resolve null pointer in project query"

# With scope
git commit -m "feat(auth): implement JWT refresh token"

# Breaking change
git commit -m "feat!: change user ID from UUID to ULID

BREAKING CHANGE: User IDs are now ULIDs instead of UUIDs"
```

### Commit Message Rules

- Use present tense ("add" not "added")
- Use imperative mood ("move cursor to" not "moves cursor to")
- Keep first line under 72 characters
- Reference issue numbers when applicable
- Provide context in the body for complex changes

## 🔄 Pull Request Process

### Before Creating a PR

1. **Ensure tests pass**:
   ```bash
   ./gradlew test
   ```

2. **Code compiles without warnings**:
   ```bash
   ./gradlew build
   ```

3. **Code is formatted** according to style guide

4. **Branch is up to date with dev**:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout feature/issue-X-description
   git rebase dev
   ```

### Creating a Pull Request

1. **Push your branch** to GitHub

2. **Open Pull Request** with this template:

```markdown
## Description
Brief description of changes

Closes #X

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Changes Made
- Added user authentication endpoint
- Implemented JWT token generation
- Added password encryption with BCrypt

## Testing
- [ ] All existing tests pass
- [ ] Added new tests for new functionality
- [ ] Manually tested the feature

## Screenshots (if applicable)
N/A for backend

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests added/updated
```

3. **Request Reviews** from at least 2 team members

4. **Link to Issue**: Use `Closes #X` in the description

### PR Review Process

- **Reviewer responsibilities**:
  - Check code quality and style
  - Verify tests are adequate
  - Test functionality locally if needed
  - Provide constructive feedback
  - Approve only if ready to merge

- **Author responsibilities**:
  - Address all review comments
  - Keep PR up to date with dev
  - Respond to feedback promptly
  - Mark conversations as resolved

### Merging

- **Merge only when**:
  - All tests pass
  - At least 2 approvals received
  - All review comments addressed
  - No merge conflicts
  - Branch is up to date with dev

- **Merge strategy**: Squash and merge (preferred) or Merge commit

## 🧪 Testing Guidelines

### Test Structure

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
    @DisplayName("Should register user when valid command provided")
    void shouldRegisterUser_whenValidCommand() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            "student@std.iyte.edu.tr",
            "SecurePass123!",
            "John",
            "Doe"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");

        // When
        RegisterUserResult result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("student@std.iyte.edu.tr", result.email());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowException_whenEmailExists() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            "existing@std.iyte.edu.tr",
            "SecurePass123!",
            "John",
            "Doe"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> handler.handle(command));
    }
}
```

### Test Coverage

- Aim for **80%+ code coverage**
- Focus on critical business logic
- Test happy paths and edge cases
- Test error handling

### Integration Tests

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
                  "email": "student@std.iyte.edu.tr",
                  "password": "SecurePass123!",
                  "firstName": "John",
                  "lastName": "Doe"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

## 🐛 Reporting Bugs

### Bug Report Template

```markdown
**Describe the bug**
A clear description of the bug.

**To Reproduce**
Steps to reproduce:
1. Call endpoint '...'
2. With payload '...'
3. See error

**Expected behavior**
What should happen instead.

**Actual behavior**
What actually happens.

**Environment**
- Java version: 21
- Spring Boot version: 4.0.0
- Database: PostgreSQL 16

**Additional context**
Stack trace, logs, screenshots if applicable.
```

## 💡 Feature Requests

Before requesting a feature:
1. Check if it already exists in issues
2. Discuss with the team
3. Create a detailed proposal with use cases

## 📞 Questions?

- Open a discussion on GitHub
- Ask in team chat
- Contact: yazilim@iyte.edu.tr

## 📜 License

By contributing, you agree that your contributions will be part of this educational project.

---

Thank you for contributing! 🎉
