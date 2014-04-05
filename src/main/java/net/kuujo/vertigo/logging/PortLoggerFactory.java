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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.output.OutputPort;

import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Output port logger factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PortLoggerFactory {
  private static final Map<String, Map<OutputPort, PortLogger>> loggers = new HashMap<>();

  /**
   * Creates an output port logger for the given type.
   *
   * @param clazz The type being logged.
   * @param port The output port to which to log messages.
   * @return An output port logger.
   */
  public static PortLogger getLogger(Class<?> clazz, OutputPort port) {
    return getLogger(clazz.getCanonicalName(), port);
  }

  /**
   * Creates an output port logger with the given name.
   *
   * @param name The logger name.
   * @param port The output port to which to log messages.
   * @return An output port logger.
   */
  public static PortLogger getLogger(String name, OutputPort port) {
    Map<OutputPort, PortLogger> loggers = PortLoggerFactory.loggers.get(name);
    if (loggers == null) {
      loggers = new HashMap<>();
      PortLoggerFactory.loggers.put(name, loggers);
    }

    PortLogger logger = loggers.get(port);
    if (logger == null) {
      logger = new PortLogger(LoggerFactory.getLogger(name), port);
      loggers.put(port, logger);
    }
    return logger;
  }

}
