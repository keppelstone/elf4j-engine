# AGENTS.md - elf4j-engine

An asynchronous Java log engine implementing the ELF4J API. Java 21+, Maven build, MIT license.

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
- **Formatter**: Spotless with Palantir Java Format 2.87.0 (Google style). Runs automatically during `process-sources`. Always run `./mvnw spotless:apply` before committing.

### Imports

- Static imports first: `import static org.junit.jupiter.api.Assertions.*`
- Then `java.*`, `javax.*`, external libs, then internal `elf4j.*`
- Use static imports for test assertions and Mockito BDD methods

### Naming Conventions

| Element              | Convention       | Example                              |
|----------------------|------------------|--------------------------------------|
| Classes/Records      | PascalCase       | `NativeLogger`, `LogEvent`           |
| Methods/Variables    | camelCase        | `isEnabled()`, `logHandler`          |
| Constants            | UPPER_SNAKE_CASE | `DEFAULT_THROWABLE_MESSAGE`          |
| Packages             | lowercase        | `elf4j.engine.logging`               |
| Test Classes         | `*Test` suffix   | `NativeLoggerTest`                   |
| Test Methods         | camelCase verb   | `instanceForDifferentLevel()`        |
| Nested Test Classes  | camelCase        | `class atLevels {}`, `class render {}`|

### Null Safety

- Every package has `@NullMarked` (jspecify) in its `package-info.java`
- Use `@Nullable` from `org.jspecify.annotations` (NOT `javax.annotation`)
- All types are non-null by default; annotate nullable params/returns/fields explicitly

### Annotations

- **Lombok**: `@Value` (immutable classes), `@Getter`, `@Builder`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)`, `@ToString`
- **jspecify**: `@NullMarked` (package-level), `@Nullable` (per-element)
- **Thread safety**: `@ThreadSafe` from `javax.annotation.concurrent`
- **JSON**: `@CompiledJson` from DSL-JSON for compile-time serialization
- Lombok and DSL-JSON are annotation-processed at compile time

### Records and Immutability

- Use records for immutable data carriers: `LogEvent`, `ConfigurationProperties`, `LoggerThresholdLevels`, etc.
- Records commonly implement interfaces (e.g., `TimestampPattern implements RenderingPattern`)
- Use Lombok `@Value` for immutable classes that need more than record semantics

### Design Patterns

- **Interface-based design**: `LogHandler`, `LogEventWriter`, `RenderingPattern`, `PerformanceSensitive`
- **Enum singletons**: `NativeLogServiceManager.INSTANCE` for lifecycle management
- **Private constructors** on utility classes: `StackTraces`, `ElementPatterns`

### Error Handling

- Wrap checked exceptions in unchecked: `UncheckedIOException` for IO errors
- Use `IllegalArgumentException` for invalid configuration/inputs
- Use `assert` for internal invariants
- Avoid checked exceptions in public APIs
- Internal logging uses `elf4j.util.UtilLogger` (e.g., `UtilLogger.WARN.log(...)`)

### Thread Safety

- Logger instances are thread-safe (usable as static/instance/local variables)
- Annotate with `@ThreadSafe` from `javax.annotation.concurrent`
- Use `ReentrantLock` instead of `synchronized` (virtual-thread friendly)
- Prefer immutable records and `@Value` classes

### Javadoc

- Use `/** */` for public API documentation; `/* */` for implementation details
- Javadoc formatting is enforced by Spotless

## Testing

- **JUnit 5** (Jupiter) with `@Test`, `@Nested`, `@ParameterizedTest`, `@ValueSource`
- **Mockito** with BDD style: `given()`, `then()`, `willReturn()`, `should()`
- **JUnit assertions** for most tests: `assertEquals`, `assertNotNull`, `assertThrows`, etc.
- **AssertJ** available for fluent assertions
- **Spring `ReflectionTestUtils`** for injecting mocks into Lombok `@Value` classes

### Test Conventions

- Test classes are **package-private** (no `public` modifier)
- Use `@ExtendWith(MockitoExtension.class)` for Mockito integration
- Group related tests with `@Nested` inner classes named after the method under test
- Use `@Mock` for field-level mocks, `mock()` for local/inline mocks
- Mockito uses `mock-maker-inline` (configured in test resources)
- A custom `TestExecutionListener` adds a 2-second delay after tests for async log flushing

### Test Example Pattern

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
    NativeLogger.java                  # Core Logger (implements elf4j API)
    NativeLoggerFactory.java           # SPI LoggerFactory entry point
    logging/
        LogEvent.java                  # Record: log event data
        EventingLogHandler.java        # Async log handler
        NativeLogServiceManager.java   # Enum singleton: lifecycle
        configuration/                 # Config loading & logger thresholds
        pattern/element/               # Log output patterns (timestamp, JSON, etc.)
        writer/                        # stdout/stderr writers
        util/                          # Stack trace utilities
src/test/java/elf4j/engine/           # Mirrors main structure
src/test/resources/
    elf4j-test.properties             # Test log configuration
```

## Key Dependencies

| Dependency  | Purpose                                      |
|-------------|----------------------------------------------|
| elf4j       | ELF4J logging API (this engine implements it) |
| Lombok      | `@Value`, `@Builder`, `@Getter`, code gen     |
| jspecify    | `@NullMarked`, `@Nullable` null safety        |
| DSL-JSON    | Compile-time JSON serialization (`@CompiledJson`) |
| conseq4j    | Sequenced concurrent async execution          |
| Guava       | General utilities                             |
| SLF4J       | MDC adapter integration                       |
| JUnit 5     | Test framework                                |
| Mockito     | Mocking (BDD style)                           |
