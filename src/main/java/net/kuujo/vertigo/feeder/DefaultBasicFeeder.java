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
package net.kuujo.vertigo.feeder;

import net.kuujo.vertigo.context.InstanceContext;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default basic feeder implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultBasicFeeder extends AbstractFeeder<BasicFeeder> implements BasicFeeder {

  public DefaultBasicFeeder(Vertx vertx, Container container, InstanceContext context) {
    super(vertx, container, context);
  }

  @Override
  public String emit(JsonObject data) {
    return doFeed(data, null, null, null, null);
  }

  @Override
  public String emit(JsonObject data, String tag) {
    return doFeed(data, tag, null, null, null);
  }

}
