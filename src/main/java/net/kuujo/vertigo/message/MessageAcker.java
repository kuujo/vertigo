package net.kuujo.vertigo.message;

import java.util.List;

import org.vertx.java.core.Handler;

public interface MessageAcker {

  void register(JsonMessage message, Handler<Boolean> ackHandler);

  void anchor(JsonMessage parent, JsonMessage child);

  void anchor(JsonMessage parent, List<JsonMessage> children);

  void ack(JsonMessage message);

  void timeout(JsonMessage message);

}
