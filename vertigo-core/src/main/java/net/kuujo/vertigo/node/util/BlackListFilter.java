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
package net.kuujo.vertigo.node.util;

import java.util.HashSet;
import java.util.Set;

import org.vertx.java.core.Handler;

import net.kuujo.vertigo.messaging.JsonMessage;

/**
 * A tag black list filter.
 *
 * @author Jordan Halterman
 */
public class BlackListFilter implements Filter<BlackListFilter> {

  private Set<String> blackList = new HashSet<>();

  protected Handler<JsonMessage> messageHandler;

  protected Handler<JsonMessage> discardHandler;

  @Override
  public BlackListFilter messageHandler(Handler<JsonMessage> handler) {
    this.messageHandler = handler;
    return this;
  }

  @Override
  public BlackListFilter discardHandler(Handler<JsonMessage> handler) {
    this.discardHandler = handler;
    return this;
  }

  /**
   * Adds a tag to the black list.
   *
   * @param tag
   *   The tag to add.
   * @return
   *   The called filter instance.
   */
  public BlackListFilter addTag(String tag) {
    blackList.add(tag);
    return this;
  }

  /**
   * Removes a tag from the black list.
   *
   * @param tag
   *   The tag to remove.
   * @return
   *   The called filter instance.
   */
  public BlackListFilter removeTag(String tag) {
    blackList.remove(tag);
    return this;
  }

  @Override
  public void handle(JsonMessage message) {
    if (!blackList.contains(message.tag())) {
      messageHandler.handle(message);
    }
    else if (discardHandler != null) {
      discardHandler.handle(message);
    }
  }

}
