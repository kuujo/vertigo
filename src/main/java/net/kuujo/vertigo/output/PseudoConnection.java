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
package net.kuujo.vertigo.output;

/**
 * A pseudo-connection implementation.
 *
 * The pseudo connection is used in place of missing connections to ensure that
 * methods such as consistent hashing work properly even in the absense of some
 * connections. When a message is written to the pseudo connection, it behaves
 * as if it created/forked the message without actually sending the message data
 * to any address. This has the ultimate effect of timing out messages sent to
 * connections that are currently invalid.
 *
 * @author Jordan Halterman
 */
public interface PseudoConnection extends Connection {
}
