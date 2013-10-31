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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.filter.FieldFilter;
import net.kuujo.vertigo.input.filter.Filter;
import net.kuujo.vertigo.input.filter.SourceFilter;
import net.kuujo.vertigo.input.filter.TagsFilter;
import net.kuujo.vertigo.input.grouping.AllGrouping;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.input.grouping.Grouping;
import net.kuujo.vertigo.input.grouping.RandomGrouping;
import net.kuujo.vertigo.input.grouping.RoundGrouping;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * Factory methods for creating networks.
 *
 * @author Jordan Halterman
 */
public final class Networks {

  /**
   * Creates a network definition from JSON.
   *
   * @param json
   *   A JSON representation of the network.
   * @return
   *   A new network instance.
   */
  public static Network fromJson(JsonObject json) throws MalformedNetworkException {
    Network network = new Network(json.getString(Network.ADDRESS));
    network.setNumAuditors(json.getInteger(Network.AUDITORS, 1));
    if (json.getBoolean(Network.ACKING, true)) {
      network.enableAcking();
    }
    else {
      network.disableAcking();
    }
    network.setAckExpire(json.getLong(Network.ACK_EXPIRE, Network.DEFAULT_ACK_EXPIRE));

    JsonObject components = json.getObject("components");
    for (String address : components.getFieldNames()) {
      JsonObject componentInfo = components.getObject(address);
      String type = componentInfo.getString(Component.TYPE);
      Component<?> component;
      if (type == "module") {
        Module module = network.addComponent(new Module(componentInfo.getString(Component.ADDRESS)));
        module.setModule(componentInfo.getString(Module.MODULE));
        component = module;
      }
      else if (type == "verticle") {
        Verticle verticle = network.addComponent(new Verticle(componentInfo.getString(Component.ADDRESS)));
        verticle.setMain(componentInfo.getString(Verticle.MAIN));
        component = verticle;
      }
      else {
        throw new MalformedNetworkException(String.format("Invalid component type %s.", type));
      }

      component.setConfig(componentInfo.getObject(Component.CONFIG, new JsonObject()));
      component.setInstances(componentInfo.getInteger(Component.INSTANCES, 1));
      component.setHeartbeatInterval(componentInfo.getLong(Component.HEARTBEAT_INTERVAL, 1000));

      // Create component inputs.
      JsonObject inputs = componentInfo.getObject("inputs", new JsonObject());
      for (String inputAddress : inputs.getFieldNames()) {
        JsonObject inputInfo = inputs.getObject(inputAddress);
        Input input = component.addInput(inputAddress);

        // Set up the input grouping.
        JsonObject groupingInfo = inputInfo.getObject("grouping");
        if (groupingInfo == null) {
          groupingInfo = new JsonObject().putString("type", "round");
        }

        String groupingType = groupingInfo.getString("type");
        if (groupingType == null) {
          throw new MalformedNetworkException("Invalid grouping type. No type defined.");
        }

        Grouping grouping;
        switch (groupingType) {
          case "round":
            grouping = new RoundGrouping();
            break;
          case "random":
            grouping = new RandomGrouping();
            break;
          case "fields":
            grouping = new FieldsGrouping(groupingInfo.getString("field"));
            break;
          case "all":
            grouping = new AllGrouping();
            break;
          default:
            Class<?> groupingClass;
            try {
              groupingClass = Class.forName(groupingType);
            }
            catch (ClassNotFoundException e) {
              throw new MalformedNetworkException(String.format("Invalid grouping type %s", groupingType));
            }
            try {
              grouping = (Grouping) groupingClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
              throw new MalformedNetworkException(String.format("Invalid grouping type %s", groupingType));
            }
        }

        input.groupBy(grouping);

        // Set up input filters.
        JsonArray filters = inputInfo.getArray("filters");
        for (Object filterInfo : filters) {
          String filterType = ((JsonObject) filterInfo).getString("type");
          Filter filter;
          switch (filterType) {
            case "field":
              filter = new FieldFilter();
              break;
            case "tags":
              filter = new TagsFilter();
              break;
            case "source":
              filter = new SourceFilter();
              break;
            default:
              Class<?> filterClass;
              try {
                filterClass = Class.forName(filterType);
              }
              catch (ClassNotFoundException e) {
                throw new MalformedNetworkException(String.format("Invalid filter type %s.", filterType));
              }
              try {
                filter = (Filter) filterClass.newInstance();
              }
              catch (InstantiationException | IllegalAccessException e) {
                throw new MalformedNetworkException(String.format("Invalid filter type %s.", filterType));
              }
          }
          filter.setState((JsonObject) filterInfo);
          input.filterBy(filter);
        }
      }
    }
    return network;
  }

}
