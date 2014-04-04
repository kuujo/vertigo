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

import net.kuujo.vertigo.annotations.ClusterTypeInfo;
import net.kuujo.vertigo.annotations.LocalTypeInfo;
import net.kuujo.vertigo.state.impl.HazelcastStatePersistor;
import net.kuujo.vertigo.state.impl.MongoStatePersistor;
import net.kuujo.vertigo.state.impl.RedisStatePersistor;
import net.kuujo.vertigo.state.impl.SharedDataStatePersistor;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Component state persistor.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.NAME,
  include=JsonTypeInfo.As.PROPERTY,
  property="type"
)
@JsonSubTypes({
  @JsonSubTypes.Type(value=MongoStatePersistor.class, name="mongo"),
  @JsonSubTypes.Type(value=RedisStatePersistor.class, name="redis")
})
@LocalTypeInfo(defaultImpl=SharedDataStatePersistor.class)
@ClusterTypeInfo(defaultImpl=HazelcastStatePersistor.class)
public interface StatePersistor {

  /**
   * Loads the component state.
   *
   * @param resultHandler An asynchronous handler to be called once the state has been loaded.
   * @return The state persistor.
   */
  StatePersistor loadState(Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Stops the component state.
   *
   * @param state The component state to store.
   * @param doneHandler An asynchronous handler to be called once the state has been stored.
   * @return The state persistor.
   */
  StatePersistor storeState(JsonObject state, Handler<AsyncResult<Void>> doneHandler);

}
