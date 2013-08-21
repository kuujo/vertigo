Vine
====

Real-time computation engine built on the Vert.x application platform.

## Example

`MyVine.java`

```java
import org.vertx.java.platform.Verticle;

public class MyVine extends Verticle {

  @Override
  public void start() {
    VineBuilder builder = new DefaultVineBuilder();
    builder.addSeed();
    builder.addSeed();
    Vine vine = builder.buildVine();
    vine.deploy(new Handler<Void>() {
      @Override
      public void handle(Void deployed) {
        vine.feed("Hello world!");
      }
    });

    VineDeployer deployer = new DefaultVineDeployer();
    deployer.addSeed("myseed1", 2);
    deployer.deploy(new Handler<Vine>() {
      public void handle(Vine vine) {
        vine.feed("Hello world!");
        vine.feed("Hello world again!");
      }
    });
  }

}
```

Run the vine with `vertx run MyVine.java`

```php
public class MyVine extends VineVerticle {

  @Override
  public void start() {
    Vine vine = new BasicVine();
    vine.addVerticle("com.blankstyle.vine.MySeed").setWorkers(2);
    vine.addVerticle("com.blankstyle.vine.MyOtherSeed").setWorkers(2).connectTo("com.blankstyle.vine.MySeed");
    vine.deploy(new Handler<Feeder>() {
      @Override
      public void handle(Feeder feeder) {
        feeder.feed("Hello world!");
      }
    });
  }

}
```
