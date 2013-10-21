package net.kuujo.vertigo.output;

import java.util.ArrayList;
import java.util.List;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.input.Input;
import net.kuujo.vertigo.input.filter.Filter;
import net.kuujo.vertigo.output.selector.Selector;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.serializer.SerializationException;
import net.kuujo.vertigo.serializer.Serializer;

/**
 * A component output definition.
 *
 * @author Jordan Halterman
 */
public class Output implements Serializable {

  private static final String ADDRESS = "address";
  private static final String GROUP = "group";
  public static final String SELECTOR = "selector";
  public static final String FILTERS = "filters";

  public static Output fromInput(Input input) {
    Output output = new Output();
    output.setState(input.getState());
    return output;
  }

  private JsonObject definition;

  public Output() {
  }

  public Output(String address) {
    definition = new JsonObject().putString(ADDRESS, address);
  }

  public String getGroup() {
    return definition.getString(GROUP);
  }

  public Selector getSelector() {
    JsonObject selectorInfo = definition.getObject(SELECTOR);
    try {
      return selectorInfo != null ? Serializer.<Selector>deserialize(selectorInfo) : null;
    }
    catch (SerializationException e) {
      return null;
    }
  }

  public List<Filter> getFilters() {
    List<Filter> filters = new ArrayList<Filter>();
    JsonArray filterInfos = definition.getArray(FILTERS);
    if (filterInfos == null) {
      return filters;
    }

    for (Object filterInfo : filterInfos) {
      try {
        filters.add(Serializer.<Filter>deserialize((JsonObject) filterInfo));
      } catch (SerializationException e) {
        // Do nothing.
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
