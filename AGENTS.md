# AGENTS.md - elf4j-engine

Asynchronous Java log engine implementing the ELF4J API. Java 21+, Maven build, MIT license.

## Build Commands

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=NativeLoggerTest

# Run a single test method
./mvnw test -Dtest=NativeLoggerTest#instanceForDifferentLevel

# Run tests matching a pattern
./mvnw test -Dtest="*Test"

# Auto-format code (ALWAYS run before committing)
./mvnw spotless:apply

# Check formatting without applying
./mvnw spotless:check

# Full build with all checks (CI runs this)
./mvnw clean verify
```

## Code Style

- **Java 21** - uses records, pattern matching in `instanceof`, switch expressions, `String.formatted()`
- **Formatter**: Spotless with Palantir Java Format 2.87.0 (Google style). Always run `./mvnw spotless:apply` before committing.

### Imports

Static imports first: `import static org.junit.jupiter.api.Assertions.*`. Then `java.*`, `javax.*`, external libs, then internal `elf4j.*`.

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes/Records | PascalCase | `NativeLogger`, `LogEvent` |
| Methods/Variables | camelCase | `isEnabled()`, `logHandler` |
| Constants | UPPER_SNAKE_CASE | `DEFAULT_MESSAGE` |
| Packages | lowercase | `elf4j.engine.logging` |
| Test Classes | `*Test` suffix | `NativeLoggerTest` |
| Test Methods | camelCase verb | `instanceForDifferentLevel()` |

### Null Safety

Every package has `@NullMarked` in `package-info.java`. Use `@Nullable` from `org.jspecify.annotations` (NOT `javax.annotation`).

### Annotations

- **Lombok**: `@Value`, `@Getter`, `@Builder`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`
- **jspecify**: `@NullMarked` (package-level), `@Nullable` (per-element)
- **Thread safety**: `@ThreadSafe` from `javax.annotation.concurrent`
- **JSON**: `@CompiledJson` from DSL-JSON

### Records & Immutability

Use records for immutable data carriers. Use Lombok `@Value` for immutable classes needing more than record semantics.

### Design Patterns

- Interface-based: `LogHandler`, `LogEventWriter`, `RenderingPattern`
- Enum singletons: `NativeLogServiceManager.INSTANCE`
- Private constructors on utility classes

### Error Handling

Wrap checked exceptions in unchecked (`UncheckedIOException`). Use `IllegalArgumentException` for invalid config/inputs. Use `assert` for internal invariants. Internal logging uses `elf4j.util.UtilLogger`.

### Thread Safety

Logger instances are thread-safe. Use `@ThreadSafe` annotation. Use `ReentrantLock` instead of `synchronized`.

## Testing

- **JUnit 5**: `@Test`, `@Nested`, `@ParameterizedTest`, `@ValueSource`
- **Mockito**: BDD style with `given()`, `then()`, `willReturn()`, `should()`
- **AssertJ**: Available for fluent assertions
- **Spring `ReflectionTestUtils`**: For injecting mocks into Lombok `@Value` classes

### Test Conventions

- Test classes are **package-private** (no `public`)
- Use `@ExtendWith(MockitoExtension.class)`
- Group tests with `@Nested` classes named after the method under test
- Use `@Mock` for field mocks, `mock()` for inline mocks
- Tests have a 2-second delay after completion for async log flushing

```java
@ExtendWith(MockitoExtension.class)
class FooTest {
    @Mock SomeDependency dep;

    @Nested
    class methodName {
        @Test
        void behaviorDescription() {
            given(dep.call()).willReturn(value);
            // act
            then(dep).should().verify(args);
        }
    }
}
```

## Project Structure

```
src/main/java/elf4j/engine/
    NativeLogger.java              # Core Logger (implements elf4j API)
    NativeLoggerFactory.java       # SPI LoggerFactory entry point
    logging/
        LogEvent.java              # Record: log event data
        EventingLogHandler.java    # Async log handler
        NativeLogServiceManager.java  # Enum singleton: lifecycle
        configuration/             # Config loading & logger thresholds
        pattern/element/          # Log output patterns (timestamp, JSON)
        writer/                    # stdout/stderr writers
        util/                      # Stack trace utilities
src/test/java/elf4j/engine/        # Mirrors main structure
src/test/resources/
    elf4j-test.properties         # Test log configuration
```

## Key Dependencies

| Dependency | Purpose |
|------------|---------|
| elf4j | ELF4J logging API (this engine implements) |
| Lombok | `@Value`, `@Builder`, `@Getter`, code gen |
| jspecify | `@NullMarked`, `@Nullable` null safety |
| DSL-JSON | Compile-time JSON (`@CompiledJson`) |
| conseq4j | Sequenced concurrent async execution |
| JUnit 5 | Test framework |
| Mockito | Mocking (BDD style) |
