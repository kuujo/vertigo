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
package net.kuujo.vertigo.input.filter;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.output.condition.Condition;
import net.kuujo.vertigo.output.condition.TagsCondition;

/**
 * A tag filter.
 *
 * @author Jordan Halterman
 */
public class TagsFilter implements Filter {
  private JsonObject definition;

  public TagsFilter() {
  }

  public TagsFilter(String... tags) {
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
  public TagsFilter addTag(String tag) {
    JsonArray tagList = definition.getArray("tags");
    if (tagList == null) {
      tagList = new JsonArray();
      definition.putArray("tags", tagList);
    }
    if (!tagList.contains(tag)) {
      tagList.add(tag);
    }
    return this;
  }

  @Override
  public JsonObject getState() {
    return definition;
  }

  @Override
  public void setState(JsonObject state) {
    definition = state;
  }

  @Override
  public Condition createCondition() {
    return new TagsCondition((String[]) definition.getArray("tags").toArray());
  }

}
