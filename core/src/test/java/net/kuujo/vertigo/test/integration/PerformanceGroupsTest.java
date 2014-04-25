package net.kuujo.vertigo.test.integration;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.NetworkConfig;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

public class PerformanceGroupsTest extends TestVerticle {

  public static class TestComponent1 extends ComponentVerticle {
    private final Handler<Void> sendRunner = new Handler<Void>() {
      @Override
      public void handle(Void _) {
        doSend();
      }
    };
    @Override
    public void start() {
      doSend();
    }
    private void doSend() {
      if (!output.port("out").sendQueueFull()) {
        output.port("out").group("foo", new Handler<OutputGroup>() {
          @Override
          public void handle(OutputGroup group) {
            group.send("Hello world!").end();
          }
        });
      }
      vertx.runOnContext(sendRunner);
    }
  }

  public static class TestComponent2 extends ComponentVerticle {
    @Override
    public void start() {
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(final InputGroup ingroup) {
          output.port("out").group("foo", new Handler<OutputGroup>() {
            @Override
            public void handle(final OutputGroup outgroup) {
              ingroup.messageHandler(new Handler<String>() {
                @Override
                public void handle(String message) {
                  outgroup.send(message);
                }
              });
              ingroup.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void _) {
                  outgroup.end();
                }
              });
            }
          });
        }
      });
    }
  }

  public static class TestComponent3 extends ComponentVerticle {
    private long lastTime;
    private long lastCount;
    private long currentCount;
    @Override
    public void start() {
      vertx.setPeriodic(1000, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          long currentTime = System.currentTimeMillis();
          System.out.println(String.format("Received %d grouped messages in %d milliseconds.", currentCount - lastCount, currentTime - lastTime));
          lastTime = currentTime;
          lastCount = currentCount;
        }
      });
      input.port("in").groupHandler("foo", new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          group.messageHandler(new Handler<String>() {
            @Override
            public void handle(String message) {
              currentCount++;
            }
          });
        }
      });
    }
  }

  @Test
  public void testPerformance() {
    Vertigo vertigo = new Vertigo(this);
    NetworkConfig network = vertigo.createNetwork("performance");
    network.addVerticle("1", TestComponent1.class.getName());
    network.addVerticle("2", TestComponent2.class.getName());
    network.addVerticle("3", TestComponent3.class.getName());
    network.createConnection("1", "out", "2", "in");
    network.createConnection("2", "out", "3", "in");
    vertigo.deployNetwork(network);
  }

}
