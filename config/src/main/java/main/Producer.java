package main;

import io.vertx.core.json.JsonObject;
import net.kuujo.vertigo.component.AbstractComponent;
import net.kuujo.vertigo.component.ComponentInfo;
import net.kuujo.vertigo.io.OutputInfo;
import net.kuujo.vertigo.io.port.PortInfo;

@ComponentInfo(stateful = true)
@OutputInfo(value = { @PortInfo(name = "out", type = String.class),
  @PortInfo(name = "config", type = JsonObject.class) })
public class Producer extends AbstractComponent {
  @Override
  public void start() throws Exception {
    System.out.println("Producer start()");
  }
}
