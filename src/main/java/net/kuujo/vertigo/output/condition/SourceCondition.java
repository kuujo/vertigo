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
package net.kuujo.vertigo.output.condition;

import net.kuujo.vertigo.message.JsonMessage;

import org.vertx.java.core.json.JsonObject;

/**
 * A source condition.
 *
 * @author Jordan Halterman
 */
public class SourceCondition implements Condition {
  private String source;

  public SourceCondition() {
  }

  public SourceCondition(String source) {
    this.source = source;
  }

  @Override
  public JsonObject getState() {
    return new JsonObject().putString("source", source);
  }

  @Override
  public void setState(JsonObject state) {
    source = state.getString("source");
  }

  @Override
  public boolean isValid(JsonMessage message) {
    String source = message.source();
    return source != null && source.equals(this.source);
  }

}
