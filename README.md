Vine
====

Vine is a real-time computation engine built on the Vert.x application platform.
Similar to Storm, work can be distributed across clusters with a central module
assigning work to Vert.x instances in the cluster.

## Features
* Built on the [Vert.x](http://vertx.io/) application platform, so core Vert.x
  APIs, languages, and contributed modules can be used seamlessly with Vine.
* Easy to learn and integrate with existing applications using the familiar
  Vert.x handler system and Vert.x' native support for JSON.
* Provides limited reliability features such as timeouts, heartbeats, and
  message acking.
* Allows vines to be deployed locally within a single Vert.x instance or across
  a cluster of Vert.x instances.
* Supports remote-procedure calls, allowing for real-time request-response
  spanning multiple Vert.x instances.

## Road Map
1. Add language support for all core supported Vert.x language implementations.
2. Support transactional processing.
3. Improve tracking of complex trees.

## User Manual

1. [What is a vine?](#what-is-a-vine)
  * [The vine](#the-vine)
  * [The seed](#the-seed)
  * [The feeder](#the-feeder)
  * [The root](#the-root)
  * [The stem](#the-stem)
1. [Creating vines](#creating-vines)
1. [Writing Seeds](#writing-seeds)
1. [Deploying vines](#deploying-vines)
  * [Deploying vines in local mode](#deploying-vines-in-local-mode)
  * [Deploying vines in remote mode](#deploying-vines-in-remote-mode)
  * [Shutting down vines](#shutting-down-vines)
1. [Working with feeders](#working-with-feeders)
  * [Retrieving a feeder from a deployed vine](#retrieving-a-feeder-from-a-deployed-vine)
  * [Opening feeders to existing vines](#opening-feeders-to-existing-vines)
  * [Feeding data to a vine](#feeding-data-to-a-vine)
  * [Reading responses from a vine](#reading-responses-from-a-vine)
  * [Feeder flow control](#feeder-flow-control)
1. [The Python API](#the-python-api)
  * [Creating vines in Python](#creating-vines-in-python)
  * [Deploying vines in Python](#deploying-vines-in-python)
  * [Writing seeds in Python](#writing-seeds-in-python)

## What is a vine?
Vines are collections of processing elements - Vert.x verticles - that communicate
with each other in a predictable and reliable manner. Each vine consists of any number
of seeds that may be interconnected in any way. The vine understands the relationship
between seeds and handles setup and monitoring of seed verticles.

### How it works
#### The vine
When a new vine - a collection of worker seeds - is deployed with Vert.x, Vine starts
a special module called the vine verticle. The vine verticle serves as an entry point
for messages into the vine, directing data to the appropriate [seeds](#the-seed). The
vine verticle maintains a history of requests made to the vine, and once requests are
processed by each seed in the vine, a response is sent using the original message.
Finally, the vine verticle is responsible for recognizing when a request has failed to
be processed (timed out) and replay that message through the vine.

#### The seed
Seeds are the elements of a vine that perform processing on data. Each vine can consist
of any number of seeds, each of which may contain any number of workers that are
interconnected with other seed workers via special one-directional communication
channels. The seed's sole responsibility is to receive and emit messages in a timely
manner.

#### The feeder
Feeders are the entry point to a vine. Once a vine has been deployed, a feeder can be
used to send messages to and receive replies from the vine and read status information
from a vine.

#### The root
The vine *root* is at the core of a vine, hence the name. On the API side, the root is
the element that handles deployment of a complete vine. However, the user-side API
is not the only purpose of the vine root. When clustering vines, the *root* is a
module that runs on a single node in a Vert.x cluster. The root module's purpose is
to maintain a connection with all nodes in a cluster (using heartbeats) and assign
or reassign tasks to those nodes.

#### The stem
The *stem* is the modules that performs deployment of worker verticles. The stem
receives commands from the root indicating deployment information for a specific
seed worker, and the stem deploys the worker and ensures that the worker continues
to run (using heartbeats).

## Creating vines
In Java, vines are defined using the `com.blankstyle.vine.definition.VineDefinition`
helper class. This class creates a JSON representation of the vine structure that
can be passed around a Vert.x cluster and through Vert.x module/verticle configurations.

```java
VineDefinition vine = new VineDefinition("my.vine");
```

Each vine definition may contain any number of `SeedDefinition` instances, and each
`SeedDefinition` may contain any number of `SeedDefinition` as well. This creates
a hierarchical relationship between the parent vine and the seeds within them. To
connect an initial seed to a vine (a seed that will be fed all messages sent to
the vine) call the `VineDefinition.feed` method.

```java
SeedDefinition seedOne = vine.feed(new SeedDefinition("seedone").setMain("com.mycompany.myproject.SeedOne"));
```

As mentioned, each seed definition may contain any number of `SeedDefinition`
instances, as well, so one can chain definitions into a pipeline.

```java
SeedDefinition seedOne = new SeedDefinition("seedone").setMain("com.mycompany.myproject.SeedOne");
SeedDefinition seedTwo = new SeedDefinition("seedtwo").setMain("com.mycompany.myproject.SeedTwo");
SeedDefinition seedThree = new SeedDefinition("seedthree").setMain("com.mycompany.myproject.SeedThree");

vine.feed(seedOne).to(seedTwo).to(seedThree);
```

This example creates a vine that feeds data to `seedone` which feeds data to `seedtwo`
which feeds data to `seedthree`.

### Seed groupings
For seeds that have more than one worker, groupings may be used to define
how messages are dispatched between workers. For instance, in one scenario it
may be best to dispatch messages to workers in a round-robin fashion, while in
others consistent hashing may be required to ensure data integrity within each
worker instance.

```java
// Ensure messages with the same 'state' field value always go to the same worker instance.
SeedDefinition stateCounter = new SeedDefinition("statecounter")
  .setMain("com.mycompany.myproject.StateCounter")
  .groupBy(new FieldsGrouping("state"));
```

### Seed methods
* `setMain(string main)` - sets the seed Vert.x *main*, see [writing seeds](#writing-seeds)
  for more information on seed verticle implementations
* `setWorkers(int workers)` - sets the number of workers for the seed
* `setOption(string option, string value)` - sets an arbitrary seed option which may
  be used in task schedulers, for instance
* `groupBy(Grouping grouping)` - sets the seed grouping, see [groupings](#seed-groupings)
  for more information

## Writing seeds
Seeds represent a single element of data processing in Vine. Each seed can consist
of one or many workers, and seed workers can run within separate Vert.x instances.
Seeds, workers, and the overall vine are interconnected via the Vert.x eventbus.

In Java, seeds are defined by extending the `com.blankstyle.vine.SeedVerticle`
base verticle class and overriding the abstract `process` method.

```java
import com.blankstyle.vine.BasicSeedVerticle;

public class MyVerticle extends BasicSeedVerticle {

  @Override
  public void handle(JsonObject data) {
    emit(data);
  }

}
```

Seed verticles consist of only two public methods. The `handle` method is called
each time the worker receives a message on the vine. The `emit` method is called to
send data on to the next seed in the vine and can be called zero or many times for
each message processed.

## Deploying vines
Vines are deployed using the *root* API. Vine provides two different methods of
deployment - local and remote - which allow users to deploy vines within a single
Vert.x instance or a cluster of Vert.x instances respectively. Once a vine is deployed,
a *feeder* can be used to feed messages to the vine.

Each root contains two methods for deploying vines:
* `deploy(VineDefinition definition, Handler<AsyncResult<Vine>> resultHandler)`
* `deploy(VineDefinition definition, long timeout, Handler<AsyncResult<Vine>> resultHandler)`
* `shutdown(String address)` - shuts down a vine
* `shutdown(String address, Handler<AsyncResult<Void>> doneHandler)` - shuts down a vine
* `shutdown(String address, long timeout, Handler<AsyncResult<Void>> doneHandler)` - shuts down a vine

The `Feeder` instance that is returned by *root* `deploy` methods provides an interface
similar to that of the Vert.x `WriteStream`:
* `feedQueueFull()` - indicates whether the queue is full
* `feed(JsonObject data)` - feeds data to the vine
* `feed(JsonObject data, Handler<AsyncResult<Void>> doneHandler)` - feeds data
  to the vine, providing an asynchronous completion handler
* `feed(JsonObject data, long timeout, Handler<AsyncResult<Void>> doneHandler)` - feeds
  data to the vine, providing an asynchronous completion handler and processing timeout
* `execute(JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler)` - executes
  the vine as a remote procedure, awaiting a result
* `execute(JsonObject data, long timeout, Handler<AsyncResult<JsonObject>> resultHandler)`
  - executes the vine as a remote procedure, awaiting a result with a timeout
* `drainHandler(Handler<Void> handler)` - sets a drain handler on the feeder

### Deploying vines in local mode
Local vines are deployed within a single Vert.x instance. Using the
`com.blankstyle.vine.local.LocalRoot` [root](#the-root) implementation, a
[stem](#the-stem) is deployed on the local machine, and all vine seeds are deployed
using that stem. Deploying and operating on vines in local mode requires no setup
other than the creation of seed verticles and the definition of the vine.

```java
final logger = container.logger();

VineDefinition definition = Vines.createDefinition("word.counter")
  .feed(new SeedDefinition("count").setMain("com.mycompany.wordcounter.CountSeed").setWorkers(2).setGrouping(new FieldsGrouping("words")))
  .to(new SeedDefinition("persist").setMain("com.mycompany.wordcounter.PersistSeed").setWorkers(2));

Root root = new LocalRoot(vertx, container);
long timeout = 10000;

root.deploy(definition, timeout, new Handler<AsyncResult<Feeder>>() {
  public void handle(AsyncResult<Feeder> result) {
    if (result.succeeded()) {
      Feeder feeder = result.result();

      // Feed a message to the vine, awaiting a result.
      JsonObject message = new JsonObject().putString("words", "Hello world!");
      feeder.feed(message, new Handler<AsyncResult<Void>>() {
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            logger.warning("Failed to process message.");
          }
          else {
            logger.info("Processing completed!");
          }
          // Shut down the vine. Note that it is *important* that we shut down
          // the vine with the same *type* of root as we deployed it with. So,
          // local vines must be shut down with a local root and remote with remote.
          root.shutdown("word.counter");
        }
      });
    }
    else {
      logger.error(result.cause().getMessage());
    }
  }
});
```

### Deploying vines in remote mode
When deploying vines across a Vert.x cluster, Vine uses modules running within
each instance of the cluster to track and maintain local seed deployments. Each
Vine cluster must consist of the following:
* The Vine *root* module running on *exactly one* Vert.x instance in the cluster.
* The Vine *stem* module running on *each* Vert.x instance in the cluster.

The *root* module monitors each of the Vert.x instances and assigns tasks to the
stem running within that instance. The *stem* module receives assignments from
the root, starts seed workers, and restarts them if they die. Note that in remote
mode, *vine verticles are started within the Vert.x instance from which they were
deployed.*

#### Starting the remote root
To start the remote root module use

```
vertx runmod com.blankstyle~vine-root~0.1.0-SNAPSHOT -cluster -cluster-port 1234
```

or

```java
container.deployModule("com.blankstyle~vine-root~0.1.0-SNAPSHOT", new JsonObject().putString("address", "vine.root"));
```

#### Starting the remote stem
*The stem module always requires a configuration as each stem should have a unique address.*
```
{
  "address": "vine.stem.1"
}
```

To start the remote stem module use

```
vertx runmod com.blankstyle~vine-stem~0.1.0-SNAPSHOT -cluster -cluster-port 1234 -conf stem.json
```

or

```java
container.deployModule("com.blankstyle~vine-stem~0.1.0-SNAPSHOT", new JsonObject().putString("address", "vine.stem.1"));
```

#### Deploying the vine
Finally, once the *root* and *stems* have been started on the appropriate machines,
vines are deployed in exactly the same manner as with the `LocalRoot` except that
the `RemoteRoot` implementation accepts an additional `address` constructor argument.

```java
final logger = container.logger();

VineDefinition definition = Vines.createDefinition("word.counter")
  .feed(new SeedDefinition("count").setMain("com.mycompany.wordcounter.CountSeed").setWorkers(2).setGrouping(new FieldsGrouping("words")))
  .to(new SeedDefinition("persist").setMain("com.mycompany.wordcounter.PersistSeed").setWorkers(2));

Root root = new RemoteRoot("vine.root", vertx, container);
long timeout = 10000;

root.deploy(definition, timeout, new Handler<AsyncResult<Feeder>>() {
  public void handle(AsyncResult<Feeder> result) {
    if (result.succeeded()) {
      Feeder feeder = result.result();

      // Feed a message to the vine, awaiting a result.
      JsonObject message = new JsonObject().putString("words", "Hello world!");
      feeder.feed(message, new Handler<AsyncResult<JsonObject>>() {
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            logger.warning("Failed to process message.");
          }
          else {
            logger.info("Processing completed!");
          }
          // Shut down the vine.
          root.shutdown("word.counter");
        }
      });
    }
    else {
      logger.error(result.cause().getMessage());
    }
  }
});
```

### Shutting down vines
Vines can be shutdown by calling the `Vine.shutdown` method. Note that *vines
can only be shutdown by the process that created the vine.* This is because the
process for shutting down local vs remote vines differs significantly.

## Working with feeders
Feeders allow the user to control the flow of information to and from a vine.

### Retrieving a feeder from a deployed vine
Once a vine is deployed, the returned feeder can be used to feed data to the vine.

```java
root.deploy(definition, new Handler<AsyncResult<Feeder>>() {
  public void handle(AsyncResult<Feeder> result) {
    if (result.succeeded()) {
      Feeder feeder = result.result();
    }
  }
});
```

### Opening feeders to existing vines
Once a vine is deployed, a feeder can be retrieved or instantiated simply by
referencing the unique vine `address` that is assigned to each vine.

For instance, if we started a vine at `word.counter`, we can open a
feeder to the vine from a separate verticle.

```java
Feeder feeder = Vines.open("word.counter", vertx);
feeder.feed(new JsonObject().putString("body", "Hello world!"));
```

or

```java
```

### Feeding data to a vine
Data is fed to vines using the `Feeder.feed()` method. Then the `feed()`
method is called, the message will be sent to the vine's verticle for
storage before it is copied and forwarded on to the initial step(s) of
the vine.

Currently, vines and seeds *only support messages in JSON format* to
ensure seamless compatibility with the other languages supported by Vert.x.

```java
JsonObject data = new JsonObject().putString("body", "Hello world!");
feeder.feed(data);
```

### Reading responses from a vine
Each time a message is sent to a vine, the vine verticle records the
original message and stores it in memory until the request is completed.
Not only does this allow the vine verticle to ensure that data is completely
processed before it is lost, but it also allows vines to be used as an RPC
handler. If an additional response handler is provided to the `execute()` method,
the vine will send a reply with the vine result once processing is complete.

```java
JsonObject data = new JsonObject().putString("body", "Hello world!");
feeder.execute(data, new Handler<AsyncResult<JsonObject>>() {
  public void handle(AsyncResult<JsonObject> result) {
    if (result.succeeded()) {
      // Processing has completed. Get the response.
      JsonObject response = result.result();
      container.logger().info("Got a result! " + response.encode());
    }
    else {
      // Processing failed for some reason. Log the failure.
      container.logger().error(result.cause());
    }
  }
});
```

### Feeder flow control
The Vine feeder supports an interface similar to that of the Vert.x `WriteStream`.
Each vine verticle's configuration contains a max queue size - the maximum number
of messages that may be processing at any given time. This option can be configured
via the `VineDefinition`.
* `feedQueueFull()` - indicates whether the vine feed queue is full
* `drainHandler(Handler<Void> handler)` - sets a drain handler on the feeder

These features allow vines to be used seamlessly with Vert.x `ReadStream` APIs.

```java
// Create a netserver that feeds data to a vine.
vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
  @Override
  public void handle(NetSocket socket) {
    socket.dataHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        if (!feeder.feedQueueFull()) {
          feeder.feed(buffer);
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
```

## The Python API

### Creating Vines in Python
```python
from vine import definition

vine = definition.VineDefinition(address='my.vine')
vine.feed('seedone', main='seedone.py', workers=2).to('seedtwo', main='seedtwo.py', workers=2)
```

### Writing Seeds in Python
```python
from vine.seed import ReliableSeed

seed = ReliableSeed()

@seed.data_handler
def handle(data):
  seed.emit({'foo': 'bar', 'bar': 'baz'})

seed.start()
```

### Deploying Vines in Python
```python
from vine.definition import VineDefinition
from vine.root import LocalRoot

vine = definition.VineDefinition(address='my.vine')
vine.feed('seedone', main='seedone.py', workers=2).to('seedtwo', main='seedtwo.py', workers=2)

root = LocalRoot()

def deploy_handler(feeder):
  feeder.feed({'body': 'Hello world!'})

root.deploy(vine, deploy_handler)
```
