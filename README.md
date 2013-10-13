Vert.igo
========

**[The Vertigo Python API has moved](https://github.com/kuujo/vertigo-python)**

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
  **random, round-robin, consistent hashing, or fanout** approaches
* Supports **distribution of verticle/modules instances across a cluster** of Vert.x
  instances
* **Monitors networks for failures** and automatically reassigns/redeploys failed
  verticles and modules
* Network components can be written in **any Vert.x supported language**, with
  current integration for Java and [Python](https://github.com/kuujo/vertigo-python)
* Integrates seemlessly with existing Vert.x applications

Vertigo is not a replacement for [Storm](https://github.com/nathanmarz/storm).
Rather, Vertigo is a lightweight alternative that is intended to be embedded
within larger Vert.x applications.

## Java User Manual

1. [Networks](#networks)
1. [Components](#components)
1. [Messaging](#messaging)
   * [How components communicate](#how-components-communicate)
   * [Message distribution](#message-distribution)
   * [Messages](#messages)
1. [Reliability](#reliability)
   * [Component supervision](#component-supervision)
   * [Message acking](#message-acking)
   * [How acking works](#how-acking-works)
1. [A Simple Network](#a-simple-network)
   * [Defining the network](#defining-the-network)
   * [Creating the feeder](#creating-the-feeder)
   * [Creating the worker](#creating-the-worker)
   * [Deploying the network](#deploying-the-network)
   * [Executing the network as a remote procedure](#executing-the-network-as-a-remote-procedure)
   * [The complete network](#the-complete-network)
1. [Creating Components](#creating-components)
   * [Contexts](#contexts)
      * [WorkerContext](#workercontext)
      * [ComponentContext](#componentcontext)
      * [NetworkContext](#networkcontext)
   * [Feeders](#feeders)
     * [BasicFeeder](#basic-feeder)
     * [PollingFeeder](#polling-feeder)
     * [StreamFeeder](#stream-feeder)
   * [Workers](#workers)
      * [JsonMessage](#jsonmessage)
      * [Emitting messages](#emitting-messages)
      * [Acking messages](#acking-messages)
   * [Executors](#executors)
1. [Defining networks](#defining-networks)
   * [Defining network components](#defining-network-components)
   * [Defining connections](#defining-connections)
   * [Component Groupings](#component-groupings)
   * [Component Filters](#component-filters)
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

### Message distribution
When messages are sent between components which have multiple instances running
within a network, Vertigo can manage distribution of messages between component
instances. To do this, Vertigo provides a *grouping* abstraction that allows
users to define how messages are dispatched to a set of component instances.
For instance, one component may require messages to be distributed among its
instances in a round-robin fashion, while another may require a consisten
hashing based approach. [Vertigo provides numerous component groupings for
different scenarios](#component-groupings).

### Messages
Messages are sent over the event bus in the form of `JsonObject` instances.
Just as Vertigo networks have a structure, Vertigo messages can have structure
as well: trees. Messages can be emitted from components either as individuals
or as children of other messages. This hierarchical system integrates with the
[message acking](#message-acking) system to provide increased reliability -
acking for entire message trees, not just individual messages.

Vertigo messages also contain a number of metadata fields in addition to
the message body. These fields describe things like where the message came
from, who the message's parents and ancestors are, and other interesting
information. [Read more about message fields here](#jsonmessage)

## Reliability
Vertigo provides a number of reliability features that help ensure networks
continue to run and messages are never lost.

### Component supervision
When a Vertigo network is deployed, a special *coordinator* verticle is
deployed as well. It is *coordinator's* task to ensure that all component
instances continue to run smoothly. To do so, component instances connect
to the *coordinator* verticle, receive a unique heartbeat address, and begin
sending heartbeat messages to the coordinator. If a component fails to send
a heartbeat within a certain amount of time, the component is assumed to be
dead and will be automatically re-deployed.

This concept holds true for both local and clustered Vertigo networks. In
the case of using [Via](https://github.com/kuujo/via) for clustering, the
*coordinator* verticle will simply re-deploy failed component verticles
or modules using the Via API, resulting in the component being assigned
to a new machine.

### Message acking
In addition to the *coordinator* verticle being deployed for each Vertigo
network, another special verticle called the *auditor* verticle is deployed,
as well. The Vertigo *auditor* is tasked with monitoring messages within the
network, tracking acks and nacks throughout the network, and notifying
*feeders* when a message tree fails.

![Network Auditor](http://s14.postimg.org/kkl297qo1/vertigo_acker.png)

Each network may have any number of auditor verticles (this is configurable
via the network definition).

#### How acking works
When a component creates and emits a new message, the message will be
assigned an *auditor* verticle (each auditor for any given network has
a unique event bus address). Any time the message or a descendent message
is emitted, acked, or failed from a component, the assigned *auditor*
will be sent a message notifying it of the change. A source message
can potentially have thousands of messages created based on its data,
and Vertigo tracks all of the messages that originate from a source
message. If a message or one of its descendents is failed or times
out at any point within the network, the original source will be
notified immediately. Internally, the auditor maintains a record of the
entire message tree structure, and only once all of the messages have
been acked will it send a message back to the original data source (the
component that created the first message). In this way, Vertigo
tracks a single message's transformation - no matter how complex -
to completion before notifying the data source.

## A simple network
In order to get a better understanding of the concepts introduced in
Vertigo, let's take a look at a simple network example.

### Defining the network
Vertigo networks are defined using the [definitions](#defining-networks)
API.

```java
NetworkDefinition network = Networks.createNetwork("word_count");
network.fromVerticle("word_feeder", WordFeeder.class.getName())
  .toVerticle("word_counter", WordCountWorker.class.getName(), 4).groupBy(new FieldsGrouping("word"));
```

This network definition defines a simple network that consists of only two
verticles, a feeder and a worker. First, we define a new network named *word_count*.
Each vertigo network should have a unique name which is used to derive each
of the network's *component* instances unique addresses (Vertigo addresses are
generated in a predictable manner). With the new network defined, we add a
*source* verticle to the network - this is usually a feeder or executor, though
workers can be used in more complex cases - which connects to a *worker* verticle.
In Vertigo, each of the elements is known as a *component* - either a verticle
or module - each of which can have any number of instances. In this case, the
*word_counter* component has four instances. This means that Vertigo will deploy
four instances of the `WordCountWorker` verticle when deploying the network.

Finally, we group the `word_counter` component using a `FieldsGrouping`. Because
this particular component counts the number of words arriving to it, the same
word *must always go to the same component instance*, and the `FieldsGrouping`
is the element of the definition that guarantees this will happen. Groupings are
used to essentially define how messages are distributed among multiple instances
of a component.

### Creating the feeder
Now let's look at how the feeder emits messages. First, to create a [feeder](#feeders)
component we need to extend the `VertigoVerticle`.

```java
public class WordFeeder extends VertigoVerticle {
  public void start() {
    vertigo.createPollingFeeder().start(new Handler<AsyncResult<PollingFeeder>>() {
      public void handle(AsyncResult<PollingFeeder> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          PollingFeeder feeder = result.result();
        }
      }
    });
  }
}
```

Here we extend the special `VertigoVerticle` class which makes the `vertigo`
member available to us. From this we create and start a new `PollingFeeder` instance.
The polling feeder allows us to feed data to the network whenever the feeder's
internal feed queue is not full. Vertigo provides several [feeders](#feeders) for
different use cases.

Once the feeder has started, we can begin feeding data.

```java
feeder.feedHandler(new Handler<PollingFeeder>() {
  public void handle(PollingFeeder feeder) {
    String[] words = new String[]{"apple", "banana", "peach", "pear", "orange"};
    Random random = new Random();
    String word = words[random.nextInt(words.length)];
    JsonObject data = new JsonObject().putString("word", word);
    feeder.feed(data);
  }
});
```

Here we feed a random word from a list of words to the network. But what if
the data fails to be processed? How can we be notified? The Vertigo feeder
API provides for additional arguments that allow the feeder to be notified
once a message is successfully processed.
[See what successfully processing means](#how-acking-words)

```java
feeder.feedHandler(new Handler<PollingFeeder>() {
  public void handle(PollingFeeder feeder) {
    String[] words = new String[]{"apple", "banana", "peach", "pear", "orange"};
    Random random = new Random();
    String word = words[random.nextInt(words.length)];
    JsonObject data = new JsonObject().putString("word", word);
    feeder.feed(data, new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          container.logger().warn("Failed to process message.");
        }
        else {
          container.logger().info("Successfully processed message.");
        }
      }
    });
  }
});
```

By providing an additional handler to the `feed()` method, the feeder will
now be notified once the message is acked or failed.

### Creating the worker
Now that we have a feeder to feed messages to the network, we need
to implement a [worker](#workers). Workers are the primary units of processing in
Vertigo. In this case, we're creating a worker that counts the occurences
of words in the `word` field of the message body.

Creating and starting workers is done in the same was as with the feeder.

```java
public class WordCountWorker extends VertigoVerticle {

  private Map<String, Integer> counts = new HashMap<String, Integer>();

  @Override
  public void start() {
    vertigo.createWorker().start(new Handler<AsyncResult<Worker>>() {
      @Override
      public void handle(AsyncResult<Worker> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          final Worker worker = result.result();
        }
      }
    });
  }
}
```

Once we have created a worker, we need to add a handler for incoming
messages. To do this we call the `messageHandler` method.

```java
worker.messageHandler(new Handler<JsonMessage>() {
  @Override
  public void handle(JsonMessage message) {
    String word = message.body().getString("word");
    Integer count = counts.get(word);
    if (count == null) {
      count = 0;
    }
    count++;
  }
});
```

Once we're done processing the message, we may want to emit the new
count to any other components that may be listening. To do so, we call
the `emit()` method on the `Worker` instance.

```java
worker.emit(new JsonObject().putString("word", word).putNumber("count", count), message);
```

Once a message has been fully processed, it is essential that the
message be acked. This notifies the network that the message has been
fully processed, and once all messages in a message tree have been
processed the original data source (the feeder above) will be notified.

```java
worker.ack(message);
```

### Deploying the network
Now that we've created the network and implemented each of its components,
we need to deploy the network. Vertigo supports both local and clustered
network deployments using the `LocalCluster` and `ViaCluster` (see
[Via](https://github.com/kuujo/via)) interfaces. In this case, we'll just
use the `LocalCluster`.

To deploy the network, we just pass our network definition to a cluster
instance's `deploy()` method.

```java
NetworkDefinition network = Networks.createNetwork("word_count");
network.fromVerticle("word_feeder", WordFeeder.class.getName())
  .toVerticle("word_counter", WordCountWorker.class.getName(), 4).groupBy(new FieldsGrouping("word"));

final Cluster cluster = new LocalCluster(vertx, container);
cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
  @Override
  public void handle(AsyncResult<NetworkContext> result) {
    if (result.failed()) {
      container.logger().error(result.cause());
    }
    else {
      final NetworkContext context = result.result();
      vertx.setTimer(5000, new Handler<Long>() {
        @Override
        public void handle(Long timerID) {
          cluster.shutdown(context);
        }
      });
    }
  }
});
```

The `NetworkContext` that is returned by the deployment contains
valuable information about the network. See [contexts](#contexts)
for more info.

### Executing the network as a remote procedure
But what if the feeder needs to receive feedback on the word count? In this
case, we can replace the feeder with an [executor](#executors). Executors
work by capitalizing on circular connections within networks. Thus, first
we need to re-define the network to create a circular connection between
the data source and the worker.

```java
NetworkDefinition network = Networks.createNetwork("word_count");
ComponentDefinition executor = network.fromVerticle("word_executor", WordExecutor.class.getName());
executor.toVerticle("word_counter", WordCountWorker.class.getName(), 4).groupBy(new FieldsGrouping("word"))
  .to(executor);
```

First, we store the `word_executor` component definition in a variable, connect
it to the `word_counter`, and then connect the `word_counter` back to the
`word_executor`. This creates the circular connections that are required for
remote procedure calls.

Now that we have our circular connections, we can re-define the original feeder
to expect a result using an executor.

```java
vertigo.createBasicExecutor().start(new Handler<AsyncResult<BasicExecutor>>() {
  @Override
  public void handle(AsyncResult<BasicExecutor> result) {
    if (result.failed()) {
      container.logger().error(result.cause());
    }
    else {
      BasicExecutor executor = result.result();
      String[] words = new String[]{"apple", "banana", "peach", "pear", "orange"};
      Random random = new Random();
      while (!executor.queueFull()) {
        String word = words[random.nextInt(words.length)];
        JsonObject data = new JsonObject().putString("word", word);
        executor.execute(data, new Handler<AsyncResult<JsonMessage>>() {
          @Override
          public void handle(AsyncResult<JsonMessage> result) {
            if (result.failed()) {
              container.logger().warn("Failed to process message.");
            }
            else {
              container.logger().info("Current word count is " + result.result().body().getInteger("count"));
            }
          }
        }
      });
    }
  }
});
```

Note that because the `word_counter` component always emits data, it does
not have to be refactored to support remote procedure calls. For this reason,
all component implementations *should always emit data* whether they may or
may not be connected to any other component. If the component is not, in fact,
connected to any other component, `emit`ing data will have no effect.

### The complete network
Finally, we have our complete Java network example.

```java
import java.util.HashMap;
import java.util.Map;

import net.kuujo.vertigo.Cluster;
import net.kuujo.vertigo.LocalCluster;
import net.kuujo.vertigo.Networks;
import net.kuujo.vertigo.component.feeder.BasicFeeder;
import net.kuujo.vertigo.component.worker.Worker;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.definition.ComponentDefinition;
import net.kuujo.vertigo.definition.NetworkDefinition;
import net.kuujo.vertigo.grouping.FieldsGrouping;
import net.kuujo.vertigo.java.VertigoVerticle;
import net.kuujo.vertigo.messaging.JsonMessage;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * A word counting network.
 */
public class WordCountNetwork extends Verticle {

  /**
   * Feeds a random word to the network.
   */
  public static class WordFeeder extends VertigoVerticle {

    private String[] words = new String[]{"apple", "banana", "peach", "pear", "orange"};

    @Override
    public void start() {
      vertigo.createPollingFeeder().start(new Handler<AsyncResult<PollingFeeder>>() {
        @Override
        public void handle(AsyncResult<PollingFeeder> result) {
          if (result.failed()) {
            container.logger().error(result.cause());
          }
          else {
            PollingFeeder feeder = result.result();
            feeder.feedHandler(new Handler<PollingFeeder>() {
              @Override
              public void handle(PollingFeeder feeder) {
                Random random = new Random();
                String word = words[random.nextInt(words.length)];
                JsonObject data = new JsonObject().putString("word", word);
                feeder.feed(data, new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      container.logger().warn("Failed to process message.");
                    }
                    else {
                      container.logger().info("Successfully processed message.");
                    }
                  }
                });
              }
            });
          }
        }
      });
    }

  }

  /**
   * Counts words incoming to the worker.
   */
  public static class WordCountWorker extends VertigoVerticle {

    private Map<String, Integer> counts = new HashMap<String, Integer>();

    @Override
    public void start() {
      vertigo.createWorker().start(new Handler<AsyncResult<Worker>>() {
        @Override
        public void handle(AsyncResult<Worker> result) {
          if (result.failed()) {
            container.logger().error(result.cause());
          }
          else {
            final Worker worker = result.result();
            worker.messageHandler(new Handler<JsonMessage>() {
              @Override
              public void handle(JsonMessage message) {
                String word = message.body().getString("word");
                Integer count = counts.get(word);
                if (count == null) {
                  count = 0;
                }
                count++;
                worker.emit(new JsonObject().putString("word", word).putNumber("count", count));
                worker.ack(message);
              }
            });
          }
        }
      });
    }
  }

  @Override
  public void start() {
    NetworkDefinition network = Networks.createNetwork("word_count");
    network.fromVerticle("word_feeder", WordFeeder.class.getName())
      .toVerticle("word_counter", WordCountWorker.class.getName(), 4).groupBy(new FieldsGrouping("word"));

    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.failed()) {
          container.logger().error(result.cause());
        }
        else {
          final NetworkContext context = result.result();
          vertx.setTimer(5000, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
              cluster.shutdown(context);
            }
          });
        }
      }
    });
  }

}
```

## Creating components
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

#### WorkerContext
The `WorkerContext` exposes the following interface:
* `address()` - the unique worker event bus address
* `config()` - the worker configuration - this is inherited from the component definition
* `getComponentContext()` - returns the parent component context

#### ComponentContext
The `ComponentContext` exposes the following interface:
* `address()` - the component address - this is the basis for all component instance addresses
* `getConnectionContexts()` - a collection of `ConnectionContext` instances that
  describe the addresses to which this component feeds/emits messages and their
  component [filters](#component-filters) and [groupings](#component-groupings)
* `getWorkerContexts()` - a collection of all component instance contexts for the component
* `getDefinition()` - the component [definition](#defining-network-components)
* `getNetworkContext()` - returns the parent network context

#### NetworkContext
The `NetworkContext` exposes the following interface:
* `address()` - the network address - this is the basis for all component addresses
* `getBroadcastAddress()` - the network broadcast address - this is the event bus
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
* `getMaxQueueSize()` - gets the maximum feed queue size
* `queueFull()` - indicates whether the feed queue is full
* `setAutoRetry(boolean retry)` - sets whether to automatically retry sending
  [failed](#message-acking) messages
* `isAutoRetry()` - indicates whether auto retry is enabled
* `setRetryAttempts(int attempts)` - sets the number of retries to attempt
  before explicitly failing the feed. To set an infinite number of retry
  attempts pass `-1` as the `attempts` argument
* `getRetryAttempts()` - indicate the number of automatic retry attempts

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
* `getFeedDelay()` - indicates the feed delay

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
Each worker must have a handler assigned to it for handling incoming messages.
To attach a handler to a `Worker`, call the `messageHandler()` method. To
start a worker, call the `start()` method, passing an asynchronous handler
to be invoked once the worker has been registered on the event bus.

```java
vertigo.createWorker().start(new Handler<AsyncResult<Worker>>() {
  public void handle(AsyncResult<Worker> result) {
    if (result.succeeded()) {
      final Worker worker = result.result();
      worker.messageHandler(new Handler<JsonMessage>() {
        public void handle(JsonMessage message) {
          // Handle the message.
        }
      });
    }
  }
});
```

#### JsonMessage
When a worker receives data, it receives it in the form of a `JsonMessage`
instance. This special message wrapper provides an API that exposes more
information about a message than just data. Vertigo messages can be organized
into message trees with relationships between messages. The `JsonMessage`
provides metadata about the message and its relationship to other messages
with the following methods:
* `id()` - the unique message identifier
* `body()` - the message body, a `JsonObject` instance
* `tag()` - the message tag
* `source()` - the `name` of the component from which this message tree originated
* `parent()` - the parent message's unique identifier
* `ancestor()` - the original message's unique identifier, the identifier
  of the source of the message tree
* `auditor()` - the address of the auditor for this message's message tree

```java
worker.messageHandler(new Handler<JsonMessage>() {
  public void handle(JsonMessage message) {
    JsonArray words = message.body().getArray("words");
  }
});
```

#### Emitting messages
Each worker component can both receive and emit messages. Of course, where
a message goes once it is emitted from a worker instance is abstracted from
the implementation, and the `Worker` interface behaves accordingly. The
`Worker` exposes the following methods for emitting messages
* `emit(JsonObject data)` - emits a message body
* `emit(JsonObject data, String tag)` - emits a message body with a tag
* `emit(JsonObject data, JsonMessage parent)` - emits a message body as
  a child of the given `JsonMessage` instance
* `emit(JsonObject data, String tag, JsonMessage parent)` emits a message body
  with a tag as a child of the given `JsonMessage` instance

```java
worker.messageHandler(new Handler<JsonMessage>() {
  public void handle(JsonMessage message) {
    JsonArray words = message.body().getArray("words");
    worker.emit(new JsonObject().putNumber("count", words.size()));
  }
});
```

Note that hierarchical message relationships are created by passing a parent
message to the `emit()` method. When this type of relationship is created, the
message tree's assigned `auditor` is notified of the new relationship. What this
relationship means is that the message `source` will not be notified of a
message's successful completion until *all messages in the message tree* have
been completely acked. However, if a message is *failed* then the message `source`
will be notified immediately.

#### Acking messages
Vertigo provides for reliable messaging between network components using acks
and fails. *Each message that is received by a worker component must be acked
or failed*, otherwise the message tree will eventually be failed via timeout.
The `Worker` provides the following methods for acks/fails:
* `ack(JsonMessage message)` - indicates that a message has been successfully
  processed
* `fail(JsonMessage message)` - indicates that a message has failed processing.
  This can be used as a vehicle for notifying data sources of invalid data

```java
worker.messageHandler(new Handler<JsonMessage>() {
  public void handle(JsonMessage message) {
    JsonArray words = message.body().getArray("words");
    if (words != null) {
      // This is a valid message.
      worker.emit(new JsonObject().putNumber("count", words.size()));
      worker.ack(message);
    }
    else {
      // This is an invalid message.
      worker.fail(message);
    }
  }
});
```

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

The `NetworkDefinition` exposes the following configuration methods:
* `setAddress(String address)` - sets the network address, this is the basis
  for all generated network addresses and is synonymous with the network `name`
* `enableAcking()` - enables acking for the network
* `disableAcking()` - disabled acking for the network
* `isAckingEnabled()` - indicates whether acking is enabled for the network
* `setNumAuditors(int numAuditors)` - sets the number of network auditors (ackers)
* `getNumAuditors()` - indicates the number of network auditors
* `setAckExpire(long expire)` - sets the message ack expiration for the network
* `getAckExpire()` - indicates the ack expiration for the network

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

* `setType(String type)` - sets the component type, *verticle* or *module*
  Two constants are also available, `ComponentDefinition.VERTICLE` or
  `ComponentDefinition.MODULE`
* `setMain(String main)` - sets a verticle main
* `setModule(String moduleName)` - sets a module name
* `setConfig(JsonObject config)` - sets the component configuration. This is made available within
  component verticles via the instance's `WorkerContext`
* `setWorkers(int numWorkers)` - sets the number of component workers
* `groupBy(Grouping grouping)` - sets the component grouping, see [groupings](#component-groupings)
* `filterBy(Filter filter)` - adds a component filter, see [filters](#component-filters)

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

### Component Groupings
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
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).groupBy(new RandomGrouping());
```

* `RoundGrouping` - component instances receive messages in round-robin fashion
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).groupBy(new RoundGrouping());
```

* `FieldsGrouping` - component instances receive messages according to basic
  consistent hashing based on a given field
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).groupBy(new FieldsGrouping("type"));
```

* `AllGrouping` - all component instances receive a copy of each message
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).groupBy(new AllGrouping());
```

### Component Filters
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
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).filterBy(new TagsFilter("product"));
```

* `FieldFilter` - filters messages according to a field/value
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).filterBy(new FieldFilter("type", "product"));
```

* `SourceFilter` - filters messages according to the source component name
```java
NetworkDefinition network = Networks.createNetwork("foo");
network.fromVerticle("bar", "com.mycompany.myproject.MyFeederVerticle")
  .toVerticle("baz", "some_worker.py", 2).filterBy(new SourceFilter("rabbit"));
```

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
