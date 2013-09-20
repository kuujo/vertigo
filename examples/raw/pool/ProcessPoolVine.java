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
package pool;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;

import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.context.VineContext;
import com.blankstyle.vine.ReliableSeedVerticle;
import com.blankstyle.vine.Groupings;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.local.LocalRoot;
import com.blankstyle.vine.feeder.Executor;
import com.blankstyle.vine.feeder.BasicExecutor;

/**
 * A process pool example.
 *
 * This example uses a single seed with multiple worker instances to
 * perform RPC on a pool of workers.
 *
 * @author Jordan Halterman
 */
class ProcessPoolVine extends Verticle {

  public static class ProcessPool extends ReliableSeedVerticle {

    @Override
    public void process(JsonObject data) {
      emit(new JsonObject().putNumber("result", data.getInteger("first") * data.getInteger("second")));
      ack(data);
    }

  }

  private Logger logger;

  @Override
  public void start() {
    logger = container.logger();

    VineDefinition vine = new VineDefinition("processpool");
    vine.feed("pool", ProcessPool.class.getName(), 6).groupBy(Groupings.random());

    final Root root = new LocalRoot(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<VineContext>>() {
      @Override
      public void handle(Handler<AsyncResult<VineContext>> result) {
        if (result.succeeded()) {
          final VineContext context = result.result();
          Executor executor = new BasicExecutor(context, vertx);
          executor.execute(new JsonObject().putNumber("first", 10).putNumber("second", 100), new Handler<AsyncResult<JsonObject>>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
              if (result.succeeded()) {
                logger.info(String.format("10 * 100 = %d", result.result().getInteger("result")));
              }
              else {
                logger.warning("Failed to execute vine. " + result.cause());
              }
              root.shutdown(context);
            }
          });
        }
      }
    });
  }

}
