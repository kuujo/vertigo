import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import com.blankstyle.vine.Vine;
import com.blankstyle.vine.VineBuilder;

/**
 * A verticle that deploys a vine that counts words.
 *
 * @author Jordan Halterman
 */
public class CountVerticle extends Verticle {

  @Override
  public void start() {
    VineContext context = Vine.createContext("someaddress");
    context.feed(Seed.createContext("seed1").setMain("MySeed.java"));
    context.feed(Seed.createContext("seed2").setMain("MyOtherSeed.java"));

    Root root = new LocalRoot(vertx, container);
    root.deploy(context, new Handler<AsyncResult<Feeder>>() {
      @Override
      public void handle(Feeder feeder) {
        Handler<Integer> resultHandler = new Handler<Integer>() {
          @Override
          public void handle(Integer count) {
            container.logger().info("Word count is " + count);
          }
        };

        vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
          @Override
          public void handle(NetSocket socket) {
            socket.dataHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer buffer) {
                if (!feeder.writeQueueFull()) {
                  feeder.feed(buffer, resultHandler);
                }
                else {
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
