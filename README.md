Vine
====

Vine is a real-time computation engine built on the Vert.x application platform.
Similar to Storm, work can be distributed across clusters with a central module
assigning work to Vert.x instances in the cluster. Messages are tracked as they
work their way through a *vine*, and messages that do not complete processing
through a *vine* are re-transmitted through the vine.

Vine instances can be easily integrated with your Vert.x application. Users can
deploy a *vine* across a Vert.x cluster with a single method call.

### A simple example

#### Deploy a vine
```java
VineDefinition definition = Vines.createDefinition("word.counter")
  .feed(new SeedDefinition("count").setMain("com.mycompany.wordcounter.CountSeed").setWorkers(2).setGrouping(new FieldsGrouping("words")))
  .to(new SeedDefinition("persist").setMain("persist_seed.js").setWorkers(2));

Root root = new LocalRoot(vertx, container);
long timeout = 10000;

root.deploy(definition, timeout, new Handler<AsyncResult<Vine>>() {
  public void handle(AsyncResult<Vine> result) {
    if (result.succeeded()) {
      Vine vine = result.result();

      // Feed a message to the vine, awaiting a result.
      JsonObject message = new JsonObject().putString("words", "Hello world!");
      vine.feeder().feed(message, new Handler<AsyncResult<JsonObject>>() {
        public void handle(AsyncResult<JsonObject> result) {
          if (result.failed()) {
            container.logger().warning("Failed to process message.");
          }
          else {
            container.logger().info("Count result is " + result.result().encode());
          }
          // Shut down the vine.
          vine.shutdown();
        }
      });
    }
    else {
      container.logger().error(result.cause().getMessage());
    }
  }
});
```

#### Process data
```java
class CountSeed extends SeedVerticle {

  protected void process(JsonObject data) {
    String words = data.getString("words", "");
    emit(new JsonObject().putNumber("count", words.length()));
  }

}
```

## Concepts

### Vine
A *vine* is a collection of Vert.x worker verticles that are linked together
by a reliable `EventBus` implementation. Each vine can contain any number of
*seeds* - worker verticles that extend the `com.blankstyle.vine.SeedVerticle`
abstract class to perform processing on data - and each *seed* can consist of
any number of workers. Vines are defined by a `VineDefinition` instance which
describes the vine configuration and layout, seed verticles and configurations,
and connections between seed workers.

#### Defining a vine
Before deploying a vine, you must first define the structure of the vine.
Vine definitions describe the message flow between different seeds (verticles)
within the vine. Once deployed, the definition is used to create a representation
of the vine topology and instruct verticles on which channels to connect to and
how to dispatch messages.

Vine provides a helper class, `Vines`, for creating vine definitions.
```java
VineDefinition vine = Vines.createDefinition("my.vine");
```

Note that the argument to the new definition is the *vine address*. For more
information see [how it works](#how-it-works).

#### Adding seeds to the vine
Seeds are added to the vine definition using the `SeedDefinition` class.
As with vine definitions, a helper `Seeds` class is provided for creating new
seed definitions.

```java
SeedDefinition seed = Seeds.createDefinition("seedone").setMain("com.mycompany.myproject.SeedOne.java").setWorkers(2);
```

Each seed within a vine must have a unique name. By default, the seed name
will be used to build a unique seed address - in this case `my.vine.seedone`.
Furthermore, unique addresses will be created for each worker, e.g.
`my.vine.seedone.1` and `my.vine.seedone.2`. Also, each seed *must define
a main* that extends the abstract `com.blankstyle.vine.SeedVerticle` class.
See [creating seeds](#creating-seeds) for more.

#### Creating connections
Connections must be defined between the vine and its seeds. Vines can be
connected to seeds and seeds can be connected to other seeds.

To better understand connections: when a vine is run, a single *vine verticle*
is started. The *vine verticle's* job is to receive incoming data, track
data through the vine, and produce results once data has completed processing.
When a vine is deployed, a feeder can be used to feed this *vine verticle*.
For each *seed* within the vine, the appropriate number of *workers* will be
started. Each worker communicates directly with any and all other seed
workers to which it is connected, so no central message broker is used
to route messages. *Any seed that does not have an outbound connection defined
will be automatically connected back to the* vine verticle.

For more information see [how it works](#how-it-works)

Connections are created using the `VineDefinition` and `SeedDefinition` APIs
respectively.

```java
VineDefinition vine = Vines.createDefinition("my.vine");

// Data fed into the vine will go to seedone. Note that we can
// feed data to any number of seeds.
SeedDefinition seedone = vine.feed(Seeds.createDefinition("seedone").setMain("com.mycompany.myproject.SeedOne").setWorkers(2));

// Feed results from seedone to seedtwo.
seedone.to(Seeds.createDefinition("seedtwo", "com.mycompany.myproject.SeedTwo"));
```

### Root
Vine supports two types of roots, `LocalRoot` and `RemoteRoot`. The primary
difference between the two is in clustering. The `LocalRoot` deploys vine
verticles within the local Vert.x instance, and `RemoteRoot` deploys vine
verticles across a cluster of Vert.x instances.

#### Deploying vines with *LocalRoot*
As mentioned, the `LocalRoot` deploys vines within a single Vert.x instance.
When a vine is deployed via a local root, the root will first deploy each of
the seed workers, then deploy a *stem* for monitoring workers, and finally
deploy a *vine verticle* for feeding and tracking data within the vine.

```java
VineDefinition vine = Vine.createDefinition("my.vine")
  .feed("seedone", "com.mycompany.myproject.SeedOne", 2)
  .to("seedtwo", "com.mycompany.myproject.SeedTwo", 2);

LocalRoot root = new LocalRoot(vertx, container);
root.deploy(vine, new Handler<AsyncResult<Vine>>() {
  public void handle(AsyncResult<Vine> result) {
    if (result.succeeded()) {
      result.result().feeder().feed(new JsonObject().putString("body", "Hello world!"));
    }
  }
});
```

#### Deploying vines with *RemoteRoot*
The `RemoteRoot` is a reference to a `vine-root` module instance runing
on a Vert.x instance within a cluster. When deploying a vine in a cluster,
one machine must be running an instance of the *root* module and each machine
must be running an instance of the *stem* module

The *root* module uses heartbeats to maintain a registry of *stems* within
the cluster and, when vines are deployed, assign *seed workers* to those
stems.

The *root* module address defaults to `vine.root`. You can provide an optional
configuration to the module to override the default address.

The *stem* module's purpose is to - when workers are assigned to it - start
those workers and ensure they continue to run until the vine is shutdown.
Heartbeats are used to ensure that workers continue to run.

When deploying the *stem* module, you *must* provide a configuration defining
the stem address.

The `RemoteRoot` API is the same as the `LocalRoot` API, as both classes
implement the `Root` interface. However, the `RemoteRoot` can accept an additional
root address argument in its constructor.

```java
Root root = new RemoteRoot("vine.root", vertx, container);
```

#### Root eventbus commands

**deploy**

Deploys a vine.

```
{
  "action": "deploy",
  "definition": {
    "address": "my.vine",
    ...
  }
}
```

**undeploy**

Undeploys a vine.

```
{
  "action": "undeploy",
  "address": "my.vine"
}
```

**register**

Registers a stem.

*address* is a stem address

*returns* a JSON object with a heartbeat *address*

```
{
  "action": "register",
  "address": "vine.stem.foo"
}
```

#### Seed eventbus commands

**assign**

Assigns a worker to the stem.

*context* is a worker context

```
{
  "action": "assign",
  "context": {
    "address": "my.vine.seedone.1",
    ...
  }
}
```

**release**

Releases a worker from the stem.

*context* is a worker context

```
{
  "action": "release",
  "context": {
    "address": "my.vine.seedone.1",
    ...
  }
}
```

**register**

Registers a worker.

*address* is a worker address

*returns* a JSON object with a heartbeat *address*

```
{
  "action": "register",
  "address": "my.vine.seedone.1"
}
```

## Creating seeds
Seeds are the elements that perform data processing in Vine. Seeds are
Vert.x verticles that maintain connections to other seeds and/or to the
parent *vine verticle*. In Java, seeds are created by extending the abstract
`com.blankstyle.vine.SeedVerticle` class and defining the protected
`process()` method.

```java
class FooSeed extends SeedVerticle {

  @Override
  protected void process(JsonObject data) {
    emit(data);
  }

}
```

Messages are sent to other seeds by calling the `emit()` method, passing
a `JsonObject` instance. *All vine messages are passed as JSON objects*
to ensure interoperability with Vert.x and its implemented languages.

It is important to note that *all seeds must emit processed messages at
least once*. Internally, the *vine verticle* maintains a history of data
transmitted through a vine, and if data does not complete processing within
a configurable amount of time, that data will be replayed through the vine.
Thus, failing to emit data within a seed will result in that data being
continuously processed.

## How it works

#### The vine root
The Vine `root` module is tasked with maintaining a list of *stems* available
within a cluster and assigning seed workers to those machines. The root may
only run on a single machine at any given time and has a unique address. It uses
heartbeats to maintain its list of *stems*, and custom *schedulers* may be
provided to perform custom assignments of seed workers to specific machines.

#### The vine stem
The Vine `stem` module is tasked with starting, stopping, and monitoring
*seed workers* assigned to a specific machine. One stem instance runs on
each machine within a cluster. Heartbeats are used to ensure that workers
continue to run.

#### Vine verticles
Each time a vine is started, a special verticle instance is started whose
task is to feed messages to the vine and keep track of data as it makes
its way through the vine. See [guaranteed processing](#guaranteed-processing)
for more information on how this works.

#### Feeders
Feeders are the interface to interact with a vine. Feeders are a direct
connection to a *vine verticle* and can be instantiated from anywhere
within a cluster by using the unique *vine address*.

### Reliability
Vine provides a `ReliableEventBus` implementation which adds features like
timeouts and re-sends to the Vert.x event bus. Additionally, Vine uses
heartbeats to ensure that seed workers stay alive.

### Guaranteed processing
Vine provides some guarantees for data processing. When a vine is started,
Vine starts a *vine verticle* to which messages are fed. Each time a message
is fed to the vine, the message is tagged with a unique identifier and a
record is kept of which messages have been received. Each time a seed receives
a message, the message is tagged with the seed name. Once the last seed in
a vine finishes processing a message, the message is emitted back to the
*vine verticle* which then checks to ensure that the message completed
each of the steps in the vine. Only if the message has completed processing
will the *vine verticle* response to the original feed message. If a message
does not finish running through the vine in a configurable amount of time,
the message will be considered lost and will be replayed through the vine.

## Setting up a cluster

### Deploying the root
The `vine-root` module should be deployed in a single Vert.x instance
within the cluster. The root module accepts an optional configuration:

```
{
  "address": "vine.root"
}
```

To deploy the root module use

```
vertx runmod com.blankstyle~vine-root~0.1.0-SNAPSHOT -cluster -cluster-port 1234
```

### Deploying the stem
The `vine-stem` module should be deployed in any Vert.x instance in which
you wish to run *seed workers*. The stem module *always requires a configuration*
as each stem should have a unique *address*.

```
{
  "address": "vine.stem.foo"
}
```

To deploy the stem module use

```
vertx runmod com.blankstyle~vine-stem~0.1.0-SNAPSHOT -cluster -cluster-port 1234 -conf stem.json
```

### Deploying a vine across a cluster

```java
import com.blankstyle.vine.definition.VineDefinition;
import com.blankstyle.vine.definition.SeedDefinition;
import com.blankstyle.vine.remote.RemoteRoot;
import com.blankstyle.vine.Root;
import com.blankstyle.vine.Vine;

import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;

import org.vertx.java.platform.Verticle;

class DeployVerticle extends Verticle {

  @Override
  public void start() {
    VineDefinition vine = new VineDefinition("my.vine")
      .feed(new SeedDefinition("seedone", "com.mycompany.myproject.SeedOne"))
      .to(new SeedDefinition("seedtwo", "com.mycompany.myproject.SeedTwo"))

    Root root = new RemoteRoot("vine.root", vertx, container);
    root.deploy(vine, new Handler<AsyncResult<Vine>>() {
      @Override
      public void handle(AsyncResult<Vine> result) {
        if (result.succeeded()) {
          Vine vine = result.result();
        }
      }
    });
  }
}
```
