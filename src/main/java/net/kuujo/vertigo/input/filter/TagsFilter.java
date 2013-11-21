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

import java.util.HashSet;
import java.util.Set;

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
  private Set<String> tags;

  public TagsFilter() {
    tags = new HashSet<>();
  }

  public TagsFilter(String... tags) {
    this.tags = new HashSet<>();
    for (String tag : tags) {
      this.tags.add(tag);
    }
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
    tags.add(tag);
    return this;
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
    tags = new HashSet<>();
    JsonArray tagsArray = state.getArray("tags");
    if (tagsArray != null) {
      for (Object tag : tagsArray) {
        tags.add((String) tag);
      }
    }
  }

  @Override
  public Condition createCondition() {
    return new TagsCondition(tags);
  }

}
