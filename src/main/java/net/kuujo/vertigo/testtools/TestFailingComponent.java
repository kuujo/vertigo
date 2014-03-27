package net.kuujo.vertigo.testtools;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.Handler;

public class TestFailingComponent extends ComponentVerticle {

  @Override
  public void start(final Component component) {
    component.input().port("in").messageHandler(new Handler<JsonMessage>() {
      @Override
      public void handle(JsonMessage message) {
        component.input().port("out").fail(message);
      }
    });
  }

}
