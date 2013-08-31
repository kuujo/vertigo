package com.blankstyle.vine.test.integration;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Seeds;
import com.blankstyle.vine.Vine;
import com.blankstyle.vine.Vines;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.eventbus.root.RootVerticle;
import com.blankstyle.vine.eventbus.stem.StemVerticle;
import com.blankstyle.vine.local.LocalRoot;
import com.blankstyle.vine.remote.RemoteRoot;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A vine deploy test.
 *
 * @author Jordan Halterman
 */
public class VineTest extends TestVerticle {

  private VineDefinition createTestDefinition() {
    VineDefinition vine = Vines.createDefinition("test.vine");
    vine.setMessageTimeout(5000).setMessageExpiration(15000);
    vine.feed(Seeds.createDefinition("seedone", TestSeedOne.class.getName()).setWorkers(2))
      .to(Seeds.createDefinition("seedtwo", TestSeedTwo.class.getName()).setWorkers(2));
    return vine;
  }

  @Test
  public void testLocalDeploy() {
    VineDefinition vine = createTestDefinition();
    LocalRoot root = new LocalRoot(vertx, container);
    root.deploy(vine, new Handler<AsyncResult<Vine>>() {
      @Override
      public void handle(AsyncResult<Vine> result) {
        if (result.succeeded()) {
          Vine vine = result.result();
          Feeder feeder = vine.feeder();
          feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<JsonObject>>() {
            @Override
            public void handle(AsyncResult<JsonObject> result) {
              if (result.succeeded()) {
                assertEquals("Hello world again again!", result.result().getString("body"));
              }
              else {
                assertTrue("Failed to process message.", false);
              }
              testComplete();
            }
          });
        }
        else {
          assertTrue("Failed to deploy vine. "+ result.cause(), false);
        }
      }
    });
  }

  @Test
  public void testRemoteDeploy() {
    deployRoot(new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.succeeded()) {
          deployStem(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.succeeded()) {
                VineDefinition vine = createTestDefinition();
                RemoteRoot root = new RemoteRoot("vine.root.test", vertx, container);
                root.deploy(vine, new Handler<AsyncResult<Vine>>() {
                  @Override
                  public void handle(AsyncResult<Vine> result) {
                    if (result.succeeded()) {
                      Vine vine = result.result();
                      Feeder feeder = vine.feeder();
                      feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<JsonObject>>() {
                        @Override
                        public void handle(AsyncResult<JsonObject> result) {
                          if (result.succeeded()) {
                            assertEquals("Hello world again again!", result.result().getString("body"));
                          }
                          else {
                            assertTrue("Failed to process message.", false);
                          }
                          testComplete();
                        }
                      });
                    }
                    else {
                      assertTrue("Failed to deploy vine. "+ result.cause(), false);
                    }
                  }
                });
              }
            }
          });
        }
      }
    });
  }

  /**
   * Deploys a test root.
   */
  private void deployRoot(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(RootVerticle.class.getName(), new JsonObject().putString("address", "vine.root.test"), new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          future.setResult(null);
        }
        else {
          assertTrue("Failed to deploy root.", false);
        }
      }
    });
  }

  /**
   * Deploys a test stem.
   */
  private void deployStem(Handler<AsyncResult<Void>> doneHandler) {
    final Future<Void> future = new DefaultFutureResult<Void>().setHandler(doneHandler);
    container.deployVerticle(StemVerticle.class.getName(),
      new JsonObject().putString("address", "vine.stem.test").putString("root", "vine.root.test"),
      new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          future.setResult(null);
        }
        else {
          assertTrue("Failed to deploy stem.", false);
        }
      }
    });
  }

}
