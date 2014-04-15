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

import net.kuujo.vertigo.context.OutputConnectionContext;
import net.kuujo.vertigo.util.Closeable;
import net.kuujo.vertigo.util.Openable;

/**
 * Output connection.<p>
 *
 * Output connections represent a direct connection between two instances
 * of separate components. Each output connection points to a single
 * input connection which receives messages from this connection and this
 * connection only.
 *
 * @author Jordan Halterman
 */
public interface OutputConnection extends Output<OutputConnection>, Openable<OutputConnection>, Closeable<OutputConnection> {

  /**
   * Returns the output connection context.
   *
   * @return The output connection context.
   */
  OutputConnectionContext context();

  /**
   * Returns the current connection send queue size.
   *
   * @return The current connection send queue size.
   */
  int getSendQueueSize();

}
