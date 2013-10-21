package net.kuujo.vertigo.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * A default input collector implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultInputCollector implements InputCollector {
  private Vertx vertx;
  private EventBus eventBus;
  private Set<Input> inputs = new HashSet<>();
  private Map<Input, Observer> observers = new HashMap<>();
  private Handler<JsonMessage> messageHandler;

  public DefaultInputCollector(Vertx vertx) {
    this.vertx = vertx;
    this.eventBus = vertx.eventBus();
  }

  public DefaultInputCollector(Vertx vertx, EventBus eventBus) {
    this.vertx = vertx;
    this.eventBus = eventBus;
  }

  @Override
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    return this;
  }

  @Override
  public InputCollector listen(Input input) {
    return null;
  }

  @Override
  public InputCollector listen(Input input, Handler<AsyncResult<Void>> doneHandler) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub
    
  }

}
