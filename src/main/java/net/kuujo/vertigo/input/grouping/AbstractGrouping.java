package net.kuujo.vertigo.input.grouping;

import java.util.UUID;

import org.vertx.java.core.json.JsonObject;

/**
 * An abstract grouping implementation.
 *
 * @author Jordan Halterman
 */
public abstract class AbstractGrouping implements Grouping {
  private JsonObject definition = new JsonObject();

  public AbstractGrouping() {
    definition.putString("id", UUID.randomUUID().toString());
  }

  @Override
  public String id() {
    return definition.getString("id");
  }

  @Override
  public int count() {
    return definition.getInteger("count", 1);
  }

  @Override
  public Grouping setCount(int count) {
    definition.putNumber("count", count);
    return this;
  }

  @Override
  public JsonObject getState() {
    return definition.copy();
  }

  @Override
  public void setState(JsonObject state) {
    definition = state.copy();
  }

}
