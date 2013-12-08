Vert.igo
========

**[Java User Manual](https://github.com/kuujo/vertigo/wiki/Java-User-Manual) | [Javadoc](http://vertigo.kuujo.net/java/)**

Vertigo is a distributed event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Following a concept and
structure similar to [Storm](https://github.com/nathanmarz/storm), Vertigo
allows real-time problems to be broken down into smaller tasks (as Vert.x
verticles) and distributed across **one or many Vert.x instances**, managing
communication between components in a **predictable and reliable** manner.

* Manages multi-step event processing systems, from simple pipelines to
  **complex networks of Vert.x modules/verticles**, including **remote procedure
  calls spanning multiple Vert.x verticle instances**
* **Abstracts communication details** from verticle implementations by providing
  an API for defining verticle relationships at the point of deployment
* **Guarantees message processing** through ack/fail/timeout mechanisms, providing
  data sources with feedback on the status of processing simple or complex
  message trees
* Supports distribution of messages between multiple verticle instances using
  **random, round-robin, hashing, or fanout** approaches
* Supports **distribution of verticle/modules instances across a cluster** of Vert.x
  instances
* **Monitors networks for failures** and automatically reassigns/redeploys failed
  verticles and modules
* Network components can be written in **any Vert.x supported language**, with
  APIs for Vertigo 0.6 in [Javascript](https://github.com/kuujo/vertigo-js)
  and [Python](https://github.com/kuujo/vertigo-python)
* Integrates seemlessly with existing Vert.x applications

### Vertigo 0.6 is almost here!
Vertigo 0.6.0 features significant API and performance improvements and will be
released along with fully functional [Javascript](https://github.com/kuujo/vertigo-js)
and [Python](https://github.com/kuujo/vertigo-python) APIs. This version will signify
the solidification of the core Vertigo API and lays the foundation for a higher
level API and advanced operations in Vertigo.

#### New in Vertigo 0.6

* Smaller, **simpler core APIs** with explicitly typed components - feeder, worker, and executor
* Consolidated network deployment API in a `Vertx` like object

```java
Network network = vertigo.createNetwork("test");
network.addFeeder("test.feeder", "MyFeeder.java", 2);
network.addWorker("test.worker", "MyWorker.java", 4).addInput("test.feeder").randomGrouping();
vertigo.deployLocalNetwork(network);
```

* Automatic construction of component instances within component Vert.x verticles
* Special Java component verticle implementations
* Custom annotations for configuring Java components

```java
@FeederOptions(autoRetry=true)
public class MyFeeder extends FeederVerticle {
  @Override
  public void start(Feeder feeder) {
    feeder.emit(new JsonObject().putString("hello", "world"));
  }
}
```

* New **stream abstraction**. Emit messages to specific output streams and subscribe
to messages from specific input streams:

*network*

```java
Network network = vertigo.createNetwork("the.network");
network.addFeeder("the.feeder", "OrderProductFeeder.java");
network.addWorker("the.order_worker", "OrderWorker.java", 2).addInput("test.feeder", "order");
network.addWorker("the.product_worker", "ProductWorker.java", 4).addInput("test.feeder", "product");
```

`OrderProductFeeder.java`

```java
public class OrderProductFeeder extends FeederVerticle {
  @Override
  public void start(Feeder feeder) {
    feeder.emit("product", new JsonObject().putString("productid", "12345"));
    feeder.emit("order", new JsonObject().putString("orderid", "23456"));
  }
}
```

* Improved ack/fail/timeout feedback mechanisms
* Network deployment events via the Vert.x event bus
* Component event hooks via Java classes or the event bus
* Automated message schema validation
* Completely redesigned **Jackson-based serializer** which supports automated serialization
  of most obejcts (with a possible future in passing objects between components)
* Improved message tracking algorithm with significantly smaller memory footprint
  (able to track huge numbers of messages efficiently)
* [Complete Python API](https://github.com/kuujo/vertigo-python)
* [Complete Javascript API](https://github.com/kuujo/vertigo-js)

Vertigo is not a replacement for [Storm](https://github.com/nathanmarz/storm).
Rather, Vertigo is a lightweight alternative that is intended to be embedded
within larger Vert.x applications.

For an in-depth look at the concepts underlying Vertigo, check out
[how it works](https://github.com/kuujo/vertigo/wiki/How-it-works).

**[Javascript API](https://github.com/kuujo/vertigo-js)**

**[Python API](https://github.com/kuujo/vertigo-python)**

### Adding Vertigo as a Maven dependency

```
<dependency>
  <groupId>net.kuujo</groupId>
  <artifactId>vertigo</artifactId>
  <version>${vertigo.version}</version>
</dependency>
```

### Including Vertigo in a Vert.x module
To use the Vertigo Java API, you can include the Vertigo module in your module's
`mod.json` file. This will make Vertigo classes available within your module.

```
{
  "main": "com.mycompany.myproject.MyVerticle",
  "includes": "net.kuujo~vertigo~0.6.0"
}
```
