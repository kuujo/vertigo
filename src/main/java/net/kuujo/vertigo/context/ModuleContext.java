package net.kuujo.vertigo.context;

import net.kuujo.vertigo.network.Module;

import org.vertx.java.core.json.JsonObject;

/**
 * A module component context.
 *
 * @author Jordan Halterman
 */
public class ModuleContext extends ComponentContext {
 
  public ModuleContext() {
    super();
  }

  ModuleContext(JsonObject context) {
    super(context);
  }

  /**
   * Gets the module name.
   *
   * @return
   *   The module name.
   */
  public String getModule() {
    return context.getString(Module.MODULE);
  }

}
