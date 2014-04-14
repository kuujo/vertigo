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
package net.kuujo.vertigo.output;

import net.kuujo.vertigo.streams.SendStream;

import org.vertx.java.core.Handler;

/**
 * Output interface.<p>
 *
 * This is the base interface for all output interfaces. It exposes basic
 * methods for grouping and sending messages.
 *
 * @author Jordan Halterman
 */
public interface Output<T extends Output<T>> extends SendStream<T> {

  /**
   * Creates an output group.
   *
   * @param name The output group name.
   * @param handler A handler to be called once the output group is created.
   * @return The output buffer.
   */
  T group(String name, Handler<OutputGroup> handler);

}
