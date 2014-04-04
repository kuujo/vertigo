/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.state;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;

/**
 * Component snapshot object.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class Snapshot extends JsonObject {
  private final StatePersistor persistor;

  public Snapshot(StatePersistor persistor) {
    this.persistor = persistor;
  }

  /**
   * Loads the snapshot state.
   *
   * @param doneHandler An asynchronous handler to be called once the state has been loaded.
   * @return The snapshot.
   */
  public Snapshot load(final Handler<AsyncResult<Void>> doneHandler) {
    persistor.loadState(new Handler<AsyncResult<JsonObject>>() {
      @Override
      public void handle(AsyncResult<JsonObject> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else {
          for (String fieldName : result.result().getFieldNames()) {
            putValue(fieldName, result.result().getValue(fieldName));
          }
          new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
        }
      }
    });
    return this;
  }

  /**
   * Persists the snapshot to underlying storage.
   *
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The snapshot.
   */
  public Snapshot persist(Handler<AsyncResult<Void>> doneHandler) {
    persistor.storeState(this, doneHandler);
    return this;
  }

}
