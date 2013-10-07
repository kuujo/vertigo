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
package net.kuujo.vertigo.filter;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A tag filter.
 *
 * @author Jordan Halterman
 */
public class TagFilter implements Filter {

  private JsonObject definition;

  public TagFilter() {
  }

  public TagFilter(String... tags) {
    definition = new JsonObject();
    JsonArray tagList = new JsonArray();
    for (String tag : tags) {
      tagList.add(tag);
    }
    definition.putArray("tags", tagList);
  }

  /**
   * Adds a tag to the filter.
   *
   * @param tag
   *   The tag to add.
   * @return
   *   The called filter instance.
   */
  public TagFilter addTag(String tag) {
    JsonArray tagList = definition.getArray("tags");
    if (tagList == null) {
      tagList = new JsonArray();
      definition.putArray("tag", tagList);
    }
    if (!tagList.contains(tag)) {
      tagList.add(tag);
    }
    return this;
  }

  @Override
  public boolean valid(JsonMessage message) {
    JsonArray tags = definition.getArray("tags");
    return tags.contains(message.tag());
  }

  @Override
  public JsonObject serialize() {
    return definition.copy().putString("filter", TagFilter.class.getName());
  }

  @Override
  public Filter initialize(JsonObject data) {
    definition = data;
    return this;
  }

}
