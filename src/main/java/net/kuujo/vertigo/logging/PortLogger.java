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
package net.kuujo.vertigo.logging;

import net.kuujo.vertigo.output.OutputPort;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

/**
 * Component port logger logs messages to an output port.<p>
 *
 * The component port logger provides a basic Vert.x logger that
 * sends messages to an output port. This allows components to
 * potentially subscribe to error logs from other components.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PortLogger extends Logger {

  /**
   * The logger port name.
   */
  public static final String LOGGER_PORT = "__log__";

  private final Logger logger;
  private final OutputPort port;

  PortLogger(Logger logger, OutputPort port) {
    super(null);
    this.logger = logger;
    this.port = port;
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
    port.send(new JsonObject().putString("level", "fatal").putValue("message", message));
  }

  @Override
  public void fatal(final Object message, final Throwable t) {
    logger.fatal(message, t);
    port.send(new JsonObject().putString("level", "fatal").putValue("message", message));
  }

  @Override
  public void error(final Object message) {
    logger.error(message);
    port.send(new JsonObject().putString("level", "error").putValue("message", message));
  }

  @Override
  public void error(final Object message, final Throwable t) {
    logger.error(message, t);
    port.send(new JsonObject().putString("level", "error").putValue("message", message));
  }

  @Override
  public void warn(final Object message) {
    logger.warn(message);
    port.send(new JsonObject().putString("level", "warn").putValue("message", message));
  }

  @Override
  public void warn(final Object message, final Throwable t) {
    logger.warn(message, t);
    port.send(new JsonObject().putString("level", "warn").putValue("message", message));
  }

  @Override
  public void info(final Object message) {
    logger.info(message);
    port.send(new JsonObject().putString("level", "info").putValue("message", message));
  }

  @Override
  public void info(final Object message, final Throwable t) {
    logger.info(message, t);
    port.send(new JsonObject().putString("level", "info").putValue("message", message));
  }

  @Override
  public void debug(final Object message) {
    logger.debug(message);
    port.send(new JsonObject().putString("level", "debug").putValue("message", message));
  }

  @Override
  public void debug(final Object message, final Throwable t) {
    logger.debug(message, t);
    port.send(new JsonObject().putString("level", "debug").putValue("message", message));
  }

  @Override
  public void trace(final Object message) {
    logger.trace(message);
    port.send(new JsonObject().putString("level", "trace").putValue("message", message));
  }

  @Override
  public void trace(final Object message, final Throwable t) {
    logger.trace(message, t);
    port.send(new JsonObject().putString("level", "trace").putValue("message", message));
  }

}
