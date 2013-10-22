package net.kuujo.vertigo.context;

import net.kuujo.vertigo.network.Verticle;

import org.vertx.java.core.json.JsonObject;

/**
 * A verticle component context.
 *
 * @author Jordan Halterman
 */
public class VerticleContext extends ComponentContext {

  VerticleContext(JsonObject context) {
    super(context);
  }

  VerticleContext(JsonObject context, JsonObject parent) {
    super(context, parent);
  }

  /**
   * Gets the verticle main.
   *
   * @return
   *   The verticle main.
   */
  public String getMain() {
    return context.getString(Verticle.MAIN);
  }

}
