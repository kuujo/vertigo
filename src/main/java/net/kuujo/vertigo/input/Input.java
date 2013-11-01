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
package net.kuujo.vertigo.input;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.filter.Filter;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A component input.
 *
 * @author Jordan Halterman
 */
public class Input implements Serializable {
  public static final String ID = "id";
  public static final String COUNT = "count";
  public static final String ADDRESS = "address";
  public static final String GROUPING = "grouping";
  public static final String FILTERS = "filters";

  private JsonObject definition;

  public Input() {
  }

  public Input(String address) {
    definition = new JsonObject().putString(ADDRESS, address).putString(ID, UUID.randomUUID().toString());
    groupBy(new RoundGrouping());
  }

  /**
   * Returns the input id.
   *
   * @return
   *   The input id.
   */
  public String id() {
    return definition.getString(ID);
  }

  /**
   * Returns the input count.
   *
   * @return
   *   The input count.
   */
  public int getCount() {
    return definition.getInteger(COUNT, 1);
  }

  /**
   * Sets the input count.
   *
   * @param count
   *   The input count.
   * @return
   *   The called input instance.
   */
  public Input setCount(int count) {
    definition.putNumber(COUNT, count);
    return this;
  }

  /**
   * Returns the input address.
   *
   * @return
   *   The input address.
   */
  public String getAddress() {
    return definition.getString(ADDRESS);
  }

  /**
   * Sets the input grouping.
   *
   * @param grouping
   *   An input grouping.
   * @return
   *   The called input instance.
   */
  public Input groupBy(Grouping grouping) {
    definition.putObject(GROUPING, Serializer.serialize(grouping));
    return this;
  }

  /**
   * Returns the current input grouping.
   *
   * @return
   *   The current input grouping.
   */
  public Grouping getGrouping() {
    JsonObject groupingInfo = definition.getObject(GROUPING);
    try {
      return Serializer.<Grouping>deserialize(groupingInfo);
    }
    catch (SerializationException e) {
      return null;
    }
  }

  /**
   * Adds an input filter.
   *
   * @param filter
   *   An input filter.
   * @return
   *   The called input instance.
   */
  public Input filterBy(Filter filter) {
    JsonArray filters = definition.getArray(FILTERS);
    if (filters == null) {
      filters = new JsonArray();
      definition.putArray(FILTERS, filters);
    }
    filters.add(Serializer.serialize(filter));
    return this;
  }

  /**
   * Returns a list of input filters.
   *
   * @return
   *   A list of input filters.
   */
  public List<Filter> getFilters() {
    List<Filter> filters = new ArrayList<>();
    JsonArray filterInfos = definition.getArray(FILTERS);
    if (filterInfos == null) {
      return filters;
    }

    for (Object filterInfo : filterInfos) {
      try {
        filters.add(Serializer.<Filter>deserialize((JsonObject) filterInfo));
      }
      catch (SerializationException e) {
        continue;
      }
    }
    return filters;
  }

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

}
