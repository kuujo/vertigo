package net.kuujo.vertigo.context;

import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.network.Verticle;

/**
 * A verticle component context.
 *
 * @author Jordan Halterman
 */
public class VerticleContext extends ComponentContext {
 
  public VerticleContext() {
    super();
  }

  public VerticleContext(JsonObject context) {
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
