package net.kuujo.vertigo.context;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.network.Module;

/**
 * A module component context.
 *
 * @author Jordan Halterman
 */
public class ModuleContext extends ComponentContext {
 
  public ModuleContext() {
    super();
  }

  public ModuleContext(JsonObject context) {
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
