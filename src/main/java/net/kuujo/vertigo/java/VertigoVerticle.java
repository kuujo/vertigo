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
package net.kuujo.vertigo.java;

import net.kuujo.vertigo.Vertigo;

import org.vertx.java.core.Future;
import org.vertx.java.platform.Verticle;

/**
 * A Vertigo verticle implementation.
 * <p>
 * 
 * This base class makes the primary {@link Vertigo} API available to Java verticle
 * implementations as the <code>vertigo</code> protected member. This class should not be
 * extended within actual component implementations. For components, use the respective
 * component verticle implementations.
 * <p>
 * 
 * <pre>
 * public class MyVerticle extends VertigoVerticle {
 *   public void start() {
 *     Network network = vertigo.createNetwork(&quot;my_network&quot;);
 *     network.addFeeder(&quot;foo.bar&quot;, &quot;foobar.py&quot;);
 *     network.addWorker(&quot;foo.baz&quot;, &quot;foobaz.js&quot;, 4);
 * 
 *     vertigo.deployLocalNetwork(network);
 *   }
 * }
 * </pre>
 * 
 * @author Jordan Halterman
 */
public abstract class VertigoVerticle extends Verticle {
  protected Vertigo vertigo;

  @Override
  public void start(Future<Void> future) {
    vertigo = new Vertigo(this);
    super.start(future);
  }

}
