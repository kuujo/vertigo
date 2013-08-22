import org.vertx.java.core.net.NetSocket;
import org.vertx.java.platform.Verticle;

import com.blankstyle.vine.Vine;
import com.blankstyle.vine.VineBuilder;

/**
 * A verticle that deploys a vine that counts words.
 *
 * @author Jordan Halterman
 */


/**
 * If the vine is created like:
 *
 * vine.from(this)
 *   .to(new SeedContext("SomeSeed.java")
 *   .deploy(new Handler<AsyncResult<Feeder>>() {
 *   
 *   });
 */
public class CountVine extends VineVerticle {

  public void start() {
    new VineContext("myvine").from("vine.myvine")
      .to(new SeedContext("vine.someseed", "SomeSeed.java").setWorkers(2))
      .to(new SeedContext("vine.someotherseed", "SomeOtherSeed.java").setWorkers(1))
      .deploy(new Handler<AsyncResult<Feeder>>() {
        public void handle(AsyncResult<Feeder> result) {
          if (result.succeeded()) {
            feeder.feed("Hello world!");
          }
          else {
            // We failed!
          }
        }
      });
  }

}


/**
 * If the vine is created like:
 *
 * vine.from(new FeederContext("myfeeder"))
 *   .to(new SeedContext("myseed").setMain("MySeed.java"))
 *   .deploy();
 */
public class CountVerticle extends FeederVerticle {

  public void feed() {
    
  }

}


public class CountVerticle extends Verticle {

  @Override
  public void start() {
    VineContext context = new VineContext("someaddress");
    SeedContext seed1 = context.feed(new SeedContext("seed1", "MySeed.java"));
    SeedContext seed2 = context.feed(new SeedContext("seed2", "MyOtherSeed.java"));
    seed1.feed(seed2);
    Root root = new LocalRoot(vertx, container);
    root.deploy(context, new Handler<AsyncResult<Feeder>>() {
      public void handle(AsyncResult<Feeder> result) {
        if (result.succeeded()) {
          result.result().feed("Hello world!");
        }
      }
    });

    new VineContext("someaddress").feed(new SeedContext("seed1", "MySeed.java"));

    new VineContext().from(new FeederContext("com.mycompany~my-feeder~0.1.0"))
      .to(new SeedContext("com.mycompany~my-seed~0.1.0").setWorkers(2))
      .to(new SeedContext("com.mycompany~my-other~0.1.0").setWorkers(2))
      .deploy(new Handler<AsyncResult<Feeder>>() {
        
      });

    VineBuilder builder = new VineBuilder("myvine");
    builder.setSeed("seed1", "SeedOne.java").setWorkers(2);
    builder.setSeed("seed2", "seed_two.php").setWorkers(2).connectTo("seed1");

    Root root = new LocalRoot(vertx, container);
    root.deploy(builder.createContext(), new Handler<Feeder>() {
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
