/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.blankstyle.vine.impl;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.Feeder;

/**
 * A basic vine feeder.
 *
 * @author Jordan Halterman
 */
public class DefaultFeeder implements Feeder {

  protected String address;

  protected EventBus eventBus;

  public DefaultFeeder(String address, EventBus eventBus) {
    this.address = address;
    this.eventBus = eventBus;
  }

  @Override
  public boolean feedQueueFull() {
    return false;
  }

  @Override
  public Feeder setFeedQueueMaxSize(int maxSize) {
    return this;
  }

  @Override
  public Feeder drainHandler(Handler<Void> drainHandler) {
    return this;
  }

  @Override
  public void feed(Object data) {
    eventBus.send(address, data);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void feed(Object data, final Handler resultHandler) {
    eventBus.send(address, data, new Handler<Message>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(Message message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(JsonObject data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(JsonObject data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(JsonArray data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(JsonArray data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Buffer data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Buffer data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(byte[] data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(byte[] data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(String data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(String data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Integer data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Integer data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Long data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Long data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Float data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Float data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Double data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Double data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Boolean data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Boolean data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Short data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Short data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Character data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Character data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

  @Override
  public void feed(Byte data) {
    eventBus.send(address, data);
  }

  @Override
  public <T> void feed(Byte data, final Handler<T> resultHandler) {
    eventBus.send(address, data, new Handler<Message<T>>() {
      @Override
      public void handle(Message<T> message) {
        resultHandler.handle(message.body());
      }
    });
  }

}
