# AGENTS.md - elf4j-engine

## Build Commands

```bash
# Build the project
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=NativeLoggerTest

# Run a single test method
./mvnw test -Dtest=NativeLoggerTest#instanceForDifferentLevel

# Run tests matching a pattern
./mvnw test -Dtest="*Test"

# Run with verbose output
./mvnw test -X

# Run spotless formatting (auto-formats code)
./mvnw spotless:apply

# Check formatting without applying
./mvnw spotless:check

# Full build with all checks
./mvnw clean verify
```

## Code Style Guidelines

### General

- **Language Level**: Java 21
- **Formatter**: Spotless with Palantir Java Format (Google style)
- **Build Tool**: Maven (use `./mvnw` wrapper)

### Formatting (enforced by Spotless)

- Use Google Java Format style via Palantir
- Run `./mvnw spotless:apply` before committing
- Remove unused imports automatically
- Format annotations
- Format Javadoc

### Imports

- Use static imports for test assertions: `import static org.junit.jupiter.api.Assertions.*`
- Use static imports for Mockito: `import static org.mockito.BDDMockito.*`
- Group imports: static, java., javax., external libs, internal

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes/Records | PascalCase | `NativeLogger`, `LogEvent` |
| Methods/Variables | camelCase | `isEnabled()`, `logHandler` |
| Test Classes | *Test suffix | `NativeLoggerTest` |
| Test Methods | lowerCamelCase | `instanceForDifferentLevel()` |
| Nested Test Classes | lowerCamelCase | `class atLevels {}` |
| Constants | UPPER_SNAKE_CASE | `DEFAULT_THROWABLE_MESSAGE` |
| Packages | lowercase | `elf4j.engine.logging` |

### Records

- Use records for immutable data carriers
- Place records in the same file as their primary usage when small
- Example:
  ```java
  public record LogEvent(
      Instant timestamp,
      String loggerName,
      Level level,
      @Nullable Throwable throwable) {}
  ```

### Annotations

- Use Lombok annotations: `@Getter`, `@Setter`, `@Builder`, `@EqualsAndHashCode`
- Use jspecify for nullability: `@Nullable`, `@NonNull` (not javax.annotation)
- Mark thread-safe classes with `@ThreadSafe`

### Javadoc vs JSDoc

- Use JSDoc-style `/** */` for public API documentation
- Use block comments `/* */` for implementation details
- Format Javadoc is enabled in Spotless

### Error Handling

- Avoid checked exceptions where possible
- Use specific exception types
- Document unchecked exceptions in JSDoc with `@throws`

### Testing

- Use JUnit 5 (Jupiter)
- Use nested test classes with `@Nested` for grouping related tests
- Use `@ExtendWith(MockitoExtension.class)` for Mockito
- Use BDD-style Mockito: `given()`, `then()`
- Use AssertJ for fluent assertions
- Test class naming: `<ClassName>Test.java`
- Test method naming: `<behavior>When<condition>` or `<action>`

### Project Structure

```
src/
├── main/java/
│   └── elf4j/
│       └── engine/
│           ├── logging/
│           │   ├── pattern/element/
│           │   └── writer/
│           └── ...
└── test/java/
    └── elf4j/
        └── engine/
            └── ...
```

### Key Dependencies

- **Lombok**: Code generation (getters, builders, etc.)
- **jspecify**: Nullness annotations
- **DSL-JSON**: JSON serialization
- **Mockito**: Mocking
- **AssertJ**: Fluent assertions
- **JUnit 5**: Testing framework
- **Guava**: Utilities

### Thread Safety

- Logger instances are thread-safe and can be used as static/instance/local variables
- Use `@ThreadSafe` annotation on thread-safe classes
- Prefer immutable designs where possible
