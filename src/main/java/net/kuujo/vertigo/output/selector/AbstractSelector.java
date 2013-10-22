package net.kuujo.vertigo.output.selector;

import org.vertx.java.core.json.JsonObject;

/**
 * An abstract selector implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractSelector implements Selector {
  protected int connectionCount;
  protected String grouping;

  protected AbstractSelector() {
  }

  protected AbstractSelector(String grouping) {
    this.grouping = grouping;
  }

  protected AbstractSelector(int count, String grouping) {
    this.connectionCount = count;
    this.grouping = grouping;
  }

  @Override
  public int getConnectionCount() {
    return connectionCount;
  }

  @Override
  public String getGrouping() {
    return grouping;
  }

  @Override
  public JsonObject getState() {
    return new JsonObject().putString("grouping", grouping);
  }

  @Override
  public void setState(JsonObject state) {
    grouping = state.getString("grouping");
  }

}
