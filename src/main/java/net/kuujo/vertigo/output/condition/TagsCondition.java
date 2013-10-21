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

import java.util.HashSet;
import java.util.Set;

import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * A tags condition.
 *
 * @author Jordan Halterman
 */
public class TagsCondition implements Condition {

  private Set<String> tags = new HashSet<>();

  public TagsCondition() {
  }

  public TagsCondition(String... tags) {
    for (String tag : tags) {
      this.tags.add(tag);
    }
  }

  @Override
  public JsonObject getState() {
    JsonArray tagsArray = new JsonArray();
    for (String tag : tags) {
      tagsArray.add(tag);
    }
    return new JsonObject().putArray("tags", tagsArray);
  }

  @Override
  public void setState(JsonObject state) {
    tags = new HashSet<String>();
    JsonArray tagsArray = state.getArray("tags");
    if (tagsArray == null) {
      tagsArray = new JsonArray();
    }
    for (Object tag : tagsArray) {
      tags.add((String) tag);
    }
  }

  @Override
  public boolean isValid(JsonMessage message) {
    return tags != null && tags.contains(message.tag());
  }

}
