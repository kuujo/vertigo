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
package net.kuujo.vertigo.component.util;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.Handler;

import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A tag-based filter.
 *
 * @author Jordan Halterman
 */
public class TagListFilter implements Filter<TagListFilter> {

  private Set<String> allowedTags = new HashSet<>();

  protected Handler<JsonMessage> messageHandler;

  protected Handler<JsonMessage> discardHandler;

  @Override
  public TagListFilter messageHandler(Handler<JsonMessage> handler) {
    this.messageHandler = handler;
    return this;
  }

  @Override
  public TagListFilter discardHandler(Handler<JsonMessage> handler) {
    this.discardHandler = handler;
    return this;
  }

  /**
   * Adds an allowed tag to the filter.
   *
   * @param tag
   *   The tag to add.
   * @return
   *   The called filter instance.
   */
  public TagListFilter addTag(String tag) {
    allowedTags.add(tag);
    return this;
  }

  /**
   * Removes an allowed tag from the filter.
   *
   * @param tag
   *   The tag to remove.
   * @return
   *   The called filter instance.
   */
  public TagListFilter removeTag(String tag) {
    allowedTags.remove(tag);
    return this;
  }

  @Override
  public void handle(JsonMessage message) {
    if (allowedTags.contains(message.tag())) {
      messageHandler.handle(message);
    }
    else if (discardHandler != null) {
      discardHandler.handle(message);
    }
  }

}
