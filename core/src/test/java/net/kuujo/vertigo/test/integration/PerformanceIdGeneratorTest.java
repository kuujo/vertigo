package net.kuujo.vertigo.test.integration;

import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;
import net.kuujo.vertigo.cluster.data.impl.SharedDataIdGenerator;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;

public class PerformanceIdGeneratorTest extends TestVerticle {
  private long startTime;

  @Test
  public void testPerformance() {
    startTime = System.currentTimeMillis();
    doGenerateId(0, 1000000, new SharedDataIdGenerator("test", vertx));
  }

  private void doGenerateId(final int count, final int total, final AsyncIdGenerator generator) {
    if (count == total) {
      long endTime = System.currentTimeMillis();
      System.out.println(String.format("Generated %d IDs in %d milliseconds.", total, endTime - startTime));
      testComplete();
    } else {
      generator.nextId(new Handler<AsyncResult<Long>>() {
        @Override
        public void handle(AsyncResult<Long> result) {
          assertTrue(result.succeeded());
          doGenerateId(count+1, total, generator);
        }
      });
    }
  }

}
