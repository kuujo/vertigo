Vert.igo
========

**[The Vertigo Python API has moved](https://github.com/kuujo/vertigo-python)**

Vertigo is a distributed event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Following a concept and
structure similar to [Storm](https://github.com/nathanmarz/storm), Vertigo
allows real-time problems to be broken down into smaller tasks (as Vert.x
verticles) and distributed across **one or many Vert.x instances**, managing
communication between components in a **predictable and reliable** manner.

* Supports simple pipelines or complex networks of Vert.x modules/verticles,
  including remote procedure calls spanning multiple Vert.x verticle instances
* Supports message ack/fail/timeout mechanisms, providing data sources
  with feedback on the state of processing simple or complex message trees
* Monitors networks for failures and automatically reassigns/redeploys failed
  verticles and modules
* Can distribute verticle/module instances among a single Vert.x instance or
  across a cluster of Vert.x instances
* Network components can be written in any Vert.x supported language, with
  current integration for Java and [Python](https://github.com/kuujo/vertigo-python)
* Integrates seemlessly with existing Vert.x applications

Vertigo is not a replacement for [Storm](https://github.com/nathanmarz/storm).
Rather, Vertigo is a lightweight alternative that is intended to be embedded
within larger Vert.x applications.

## Java User Manual

1. [Networks](#networks)
1. [Components](#components)
   * [Feeders](#feeders)
   * [Workers](#workers)
   * [Executors](#executors)
1. [Messaging](#messaging)
   * [How components communicate](#how-components-communicate)
   * [Groupings](#groupings)
   * [Filters](#filters)
1. [Reliability](#message-acking)
   * [How acking works](#how-acking-works)
1. [Defining networks](#defining-networks)
   * [Defining network components](#defining-network-components)
   * [Defining connections](#defining-connections)
   * [Network structures](#network-structures)
   * [Remote procedure calls](#defining-remote-procedure-calls)
1. [Network deployment](#network-deployment)
   * [Clustering](#clustering)

## Networks
A network is the representation of a collection of [components](#components) - special
Vert.x verticle implementations- and the connections between them. Put together,
a network processes streams of data in real-time. Vertigo puts no limitations
on network structures. Each component may be connected to zero or many other components,
and circular relationships may be created as well. Networks are defined using
the [definition](#defining-networks) API and [deployed](#network-deployment) using
local or [Via](https://github.com/kuujo/via) clusters.

![Vert.igo Network](http://s9.postimg.org/xuv3addj3/vertigo_complex_network.png)

This is an example of a complex network. Given a set of Vert.x verticles or
modules, Vertigo uses a code-based representation of the network structure
to define connections between network components, start component verticles
or modules, monitor components, and manage communication between them in a fast,
reliable manner.

See the [complex network example](https://github.com/kuujo/vertigo/tree/master/examples/complex)

## Components
A component represents a single vertex in a Vertigo network graph. Each network
may contain any number of components, and each component may have any number of
instances running within the network (each of which may be assigned to different
machines around a [cluster](#network-deployment)). Within the context of Vert.x
a component can be defined as a verticle that may receive messages from zero or
many verticles and send messages to one or many verticles. What happens within the
verticle depends entirely where they appear in a network graph and how the component
is implemented. Vertigo provides several component implementations.

### Creating components
Components are simply special Vert.x verticle instances. As such, Vertigo provides
a special `Verticle` helper for Java-based component verticles. To create a Java
component verticle, extend the `VertigoVerticle` class. The `VertigoVerticle`
provides a couple more *protected* members for component instances.
* `vertigo` - an instance of `Vertigo`
* `context` - a `WorkerContext` object

The `Vertigo` interface is similar to that Vert.x's `Vertx` interface in that
it is essentially a helper for creating Vertigo components. The `Vertigo` object
exposes the following methods:
* `createFeeder()` - creates a [basic feeder](#basic-feeder)
* `createBasicFeeder()` - creates a [basic feeder](#basic-feeder)
* `createPollingFeeder()` - creates a [polling feeder](#polling-feeders)
* `createStreamFeeder()` - creates a [stream feeder](#stream-feeders)
* `createExecutor()` - creates a [basic executor](#executors)
* `createBasicExecutor()` - creates a [basic executor](#executors)
* `createWorker()` - creates a [worker](#workers)

### Contexts
The `WorkerContext` object contains information relevant to the current component
instance, as well as its parent component definition and even information about
the entire network layout, including unique addresses for each network component
instance.

The `WorkerContext` exposes the following interface:
* `address()` - the unique worker event bus address
* `config()` - the worker configuration - this is inherited from the component definition
* `getComponentContext()` - returns the parent component context

The `ComponentContext` exposes the following interface:
* `address()` - the component address - this is the basis for all component instance addresses
* `getConnectionContexts()` - a collection of `ConnectionContext` instances that
  describe the addresses to which this component feeds/emits messages and their
  component [filters](#filters) and [groupings](#groupings)
* `getWorkerContexts()` - a collection of all component instance contexts for the component
* `getDefinition()` - the component [definition](#defining-network-components)
* `getNetworkContext()` - returns the parent network context

The `NetworkContext` exposes the following interface:
* `address()` - the network address - this is the basis for all component addresses
* `broadcastAddress()` - the network broadcast address - this is the event bus
  address used by network [auditors](#message-acking) to broadcast message statuses (acks/nacks)
* `getNumAuditors()` - returns the number of network [auditors](#message-acking)
* `getAuditors()` - returns a set of network auditor addresses, each auditor is
  assigned its own unique event bus address
* `getComponentContexts()` - a collection of all network component contexts
* `getDefinition()` - the network [definition](#defining-networks)

### Feeders
Feeders are components whose sole responsibility is to feed data to a network.
Data generated by feeders may come from any source, and Vertigo provides a number
of feeder implementations for integrating networks with a variety of Vert.x and
other APIs.

Each feeder exposes the following configuration methods:
* `setMaxQueueSize(long queueSize)` - sets the maximum feed queue size
* `maxQueueSize()` - gets the maximum feed queue size
* `queueFull()` - indicates whether the feed queue is full
* `setAutoRetry(boolean retry)` - sets whether to automatically retry sending
  [failed](#message-acking) messages
* `autoRetry()` - indicates whether auto retry is enabled
* `setRetryAttempts(int attempts)` - sets the number of retries to attempt
  before explicitly failing the feed. To set an infinite number of retry
  attempts pass `-1` as the `attempts` argument
* `retryAttempts()` - indicate the number of automatic retry attempts

To start a feeder, call the `start()` method:
* `start(Handler<AsyncResult<BasicFeeder>> startHandler)`

Once a feeder has been started, you can feed messages using the `feed()` method:
* `feed(JsonObject data)`
* `feed(JsonObject data, String tag)`
* `feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler)`
* `feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler)`

When passing an `ackHandler` to a `feed` method, the handler will be invoked
once an `ack` or `fail` message is received from a network *auditor*. If
`autoRetry` is enabled, `retryAttempts` attempts will be made before the
handler will be invoked.

#### Basic Feeder
The `BasicFeeder` is a simple feeder implementation that provides no additional
methods for integrating with Vert.x APIs. It is sufficient for simple feed
operations. Once a `BasicFeeder` has been created, you must call the `start`
method, passing an asynchronous result handler to be invoked once the feeder
has been registered with the network.

The basic feeder exposes only the standard feeder methods.

```java
BasicFeeder feeder = vertigo.createBasicFeeder();
feeder.start(new Handler<AsyncResult<BasicFeeder>>() {
  public void handle(AsyncResult<BasicFeeder> result) {
    BasicFeeder feeder = result.result();

    feeder.setMaxQueueSize(1000);
    final JsonObject data = new JsonObject().putString("body", "Hello world!");

    if (!feeder.queueFull()) {
      feeder.feed(data, new Handler<AsyncResult<Void>>() {
        public void handle(AsyncResult<Void> result) {
          if (result.failed()) {
            // Processing failed. Re-emit the message.
            feeder.feed(data);
          }
          else {
            // Processing succeeded!
          }
        }
      }
    });
  }
});
```

#### Polling Feeder
The `PollingFeeder` is designed to poll for new messages only when the feed
queue is not empty. To do so, a `feedHandler` is registered on the feeder.

The `PollingFeeder` provides the following additional configuration methods:
* `setFeedDelay(long delay)` - sets the interval between feed attempts when
  no messages were fed by the `feedHandler`
* `feedDelay()` - indicates the feed delay

To register a feed handler, call the `feedHandler()` method, passing a
handler instance.

```java
PollingFeeder feeder = vertigo.createPollingFeeder();
feeder.start(new Handler<AsyncResult<PollingFeeder>>() {
  public void handle(AsyncResult<PollingFeeder> result) {
    PollingFeeder feeder = result.result();

    feeder.setMaxQueueSize(1000);
    final JsonObject data = new JsonObject().putString("body", "Hello world!");

    feeder.feedHandler(new Handler<PollingFeeder>() {
      public void handle(PollingFeeder feeder) {
        feeder.feed(new JsonObject().putString("body", "Hello world!"), new Handler<AsyncResult<Void>>() {
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              // Message was failed or timed out.
            }
            else {
              // Message was successfully processed.
            }
          }
        });
      }
    });
  }
});
```

#### Stream Feeder
The `StreamFeeder` is designed to integrate with Vert.x `ReadStream` APIs,
such as with sockets. To do so, it exposes an API similar to that of the
`WriteStream`, providing `fullHandler` and `drainHandler` methods.
* `fullHandler(Handler<Void> handler)` - sets a handler to be invoked when
  the feed queue is full
* `drainHandler(Handler<Void> handler)` - sets a handler to be invoked
  once a full feed queue is prepared to accept new messages

```java
vertigo.createStreamFeeder().start(new Handler<AsyncResult<StreamFeeder>>() {
  public void handle(AsyncResult<StreamFeeder> result) {
    if (result.succeeded()) {
      final StreamFeeder feeder = result.result();

      // Always retry sending data infinitely.
      feeder.setAutoRetry(true);
      feeder.setRetryAttempts(-1);

      NetServer server = vertx.createNetServer();

      server.connectHandler(new Handler<NetSocket>() {
        public void handle(final NetSocket sock) {

          // Set full and drain handlers on the feeder.
          feeder.fullHandler(new VoidHandler() {
            public void handle() {
              sock.pause();
            }
          });
          feeder.drainHandler(new VoidHandler() {
            public void handle() {
              sock.resume();
            }
          });

          sock.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
              feeder.feed(new JsonObject().putString("body", buffer.toString()));
            }
          });
        }
      }).listen(1234, "localhost");
    }
  }
});
```

### Workers
Worker components are the primary units of processing in Vertigo. Workers
are designed to both receive input and emit output (though workers can do
one or the other). The processes that are performed in between depend
entirely on the implementation.

```java
final Worker worker = vertigo.createWorker();
worker.messageHandler(new Handler<JsonMessage>() {
  public void handle(JsonMessage message) {
    JsonArray words = message.getArray("words").size();
    if (words != null) {
      // Emit the number of words.
      worker.emit(new JsonObject().putNumber("count", words), message);
      worker.ack(message);
    }
    else {
      // This is an invalid message. Fail it.
      worker.fail(message);
    }
  }
}).start();
```

Note that the API provides additional reliability features - message acking.
See [how message acking works](#message-acking)

### Executors
Executors are components that execute part or all of a network essential
as a remote procedure invocation. Data emitted from executors is tagged
with a unique ID, and new messages received by the executor are correlated
with past emissions.

```java
vertigo.createBasicExecutor().start(new Handler<AsyncResult<BasicExecutor>>() {
  public void handle(AsyncResult<BasicExecutor> result) {
    if (result.succeeded()) {
      BasicExecutor executor = result.result();
      executor.execute(new JsonObject().putNumber("x", 10).putNumber("y", 45),
        new Handler<AsyncResult<JsonMessage>>() {
          public void handle(AsyncResult<JsonMessage> result) {
            if (result.failed()) {
              // Message was failed or timed out.
            }
            else {
              Number sum = result.result().body().getNumber("sum");
            }
          }
        });
    }
  }
});
```

Networks that use remote procedure invocations must be designed in a very
specific manner. Remote procedure calls work by essentially creating circular
connections between network components. See
[defining remote procedure calls](#defining-remote-procedure-calls) for more
on how this works.

## Messaging
One of the primary responsibilities of Vertigo is managing communication between
network components in a consistent, predictable, and reliable manner. Vertigo
uses the Vert.x event bus for inter-component communication, but Vertigo also
provides many reliability features on top of the event bus.

### How components communicate
Network components communicate with each other directly over the event bus
rather than being routed through a central message broker. When a network
is created, Vertigo assigns unique addresses *to each component (verticle)
instance* which that verticle uses to communicate with the verticles around
it. Thus, each component instance essentially maintains a direct connection
to its neighbors, ensuring fast messaging between them.

![Communication Channels](http://s7.postimg.org/unzwkrvgb/vertigo_channel.png)

### Groupings
With each component instance maintaining its own unique event bus address,
Vertigo needs a way to determine which component messages emitted from one
component are dispatched to. Each component may indicate a *grouping* which
determines how messages are distributed among multiple instances of the
component. For instance, one component may need to receive all messages
emitted to it, while another may be need to receive messages in a round-robin
fashion. Vertigo provides groupings for various scenarios, including
consistent-hashing based groupings. Custom component groupings may also
be provided.

Groupings are abstracted from component implementations, so they can be added
when *defining* a network component rather than within component verticles
themselves.

To set a component grouping, call the `groupBy()` method on a component
definition, passing a grouping instance.

```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).groupBy(new FieldsGrouping("type"));
```

When messages are emitted to instances of the component, the related grouping
*dispatcher* will be used to determine to which component instance a given
message is sent.

Vertigo provides several grouping types:
* `RandomGrouping` - component instances receive messages in random order
* `RoundGrouping` - component instances receive messages in round-robin fashion
* `FieldsGrouping` - component instances receive messages according to basic
  consistent hashing based on a given field
* `AllGrouping` - all component instances receive a copy of each message

### Filters
Vertigo messages contain metadata in addition to the message body. And just
as with grouping component instances, sometimes components may be only
interested in receiving messages containing specific metadata. For this,
Vertigo provides message filters which allow components to define the types
of messages they receive. As with groupings, custom filters may be provided.

Filters are abstracted from component implementations, so they can be added
when *defining* a network rather than within component verticles themselves.

To add a filter to a component, call the `filterBy()` method on a component
definition, passing a filter instance. Multiple filters can be set on any
given component, in which case a message must pass *all* filters before being
sent to the component.

```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).filterBy(new TagsFilter("product"));
```

Vertigo provides several types of filters:
* `TagsFilter` - filters messages by tags
* `FieldFilter` - filters messages according to a field/value
* `SourceFilter` - filters messages according to the source component name

## Message acking
Vertigo's implicit control over the messaging infrastructure of component
verticles allows it to provide some unique reliability features. When a
Vertigo network is deployed, a special verticle called the *auditor* verticle
is deployed along with it. The Vertigo *auditor* is tasked with monitoring
messages within the network, tracking acks and nacks throughout the network,
and notifying *feeders* when a message tree fails.

![Network Auditor](http://s14.postimg.org/kkl297qo1/vertigo_acker.png)

### How acking works
When a *feeder* component creates and emits a new message, the feeder's
`OutputCollector` will notify the network's *auditor* of the newly created
message. *It is the responsibility of each worker component to ack or fail
the message*. If a worker creates a child of the message, the auditor is
told about the new relationship. If a descendant of a message is failed,
the original feeder will be notified of the failure. The feeder may
optionally replay failed messages through the network. Once *all*
descendants of a message have been successfully *acked*, the original
feeder will be notified. The *auditor* maintains a timer for each message
tree (the timers are actually shared among several message trees). If the
timer is triggered prior to the entire message tree being *acked*, the
tree will be failed and the original feeder will be notified.

## Defining networks
Networks are defined in code using a `NetworkDefinition` instance. 

Some [examples](https://github.com/kuujo/vertigo/tree/master/examples/complex)
demonstrate how the network definition API works.

To define a network, create a new `NetworkDefinition` instance either using
the `Networks` API or instantiating a definition instance directly.

```java
NetworkDefinition network = Networks.createNetwork("test");
```

Each network must be given a *unique* name. Vertigo component addresses are
generated in a predictable manner, and this name is used to prefix all
component addresses and instance addresses.

### Defining network components
The `NetworkDefinition` class provides several methods for adding components
to the network.

* `from(ComponentDefinition component)`
* `fromVerticle(String name)`
* `fromVerticle(String name, String main)`
* `fromVerticle(String name, String main, JsonObject config)`
* `fromVerticle(String name, String main, int instances)`
* `fromVerticle(String name, String main, JsonObject config, int instances)`
* `fromModule(String name)`
* `fromModule(String name, String moduleName)`
* `fromModule(String name, String moduleName, JsonObject config)`
* `fromModule(String name, String moduleName, int instances)`
* `fromModule(String name, String moduleName, JsonObject config, int instances)`

Note that Vertigo supports both verticles and modules as network components.
The return value of each of these methods is a new `ComponentDefinition` instance
on which you can set the following properties:

* `setType` - sets the component type, *verticle* or *module*
* `setMain` - sets a verticle main
* `setModule` - sets a module name
* `setConfig` - sets the component configuration. This is made available within
  component verticles via the instance's `WorkerContext`
* `setWorkers` - sets the number of component workers
* `groupBy` - sets the component grouping, see [groupings](#groupings)
* `filterBy` - adds a component filter, see [filters](#filters)

### Defining connections
Connections between components are created by `toVerticle` and `toModule`
instances on `ComponentDefinition` objects. By calling one of the `to*` methods,
a connection from one component to the new component is implicitly created,
and a new component definition is returned. These methods follow the same
interface as the `fromVerticle` and `fromModule` methods on the `NetworkDefinition`
class.

* `to(ComponentDefinition component)`
* `toVerticle(String name)`
* `toVerticle(String name, String main)`
* `toVerticle(String name, String main, JsonObject config)`
* `toVerticle(String name, String main, int instances)`
* `toVerticle(String name, String main, JsonObject config, int instances)`
* `toModule(String name)`
* `toModule(String name, String moduleName)`
* `toModule(String name, String moduleName, JsonObject config)`
* `toModule(String name, String moduleName, int instances)`
* `toModule(String name, String moduleName, JsonObject config, int instances)`

### Network structures
Vertigo places *no limits* on network structures. The definitions API is
designed to support any network topology including circular structures
(which are in fact required for [remote-procedure calls](#executors)).

### Defining remote procedure calls
Remote procedure calls in Vertigo work by creating circular topologies.
When an executor executes a message, the message is tagged with a unique
ID and the executor waits for a message with the same ID to make its return.
Thus, the network must eventually lead back to the executor component.

To create a connection back to the original executor, you must store the
executor definition in a variable and then pass the definition to the `to()`
method of a component instance.

```java
NetworkDefinition network = Networks.createNetwork("rpc");
ComponentDefinition executor = network.fromVerticle("executor", "executor.py");
executor.toVerticle("sum", "com.mycompany.myproject.SumVerticle").to(executor);
```

## Network deployment
Once you have defined a network using the definition API, the network can
be deployed via the `Cluster` API. Vertigo provides two types of deployment
methods via `LocalCluster` and `ViaCluster`. Each implement the `Cluster`
interface:

* `deploy(NetworkDefinition network)`
* `deploy(NetworkDefinition network, Handler<AsyncResult<NetworkContext>> doneHandler)`
* `shutdown(NetworkContext context)`
* `shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler)`

When a network is deployed successfully, a `NetworkContext` instance may
be returned if a `doneHandler` was provided. The `NetworkContext` instance
contains information about the network components, including component
definitions, addresses, and connections.

```java
final Cluster cluster = new LocalCluster(vertx, container);
cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
  public void handle(AsyncResult<NetworkContext> result) {
    if (result.succeeded()) {
      NetworkContext context = result.result();
      cluster.shutdown(context);
    }
  }
});
```

## Clustering
Vertigo supports distributing network components across multiple Vert.x
instances using [Via](https://github.com/kuujo/via), a distributed
deployment framework for Vert.x (Via was specifically developed for Vertigo).
Following the same `Cluster` API, Via will handle assigning component
instances to various Vert.x instances within a cluster in a predictable
manner. Users can optionally specify custom Via *schedulers* in order to
control component assignments.

See the [Via documentation](https://github.com/kuujo/via) for more information
on clustering with Vertigo.
