package com.blankstyle.vine.test.integration;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import com.blankstyle.vine.Feeder;
import com.blankstyle.vine.Seeds;
import com.blankstyle.vine.Vine;
import com.blankstyle.vine.Vines;
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.local.LocalRoot;

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

}
