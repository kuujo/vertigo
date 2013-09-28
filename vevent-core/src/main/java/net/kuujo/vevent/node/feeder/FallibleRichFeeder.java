package net.kuujo.vevent.node.feeder;

import net.kuujo.vevent.context.WorkerContext;
import net.kuujo.vevent.messaging.DefaultJsonMessage;
import net.kuujo.vevent.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

@SuppressWarnings("rawtypes")
public class FallibleRichFeeder extends AbstractFeeder implements RichBasicFeeder<RichBasicFeeder> {

  protected long maxQueueSize = 1000;

  protected long defaultTimeout = DEFAULT_TIMEOUT;

  protected FallibleRichFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public RichBasicFeeder setFeedQueueMaxSize(long maxSize) {
    this.maxQueueSize = maxSize;
    return this;
  }

  @Override
  public RichBasicFeeder setDefaultTimeout(long timeout) {
    this.defaultTimeout = timeout;
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data) {
    output.emit(DefaultJsonMessage.create(data));
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data, String tag) {
    output.emit(DefaultJsonMessage.create(data, tag));
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    createFuture(message, defaultTimeout, ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data, long timeout,
      Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data);
    createFuture(message, timeout, ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    createFuture(message, defaultTimeout, ackHandler);
    output.emit(message);
    return this;
  }

  @Override
  public RichBasicFeeder feed(JsonObject data, String tag, long timeout,
      Handler<AsyncResult<Void>> ackHandler) {
    JsonMessage message = DefaultJsonMessage.create(data, tag);
    createFuture(message, timeout, ackHandler);
    output.emit(message);
    return this;
  }

}
