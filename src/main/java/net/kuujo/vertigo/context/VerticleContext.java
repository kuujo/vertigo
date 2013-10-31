package net.kuujo.vertigo.context;

import net.kuujo.vertigo.network.Verticle;

import org.vertx.java.core.json.JsonObject;

/**
 * A verticle component context.
 *
 * @author Jordan Halterman
 */
public class VerticleContext extends ComponentContext {
 
  public VerticleContext() {
    super();
  }

  VerticleContext(JsonObject context) {
    super(context);
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
