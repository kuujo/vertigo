import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.net.NetSocket;
i
import com.blankstyle.vine.Feeder;
port org.vertx.java.platform.Verticle;

import com.blankstyle.vine.Vine;
import com.blankstyle.vine.Seed;
import com.blankstyle.vine.definition.SeedDefinition;
import com.blankstyle.vine.local.LocalRoot;

/**
 * A verticle that deploys a vine that counts words.
 *
 * @author Jordan Halterman
 */
public class CountVerticle extends Verticle {

  @Override
  public void start() {
    VineDefinition definition = Vine.createDefinition("someaddress");
    SeedDefinition seed1 = definition.feed(Seed.createDefinition("seed1").setMain("MySeed.java"));
    SeedDefinition seed2 = definition.feed(Seed.createDefinition("seed2").setMain("MyOtherSeed.java"));
    SeedDefinition seed3 = Seed.createDefinition("seed3").setMain("SeedThree.java").setWorkers(2);
    seed1.to(seed3);
    seed2.to(seed3);

    Root root = new LocalRoot(vertx, container);
    root.deploy(context, new Handler<AsyncResult<Feeder>>() {
      @Override
      public void handle(AsyncResult<Feeder> result) {
        // If the vine failed to deploy the log the failure and return.
        if (result.failed()) {
          container.logger().info("Failed to deploy vine.");
          return;
        }

        Feeder feeder = result.result();

        // Create an asynchronous result handler for vine results.
        Handler<AsyncResult<Integer>> resultHandler = new Handler<AsyncResult<Integer>>() {
          @Override
          public void handle(Integer count) {
            container.logger().info("Word count is " + count);
          }
        };

        // Create a netserver that feeds data to the vine.
        vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
          @Override
          public void handle(NetSocket socket) {
            socket.dataHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer buffer) {
                if (!feeder.feedQueueFull()) {
                  feeder.feed(buffer, resultHandler);
                }
                else {
                  // If the feeder queue is full then pause the socket and
                  // set a drain handler on the feeder.
                  socket.pause();
                  feeder.drainHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void result) {
                      socket.resume();
                    }
                  });
                }
              }
            });
          }
        });
      }
    });
  }

}
