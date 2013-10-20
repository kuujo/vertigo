package net.kuujo.vertigo.context;

import net.kuujo.vertigo.network.Module;

import org.vertx.java.core.json.JsonObject;

/**
 * A module component context.
 *
 * @author Jordan Halterman
 */
public class ModuleContext extends ComponentContext {

  ModuleContext(JsonObject context) {
    super(context);
  }

  ModuleContext(JsonObject context, JsonObject parent) {
    super(context, parent);
  }

  /**
   * Gets the module name.
   *
   * @return
   *   The module name.
   */
  public String module() {
    return context.getString(Module.MODULE);
  }

}
