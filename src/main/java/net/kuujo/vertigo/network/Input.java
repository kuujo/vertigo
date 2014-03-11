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
package net.kuujo.vertigo.network;

import java.util.UUID;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.util.serializer.SerializerFactory;

/**
 * Network component input.
 * 
 * @author Jordan Halterman
 */
public class Input implements Config {

  /**
   * <code>address</code> is a string that indicates the address to which this input
   * subscribes for messages. This can be any component address in any network within the
   * same Vert.x cluster. This field is required.
   */
  public static final String INPUT_ADDRESS = "address";

  /**
   * <code>stream</code> is a string that indicates the stream to which to subscribe for
   * messages. If the <code>stream</code> is not provided then the default
   * <code>default</code> stream will be used.
   */
  public static final String INPUT_STREAM = "stream";

  /**
   * <code>grouping</code> is an object defining the configuration for the input grouping.
   * The grouping determines how streams are partitioned among multiple instances of the
   * receiving component. This object must have at least the <code>type</code> field which
   * indicates the grouping type. Available grouping types include <code>round</code>,
   * <code>random</code>, <code>fields</code>, and <code>all</code>. The
   * <code>fields</code> grouping must also include an array of <code>fields</code> on
   * which to hash messages.
   */
  public static final String INPUT_GROUPING = "grouping";

  /**
   * The default input stream: <code>default</code>
   */
  public static final String DEFAULT_STREAM = "default";

  private String id;
  private String address;
  private String stream;
  private Grouping grouping;
  private int count = 1;

  protected Input() {
  }

  public Input(String address) {
    this(address, DEFAULT_STREAM, new RoundGrouping());
  }

  public Input(String address, String stream) {
    this(address, stream, new RoundGrouping());
  }

  public Input(String address, Grouping grouping) {
    this(address, DEFAULT_STREAM, grouping);
  }

  public Input(String address, String stream, Grouping grouping) {
    id = UUID.randomUUID().toString();
    this.address = address;
    this.stream = stream;
    groupBy(grouping);
  }

  /**
   * Returns the input id. This is a unique value used to identify identical inputs
   * between multiple component instances.
   * 
   * @return The input id.
   */
  public String id() {
    return id;
  }

  /**
   * Returns the input count.
   * 
   * @return The input count.
   */
  public int getCount() {
    return count;
  }

  /**
   * Sets the input count. This indicates the total number of expected subscriptions from
   * the input component and helps ensure consistency in message distribution between
   * multiple component instances.
   * 
   * @param count The input count.
   * @return The called input instance.
   */
  public Input setCount(int count) {
    this.count = count;
    return this;
  }

  /**
   * Returns the input address.
   * 
   * This indicates the address to which the input listens.
   * 
   * @return The input address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Returns the input stream ID.
   * 
   * @return The input stream ID.
   */
  public String getStream() {
    return stream;
  }

  /**
   * Sets the input stream ID.
   * 
   * @param stream The input stream ID.
   * @return The called input instance.
   */
  public Input setStream(String stream) {
    this.stream = stream;
    return this;
  }

  /**
   * Sets the input grouping.
   * 
   * The input grouping indicates how messages should be distributed between multiple
   * instances of the input component.
   * 
   * @param grouping An input grouping.
   * @return The called input instance.
   */
  public Input groupBy(Grouping grouping) {
    this.grouping = grouping;
    return this;
  }

  /**
   * Sets the input grouping as a string.
   * 
   * @param grouping The input grouping type.
   * @return The called input instance.
   */
  public Input groupBy(String grouping) {
    try {
      this.grouping = SerializerFactory.getSerializer(Grouping.class).deserializeObject(new JsonObject().putString("type", grouping), Grouping.class);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Invalid input grouping type " + grouping);
    }
    return this;
  }

  /**
   * Sets a random input grouping on the input.
   * 
   * @return The called input instance.
   */
  public Input randomGrouping() {
    this.grouping = new RandomGrouping();
    return this;
  }

  /**
   * Sets a round-robin input grouping on the input.
   * 
   * @return The called input instance.
   */
  public Input roundGrouping() {
    this.grouping = new RoundGrouping();
    return this;
  }

  /**
   * Sets a fields grouping on the input.
   * 
   * @param fields The fields on which to hash.
   * @return The called input instance.
   */
  public Input fieldsGrouping(String... fields) {
    this.grouping = new FieldsGrouping(fields);
    return this;
  }

  /**
   * Sets an all grouping on the input.
   * 
   * @return The called input instance.
   */
  public Input allGrouping() {
    this.grouping = new AllGrouping();
    return this;
  }

  /**
   * Returns the current input grouping.
   * 
   * @return The current input grouping.
   */
  public Grouping getGrouping() {
    return grouping;
  }

}
