package net.kuujo.vertigo.context;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.serializer.Serializable;

public class InstanceContext implements Serializable {
  public static final String ID = "id";

  private JsonObject context;
  private ComponentContext parent;

  public InstanceContext() {
  }

  public InstanceContext(JsonObject context) {
    this.context = context;
    if (context.getFieldNames().contains("parent")) {
      JsonObject parentInfo = context.getObject("parent");
      String type = parentInfo.getString("type");
      if (type == "module") {
        parent = new ModuleContext();
      }
      else if (type == "verticle") {
        parent = new VerticleContext();
      }
      parent.setState(context.getObject("parent"));
    }
  }

  /**
   * Sets the instance parent.
   */
  InstanceContext setParent(ComponentContext parent) {
    this.parent = parent;
    return this;
  }

  /**
   * Returns the instance id.
   *
   * @return
   *   The unique instance id.
   */
  public String id() {
    return context.getString(ID);
  }

  /**
   * Returns the parent component context.
   *
   * @return
   *   The parent component context.
   */
  public ComponentContext getComponent() {
    return parent;
  }

  @Override
  public JsonObject getState() {
    JsonObject context = this.context.copy();
    if (parent != null) {
      context.putObject("parent", parent.getState());
    }
    return context;
  }

  @Override
  public void setState(JsonObject state) {
    context = state.copy();
  }

}
