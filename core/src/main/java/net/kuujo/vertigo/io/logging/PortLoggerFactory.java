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

import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.io.OutputCollector;

import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Port logger factory.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class PortLoggerFactory {
  private static final Map<String, Map<OutputCollector, PortLogger>> loggers = new HashMap<>();

  /**
   * Creates an output port logger for the given type.
   *
   * @param clazz The type being logged.
   * @param port The output collector to which to log messages.
   * @return An output port logger.
   */
  public static PortLogger getLogger(Class<?> clazz, OutputCollector output) {
    return getLogger(clazz.getCanonicalName(), output);
  }

  /**
   * Creates an output port logger with the given name.
   *
   * @param name The logger name.
   * @param port The output collector to which to log messages.
   * @return An output port logger.
   */
  public static PortLogger getLogger(String name, OutputCollector output) {
    Map<OutputCollector, PortLogger> loggers = PortLoggerFactory.loggers.get(name);
    if (loggers == null) {
      loggers = new HashMap<>();
      PortLoggerFactory.loggers.put(name, loggers);
    }

    PortLogger logger = loggers.get(output);
    if (logger == null) {
      logger = new PortLogger(LoggerFactory.getLogger(name), output);
      loggers.put(output, logger);
    }
    return logger;
  }

}
