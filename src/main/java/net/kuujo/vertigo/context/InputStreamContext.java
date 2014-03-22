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
package net.kuujo.vertigo.context;

import net.kuujo.vertigo.input.grouping.Grouping;


/**
 * Input stream context.
 *
 * @author Jordan Halterman
 */
public class InputStreamContext extends Context<InputStreamContext> {
  private String address;
  private String stream;
  private String source;
  private Grouping grouping;
  private ConnectionContext connection;

  /**
   * Returns the stream name.
   *
   * @return The input stream name.
   */
  public String name() {
    return stream;
  }

  @Override
  public String address() {
    return address;
  }

  /**
   * Returns the stream source.
   *
   * @return The stream source.
   */
  public String source() {
    return source;
  }

  /**
   * Returns the stream grouping.
   *
   * @return The stream grouping.
   */
  public Grouping grouping() {
    return grouping;
  }

  /**
   * Returns the input connection context.
   *
   * @return The input connection context.
   */
  public ConnectionContext connection() {
    return connection;
  }

  @Override
  public void notify(InputStreamContext update) {
    super.notify(update);
    connection.notify(update.connection());
  }

  /**
   * Connection context builder.
   *
   * @author Jordan Halterman
   */
  public static class Builder extends net.kuujo.vertigo.context.Context.Builder<InputStreamContext> {

    private Builder() {
      super(new InputStreamContext());
    }

    private Builder(InputStreamContext context) {
      super(context);
    }

    /**
     * Creates a new context builder.
     *
     * @return A new input stream context builder.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /**
     * Creates a new context builder.
     *
     * @param context A starting input stream context.
     * @return A new input stream context builder.
     */
    public static Builder newBuilder(InputStreamContext context) {
      return new Builder(context);
    }

    /**
     * Sets the input stream name.
     *
     * @param stream The stream name.
     * @return The context builder.
     */
    public Builder setName(String stream) {
      context.stream = stream;
      return this;
    }

    /**
     * Sets the input stream address.
     *
     * @param address The input stream address.
     * @return The context builder.
     */
    public Builder setAddress(String address) {
      context.address = address;
      return this;
    }

    /**
     * Sets the input stream source.
     *
     * @param source The stream source.
     * @return The context builder.
     */
    public Builder setSource(String source) {
      context.source = source;
      return this;
    }

    /**
     * Sets the input grouping.
     *
     * @param grouping The input grouping.
     * @return The context builder.
     */
    public Builder setGrouping(Grouping grouping) {
      context.grouping = grouping;
      return this;
    }

    /**
     * Sets the stream connection.
     *
     * @param connection The input stream connection.
     * @return The context builder.
     */
    public Builder setConnection(ConnectionContext connection) {
      context.connection = connection;
      return this;
    }
  }

}
