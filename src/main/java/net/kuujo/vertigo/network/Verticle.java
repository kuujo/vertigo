package net.kuujo.vertigo.network;

import net.kuujo.vertigo.context.ComponentContext;

import org.vertx.java.core.json.JsonObject;

/**
 * A verticle component.
 *
 * @author Jordan Halterman
 */
public class Verticle extends Component<Verticle> {

  public static final String MAIN = "main";

  public Verticle() {
    super();
  }

  public Verticle(String address) {
    super(address);
  }

  @Override
  public String getType() {
    return VERTICLE;
  }

  /**
   * Returns the verticle main.
   *
   * @return
   *   The verticle main.
   */
  public String getMain() {
    return definition.getString(MAIN);
  }

  /**
   * Sets the verticle main.
   *
   * @param main
   *   The verticle main.
   * @return
   *   The called verticle component.
   */
  public Verticle setMain(String main) {
    definition.putString(MAIN, main);
    return this;
  }

  @Override
  public ComponentContext createContext() throws MalformedNetworkException {
    JsonObject context = super.createJsonContext();
    context.putString(TYPE, Component.VERTICLE);
    String main = context.getString(MAIN);
    if (main == null) {
      throw new MalformedNetworkException("Invalid verticle component. No verticle main defined.");
    }
    return ComponentContext.fromJson(context);
  }

}
