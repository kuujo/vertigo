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

import net.kuujo.vertigo.output.condition.Condition;
import net.kuujo.vertigo.output.condition.SourceCondition;

import org.vertx.java.core.json.JsonObject;

/**
 * A source filter.
 *
 * @author Jordan Halterman
 */
public class SourceFilter implements Filter {
  private String source;

  public SourceFilter() {
  }

  public SourceFilter(String source) {
    this.source = source;
  }

  /**
   * Sets the filter source.
   *
   * @param source
   *   The source by which to filter.
   * @return
   *   The called filter instance.
   */
  public SourceFilter setSource(String source) {
    this.source = source;
    return this;
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
  public Condition createCondition() {
    return new SourceCondition(source);
  }

}
