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
  public InputCollector addInput(Input input) {
    if (!inputs.contains(input)) {
      inputs.add(input);
      observers.put(input, new InputObserver(input, vertx, eventBus));
    }
    return this;
  }

  @Override
  public InputCollector messageHandler(Handler<JsonMessage> handler) {
    messageHandler = handler;
    for (Observer observer : observers.values()) {
      observer.messageHandler(handler);
    }
    return this;
  }

  @Override
  public InputCollector start() {
    for (Observer observer : observers.values()) {
      observer.messageHandler(messageHandler);
      observer.start();
    }
    return this;
  }

  @Override
  public InputCollector start(Handler<AsyncResult<Void>> doneHandler) {
    Iterator<Observer> iterator = observers.values().iterator();
    if (iterator.hasNext()) {
      recursiveStart(iterator, new DefaultFutureResult<Void>().setHandler(doneHandler));
    }
    else {
      new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
    }
    return this;
  }

  /**
   * Recursively starts all inputs.
   *
   * @param iterator
   *   An input observer iterator.
   * @param future
   *   A future to be invoked once starts are all complete.
   */
  private void recursiveStart(final Iterator<Observer> iterator, final Future<Void> future) {
    iterator.next().messageHandler(messageHandler).start(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else if (iterator.hasNext()) {
          recursiveStart(iterator, future);
        }
        else {
          future.setResult(null);
        }
      }
    });
  }

  @Override
  public void stop() {
    for (Observer observer : observers.values()) {
      observer.stop();
    }
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> doneHandler) {
    Iterator<Observer> iterator = observers.values().iterator();
    if (iterator.hasNext()) {
      recursiveStop(iterator, new DefaultFutureResult<Void>().setHandler(doneHandler));
    }
    else {
      new DefaultFutureResult<Void>().setHandler(doneHandler).setResult(null);
    }
  }

  /**
   * Recursively stops all inputs.
   *
   * @param iterator
   *   An input observer iterator.
   * @param future
   *   A future to be invoked once stops are all complete.
   */
  private void recursiveStop(final Iterator<Observer> iterator, final Future<Void> future) {
    iterator.next().stop(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          future.setFailure(result.cause());
        }
        else if (iterator.hasNext()) {
          recursiveStop(iterator, future);
        }
        else {
          future.setResult(null);
        }
      }
    });
  }

}
