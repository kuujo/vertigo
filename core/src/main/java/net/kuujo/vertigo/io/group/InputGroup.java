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
package net.kuujo.vertigo.io.group;

import net.kuujo.vertigo.io.Input;
import net.kuujo.vertigo.io.InputGroupSupport;

import org.vertx.java.core.Handler;

/**
 * Input groups represent collections of input messages.<p>
 *
 * In order for an input group to receive messages, an output group
 * of the same name must have sent messages from the other side of
 * the connection. Each time an output group is created a corresponding
 * input group will be created on the other side of the connection.<p>
 *
 * Input groups use a very particular order of operations. When a new
 * group message is received on an input connection, the connection will
 * create a corresponding input group. The group will not receive any
 * messages until it has registered a message handler. Once a message
 * handler has been registered, the input group will notify the
 * corresponding output group that it's prepared to accept messages.
 * This allows time for input groups to call asynchronous APIs during
 * setup.<p>
 *
 * Input group messages will be received in strong order just as all
 * other Vertigo messages. The input group uses the underlying connection
 * to validate message ordering. This ordering helps guarantee that
 * all child groups will be ended before the parent is ended. This allows
 * inputs to reliably create and destroy state using start and end handlers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface InputGroup extends Group<InputGroup>, Input<InputGroup>, InputGroupSupport<InputGroup> {

  /**
   * Registers a start handler on the group.
   *
   * @param handler A handler to be called when the group is started.
   * @return The input group.
   */
  InputGroup startHandler(Handler<Void> handler);

  /**
   * Registers an end handler on the group.
   *
   * @param handler A handler to be called when the group is ended.
   * @return The input group.
   */
  InputGroup endHandler(Handler<Void> handler);

}
