/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.io.logging;

import net.kuujo.vertigo.io.OutputCollector;
import net.kuujo.vertigo.io.port.OutputPort;

import org.vertx.java.core.logging.Logger;

/**
 * Component port logger logs messages to an output port.<p>
 *
 * The component port logger provides a basic Vert.x logger that
 * sends messages to an output port. This allows components to
 * potentially subscribe to error logs from other components.<p>
 *
 * The {@link PortLogger} extends the Vert.x {@link Logger}, and
 * all messages logged to the port are logged to an underlying Vert.x
 * {@link Logger} as well, so there's no need to use two loggers in
 * order to log messages to log files.<p>
 *
 * Port loggers are a core element of each component, so users
 * should never need to contstruct port loggers themselves.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PortLogger extends Logger {

  /**
   * <code>fatal</code> is the output port for logging fatal messages.
   */
  public static final String FATAL_PORT = "fatal";

  /**
   * <code>error</code> is the output port for logging error messages.
   */
  public static final String ERROR_PORT = "error";

  /**
   * <code>warn</code> is the output port for logging warning messages.
   */
  public static final String WARN_PORT = "warn";

  /**
   * <code>info</code> is the output port for logging info messages.
   */
  public static final String INFO_PORT = "info";

  /**
   * <code>debug</code> is the output port for logging debug messages.
   */
  public static final String DEBUG_PORT = "debug";

  /**
   * <code>trace</code> is the output port for logging trace messages.
   */
  public static final String TRACE_PORT = "trace";

  private final Logger logger;
  private final OutputPort fatal;
  private final OutputPort error;
  private final OutputPort warn;
  private final OutputPort info;
  private final OutputPort debug;
  private final OutputPort trace;

  PortLogger(Logger logger, OutputCollector output) {
    super(null);
    this.logger = logger;
    this.fatal = output.port(FATAL_PORT);
    this.error = output.port(ERROR_PORT);
    this.warn = output.port(WARN_PORT);
    this.info = output.port(INFO_PORT);
    this.debug = output.port(DEBUG_PORT);
    this.trace = output.port(TRACE_PORT);
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public void fatal(final Object message) {
    logger.fatal(message);
    fatal.send(message);
  }

  @Override
  public void fatal(final Object message, final Throwable t) {
    logger.fatal(message, t);
    fatal.send(message);
  }

  @Override
  public void error(final Object message) {
    logger.error(message);
    error.send(message);
  }

  @Override
  public void error(final Object message, final Throwable t) {
    logger.error(message, t);
    error.send(message);
  }

  @Override
  public void warn(final Object message) {
    logger.warn(message);
    warn.send(message);
  }

  @Override
  public void warn(final Object message, final Throwable t) {
    logger.warn(message, t);
    warn.send(message);
  }

  @Override
  public void info(final Object message) {
    logger.info(message);
    info.send(message);
  }

  @Override
  public void info(final Object message, final Throwable t) {
    logger.info(message, t);
    info.send(message);
  }

  @Override
  public void debug(final Object message) {
    logger.debug(message);
    debug.send(message);
  }

  @Override
  public void debug(final Object message, final Throwable t) {
    logger.debug(message, t);
    debug.send(message);
  }

  @Override
  public void trace(final Object message) {
    logger.trace(message);
    trace.send(message);
  }

  @Override
  public void trace(final Object message, final Throwable t) {
    logger.trace(message, t);
    trace.send(message);
  }

}
