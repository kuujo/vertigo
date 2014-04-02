package net.kuujo.vertigo.message.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageAcker;

import org.apache.commons.lang.mutable.MutableInt;
import org.vertx.java.core.Handler;

public class DefaultMessageAcker implements MessageAcker {
  private final Map<String, Anchor> anchors = new HashMap<>();

  private static class Anchor {
    private final MutableInt count;
    private final Handler<Boolean> handler;

    private Anchor(MutableInt count, Handler<Boolean> handler) {
      this.count = count;
      this.handler = handler;
    }
  }

  @Override
  public void register(JsonMessage message, Handler<Boolean> ackHandler) {
    anchors.put(message.id(), new Anchor(new MutableInt(0), ackHandler));
  }

  @Override
  public void anchor(JsonMessage parent, JsonMessage child) {
    Anchor anchor = anchors.get(parent.id());
    if (anchor != null) {
      anchor.count.increment();
    }
  }

  @Override
  public void anchor(JsonMessage parent, List<JsonMessage> children) {
    Anchor anchor = anchors.get(parent.id());
    if (anchor != null) {
      anchor.count.add(children.size());
    }
  }

  @Override
  public void ack(JsonMessage parent) {
    Anchor anchor = anchors.get(parent.id());
    if (anchor != null) {
      anchor.count.decrement();
      if (anchor.count.intValue() == 0) {
        anchors.remove(parent.id()).handler.handle(true);
      }
    }
  }

  @Override
  public void timeout(JsonMessage parent) {
    Anchor anchor = anchors.remove(parent.id());
    if (anchor != null) {
      anchor.handler.handle(false);
    }
  }

}
