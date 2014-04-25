package net.kuujo.vertigo.test.integration;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.UUID;

import net.kuujo.vertigo.cluster.data.AsyncMap;
import net.kuujo.vertigo.cluster.data.impl.SharedDataMap;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

public class PerformanceMapTest extends TestVerticle {
  private long startTime;

  @Test
  public void testPerformance() {
    startTime = System.currentTimeMillis();
    doPutMap(0, 10000, new SharedDataMap<String, String>("test", vertx));
  }

  private void doPutMap(final int count, final int total, final AsyncMap<String, String> map) {
    if (count == total) {
      long endTime = System.currentTimeMillis();
      System.out.println(String.format("Mapped %d values in %d milliseconds.", total, endTime - startTime));
      testComplete();
    } else {
      map.put(UUID.randomUUID().toString(), "Hello world!", new Handler<AsyncResult<String>>() {
        @Override
        public void handle(AsyncResult<String> result) {
          assertTrue(result.succeeded());
          doPutMap(count+1, total, map);
        }
      });
    }
  }

}
