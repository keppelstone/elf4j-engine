/*
 * MIT License
 *
 * Copyright (c) 2023 Qingtian Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.engine;

import elf4j.Level;
import elf4j.Logger;
import elf4j.engine.logging.LogHandlerFactory;
import javax.annotation.concurrent.ThreadSafe;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A thread-safe, immutable logger implementation. Once configured, NativeLogger instances can be
 * safely shared across threads and used as static, instance, or local variables.
 *
 * <p>Obtaining a logger via {@link Logger#instance()} is relatively expensive and is best suited
 * for static variables. For local variables or inline usage, prefer the instance factory methods
 * like {@link NativeLogger#atLevel(Level)} or {@link Logger#atDebug()}, which are more performant.
 */
@ThreadSafe
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NativeLogger implements Logger {
  static final String DEFAULT_THROWABLE_MESSAGE = "";

  @Getter
  @EqualsAndHashCode.Include
  private final LoggerId loggerId;

  private final LogHandlerFactory logHandlerFactory;

  /**
   * Creates a new NativeLogger for the specified logger ID and handler factory.
   *
   * @param loggerId the logger identifier used for configuration lookup
   * @param logHandlerFactory the factory that provides the log handler
   */
  NativeLogger(LoggerId loggerId, LogHandlerFactory logHandlerFactory) {
    this.loggerId = loggerId;
    this.logHandlerFactory = logHandlerFactory;
  }

  @Override
  public NativeLogger atLevel(Level level) {
    return loggerId.logSeverity == level
        ? this
        : new NativeLogger(new LoggerId(loggerId.loggerName, level), logHandlerFactory);
  }

  @Override
  public Level getLevel() {
    return loggerId.logSeverity;
  }

  @Override
  public boolean isEnabled() {
    return logHandlerFactory.getLogHandler().isEnabled(loggerId);
  }

  @Override
  public void log(Object message) {
    process(null, message, null);
  }

  @Override
  public void log(String message, Object... arguments) {
    process(null, message, arguments);
  }

  @Override
  public void log(Throwable throwable) {
    process(throwable, DEFAULT_THROWABLE_MESSAGE, null);
  }

  @Override
  public void log(Throwable throwable, Object message) {
    process(throwable, message, null);
  }

  @Override
  public void log(Throwable throwable, String message, Object... arguments) {
    process(throwable, message, arguments);
  }

  /**
   * Public API in addition to the [Logger] interface.
   *
   * @param throwable the exception to log, or null
   * @param message the message to log
   * @param arguments optional format arguments for the message
   * @apiNote Used by elf4j-engine internally, not meant for direct usage by client code. Made
   *     public for potential integration with other logging frameworks.
   */
  public void process(
      @Nullable Throwable throwable, @Nullable Object message, Object @Nullable [] arguments) {
    logHandlerFactory.getLogHandler().log(loggerId, throwable, message, arguments);
  }

  /**
   * Identifies a logger by name and severity level.
   *
   * <p>The logger name determines the minimum threshold level for logging. Messages are only logged
   * when the logger's severity level meets or exceeds this threshold.
   *
   * <p>In most cases, the logger name corresponds to the caller's class. However, in some patterns
   * (such as delegating to another class), the caller class and the actual logging class may
   * differ. The "logger" field represents the caller of the logging API, while the "class" field
   * (if different) represents the actual class performing the log call.
   *
   * @param loggerName the fully-qualified name of the caller class, used for threshold
   *     configuration
   * @param logSeverity the severity level of this logger instance
   */
  public record LoggerId(String loggerName, Level logSeverity) {}
}
