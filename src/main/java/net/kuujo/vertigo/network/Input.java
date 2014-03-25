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
   * <code>out</code> is a string that indicates the output port to which this input
   * subscribes for messages.
   */
  public static final String INPUT_OUT_PORT = "out";

  /**
   * <code>in</code> is a string that indicates the input port on which this input
   * receives messages.
   */
  public static final String INPUT_IN_PORT = "in";

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
   * The default output port: <code>out</code>
   */
  public static final String DEFAULT_OUT_PORT = "out";

  /**
   * The default input port: <code>in</code>
   */
  public static final String DEFAULT_IN_PORT = "in";

  private String address;
  private String out = DEFAULT_OUT_PORT;
  private String in = DEFAULT_IN_PORT;
  private Grouping grouping;

  public Input(String address, String outPort, String inPort, Grouping grouping) {
    this.address = address;
    this.out = outPort;
    this.in = inPort;
    groupBy(grouping);
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
   * Returns the output port name.
   *
   * @return The output port name.
   */
  public String getOutputPort() {
    return out;
  }

  /**
   * Sets the output port name.
   *
   * @param out The output port name.
   * @return The input.
   */
  public Input setOutputPort(String out) {
    this.out = out;
    return this;
  }

  /**
   * Returns the input port name.
   *
   * @return The input port name.
   */
  public String getInputPort() {
    return in;
  }

  /**
   * Sets the input port name.
   *
   * @param in The input port name.
   * @return The input.
   */
  public Input setInputPort(String in) {
    this.in = in;
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
