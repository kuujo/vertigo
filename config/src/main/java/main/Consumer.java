package main;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.port.PortInfo;

@ComponentInfo(stateful = true)
@InputInfo(value = { @PortInfo(name = "in", type = String.class),
  @PortInfo(name = "config", type = JsonObject.class) })
public class Consumer extends AbstractComponent {

  @Override
  public void start() throws Exception {

  }
}
