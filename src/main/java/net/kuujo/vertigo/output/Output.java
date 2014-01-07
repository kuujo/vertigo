/*
 * Copyright 2013 the original author or authors.
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

import net.kuujo.vertigo.network.Input;
import net.kuujo.vertigo.input.Listener;
import net.kuujo.vertigo.output.selector.Selector;
import net.kuujo.vertigo.serializer.Serializable;

/**
 * A component output.<p>
 *
 * The output is the polar opposite of the {@link Input}. When a {@link Listener}
 * subscribes to output from a component, its {@link Input} is transformed into
 * an {@link Output}, which indicates the {@link Selector} that is to be used in
 * emitted messages to that input.
 *
 * @author Jordan Halterman
 */
public final class Output implements Serializable {
  public static final String DEFAULT_STREAM = "default";
  private String id;
  private String stream;
  private int count = 1;
  private Selector selector;

  @SuppressWarnings("unused")
  private Output() {
  }

  public Output(String id, String stream, int count, Selector selector) {
    this.id = id;
    this.stream = stream;
    this.count = count;
    this.selector = selector;
  }

  /**
   * Returns the output id.
   *
   * @return
   *   The output id.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the output count.
   *
   * @return
   *   The output connection count.
   */
  public int getCount() {
    return count;
  }

  /**
   * Returns the output stream.
   *
   * @return
   *   The output stream.
   */
  public String getStream() {
    return stream;
  }

  /**
   * Returns the output selector.
   *
   * @return
   *   An output selector.
   */
  public Selector getSelector() {
    return selector;
  }

}
