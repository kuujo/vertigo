import net.kuujo.vertigo.java.ComponentVerticle;

import org.vertx.java.core.Handler;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

public class App extends ComponentVerticle {

  @Override
  public void start() {
    input.port("in").messageHandler(new Handler<String>() {
      @Override
      public void handle(String message) {
        assertEquals("Hello world!", message);
        testComplete();
      }
    });
  }

}
