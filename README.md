Vert.igo
========

#### [The Vertigo networks API has changed!](#the-vertigo-api-has-changed)

Vertigo is a distributed event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Following a concept and
structure similar to [Storm](https://github.com/nathanmarz/storm), Vertigo
allows real-time problems to be broken down into smaller tasks (as Vert.x
verticles) and distributed across **one or many Vert.x instances**, managing
communication between components in a **predictable and reliable** manner.

**For more information on how Vertigo works see [how it works](#how-it-works)**

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
  APIs being developed for [Javascript](https://github.com/kuujo/vertigo-js)
  and [Python](https://github.com/kuujo/vertigo-python)
* Integrates seemlessly with existing Vert.x applications

Vertigo is not a replacement for [Storm](https://github.com/nathanmarz/storm).
Rather, Vertigo is a lightweight alternative that is intended to be embedded
within larger Vert.x applications.

#### [Java User Manual](#java-user-manual) | [Javadoc](http://vertigo.kuujo.net/java/)
#### [How it works](#how-it-works)
#### The Vertigo [Javascript API](https://github.com/kuujo/vertigo-js) is under development
#### The Vertigo [Python API](https://github.com/kuujo/vertigo-python) is under development

### The Vertigo API has changed!
After weeks of careful consideration and tireless refactoring efforts,
the Vertigo networks API has changed. Note that this does *not* effect
any of the component implementations (aside from imports). See the
following rational...

#### Rationale for API changes
The original Vertigo network definitions API had a number of flaws that
were ultimately significant enough that it warranted a change. The issue
was that the network definitions API forced networks to be rigid in
structure. Each component defined who it would send messages to, and once
those components were started that fact could not change. This fact ultimately
conflicted with several planned project features. What the project needed
was a minor overhaul of how components connect to and communicate with
each other.

First, the network definitions (now simply *networks*) had to be refactored
so to represent components *subscribing* to addresses from which they wanted
to receive messages rather than explicitly *indicatis* components to which
they would send messages. A side benefit of this was cleaning up the
underlying JSON network definition structure. So, with the new Networks
API, components are added to a network using the `addVerticle`, `addModule`,
and `addComponent` methods.

```java
Network network = new Network("test");
network.addVerticle("test.first_verticle", FirstVerticle.class.getName());
```

After changing replacing the `fromVerticle` and `fromModule` methods, I
needed a new way to represent subscriptions to other addresses. For this,
I added an `Input` abstraction. [Inputs](#inputs) simply represent subscriptions
from one network component to another. Each input may define a
[grouping](#groupings) and any number of [filters](#filters). The result
of this is that rather than applying groupings and filters to components,
they can now be applied to specific connections between components.

```java
network.addVerticle("test.second_verticle", SecondVerticle.class.getName())
  .addInput("test.first_verticle").groupBy(new RoundGrouping());
```

This API allows components to be stripped down to mere event bus addresses,
meaning any code that can access the event bus can potentially access the
output of a component.

Another limitation of the existing network definitions API was that it
could not easily support sending custom filter and grouping information
over the event bus. Thus, I created a "serialization" class that supports
serializing any filter or grouping implementation to a Vert.x `JsonObject`
instance and sending it over the event bus as part of any input definition.

With the new Networks API, I needed a way to translate the new network
and component definition structures to reality. Creating a communcation
system based on publish-subscribe mechanisms rather than explicit fixed
connections between components opens up a host of possibilities (see
the new [advanced features](#advanced-features) section). But it also
meant a major refactoring of the underlying communication components was
in order.

The primary component of the new communication structure was a publish-subscribe
mechanism. When a network is defined, each component is assigned its own
event bus address. Each instance of a component needed to register a handler
at this address to listen for interested components. For this, I created
an `OutputCollector`. [Output collectors](#output-collectors) listen for
`listen` messages from any interested sources and register those messages
as new outputs of the component. Ultimately, output collectors became the
primary interface for *emitting* messages from any component.

With output collectors listening for subscribers in each component instance,
I needed a method for interested components to subscribe by sending messages
to output collectors. For this, I created two new interfaces, the
`InputCollector` and `Listener`. Underlying each [input collector](#input-collectors)
may be many [listeners](#listeners), and listeners are the components that
send `listen` messages and await output from a specific address. These
APIs became the primary interfaces for *receiving* messages emitted by
other components.

With changes to the method of communication between components,
details of where any given component is sending messages have been completely
abstracted from that component. But this means that the component doesn't
have any way of determining when all relevant parties have started listening.
It's important that a network not start before all components have been
set up - and handlers have been registered on the event bus - lest messages
be lost due to the fact that components weren't listening. Thus, network
[coordinators](#coordinators) have been refactored to be notified once
all components of a network had been set up. This means that only once
all components of a network have been set up and indicated as much will
the network start.

Ultimately, this refactoring of Vertigo communication methods opens up
a host of possibilities - see [wire taps](#wire-taps) and
[nested networks](#nested-networks). Network communication is no longer
beholden to a fixed structure defined at the point of deployment. This
means that network structures can potentially be updated with no down
time, adding and removing components in real-time.

# Java User Manual

1. [Concepts](#concepts)
   * [Networks](#networks)
   * [Components](#components)
1. [A Simple Network](#a-simple-network)
   * [Defining the network](#defining-the-network)
   * [Creating the feeder](#creating-the-feeder)
   * [Creating the worker](#creating-the-worker)
   * [Deploying the network](#deploying-the-network)
   * [Executing the network as a remote procedure](#executing-the-network-as-a-remote-procedure)
   * [The complete network](#the-complete-network)
1. [Creating Components](#creating-components)
   * [Contexts](#contexts)
      * [InstanceContext](#instancecontext)
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
      * [BasicExecutor](#basic-executor)
      * [PollingExecutor](#polling-executor)
      * [StreamExecutor](#stream-executor)
1. [Creating networks](#creating-networks)
   * [Adding components](#adding-components)
   * [Adding inputs](#adding-inputs)
   * [Input groupings](#input-groupings)
   * [Input filters](#input-filters)
   * [Network structures](#network-structures)
   * [Remote procedure calls](#defining-remote-procedure-calls)
   * [Defining networks in JSON](#defining-networks-in-json)
   * [Instantiating networks from JSON](#instantiating-networks-from-json)
1. [Network deployment](#network-deployment)
   * [Deploying networks locally](#deploying-networks-locally)
   * [Deploying networks across a cluster](#deploying-networks-across-a-cluster)
1. [Events](#events)
   * [Network events](#network-events)
   * [Component events](#component-events)
1. [Advanced features](#advanced-features)
   * [Wire taps](#wire-taps)
   * [Nested networks](#nested-networks)

## Concepts

### Networks
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

### Components
A component represents a single vertex in a Vertigo network graph. Each network
may contain any number of components, and each component may have any number of
instances running within the network (each of which may be assigned to different
machines around a [cluster](#network-deployment)). Within the context of Vert.x
a component can be defined as a verticle that may receive messages from zero or
many verticles and send messages to one or many verticles. What happens within the
verticle depends entirely where they appear in a network graph and how the component
is implemented. Vertigo provides several component implementations.

## A simple network
In order to get a better understanding of the concepts introduced in
Vertigo, let's take a look at a simple network example.

### Defining the network
Vertigo networks are defined using the [networks](#creating-networks)
API.

```java
Network network = new Network("word_count");
network.addVerticle("word_count.word_feeder", WordFeeder.class.getName());
network.addVerticle("word_count.word_counter", WordCountWorker.class.getName(), 4)
  .addInput("word_count.word_feeder").groupBy(new FieldsGrouping("word"));
```

This code defines a simple network that consists of only two verticles, a
feeder and a worker. First, we define a new network named *word_count*.
This is the address that will be used by the network coordinator once the
network is deployed.

Next, we add the *word_feeder* component to the network. Components may be
either verticles or modules, with `addVerticle` and `addModule` methods for
adding each type respectively. The first argument to any `addVerticle` or
`addModule` method is the component address. *Note that this is actually
the event bus address to which other components may connect to receive
messages from the component, so it's important that this name does not
conflict with other addresses on the event bus.*

Next, we add the *word_counter* verticle, which will count words. After the word
counter is added we indicate that the word counter should receive messages
from the component at *word_count.word_feeder* by adding an input from
that address. This means that once the network is started, each *word_count*
instance will notify all instances of the *word_feeder* component that it
is interested in receiving messages emitted by the *word_feeder*.

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
[See what successfully processing means](#how-acking-works)

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
Network network = new Network("word_count");
network.fromVerticle("word_feeder", WordFeeder.class.getName())
  .toVerticle("word_counter", WordCountWorker.class.getName(), 4)
  .groupBy(new FieldsGrouping("word"));

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
Network network = new Network("word_count");
network.addVerticle("word_count.word_executor", WordExecutor.class.getName())
  .addInput("word_count.word_counter");
network.addVerticle("word_count.word_counter", WordCountWorker.class.getName(), 4)
  .addInput("word_count.word_executor", new FieldsGrouping("word"));
```

Here we simply add the word counter's address as an input to the word executor,
thus creating the circular connection.

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

import net.kuujo.vertigo.VertigoVerticle;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.feeder.BasicFeeder;
import net.kuujo.vertigo.worker.Worker;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.input.grouping.FieldsGrouping;
import net.kuujo.vertigo.message.JsonMessage;

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
    Network network = new Network("word_count");
    network.addVerticle("word_count.word_feeder", WordFeeder.class.getName());
    network.addVerticle("word_count.word_counter", WordCountWorker.class.getName(), 4)
      .addInput("word_count.word_feeder", new FieldsGrouping("word"));

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
* `context` - a `InstanceContext` object

The `Vertigo` interface is similar to that Vert.x's `Vertx` interface in that
it is essentially a helper for creating Vertigo components. The `Vertigo` object
exposes the following methods:
* `createFeeder()` - creates a [basic feeder](#basic-feeder)
* `createBasicFeeder()` - creates a [basic feeder](#basic-feeder)
* `createPollingFeeder()` - creates a [polling feeder](#polling-feeder)
* `createStreamFeeder()` - creates a [stream feeder](#stream-feeder)
* `createExecutor()` - creates a [basic executor](#basic-executor)
* `createBasicExecutor()` - creates a [basic executor](#basic-executor)
* `createPollingExecutor()` - creates a [polling executor](#polling-executor)
* `createWorker()` - creates a [worker](#workers)

### Contexts
The `InstanceContext` object contains information relevant to the current component
instance, as well as its parent component definition and even information about
the entire network layout, including unique addresses for each network component
instance.

#### InstanceContext
The `InstanceContext` exposes the following interface:
* `getAddress()` - the unique worker event bus address
* `getComponent()` - returns the parent component context

#### ComponentContext
The `ComponentContext` exposes the following interface:
* `getAddress()` - the component address - this is the basis for all component instance addresses
* `getType()` - the component type, either "module" or "verticle"
* `isModule()` - indicates whether the component is a module
* `isVerticle()` - indicates whether the component is a verticle
* `getConfig()` - the component configuration
* `getNumInstaces()` - the number of component instances
* `getInputs()` - a list of component inputs
* `getInstaces()` - a list of component instance contexts
* `getNetwork()` - the parent network context

#### NetworkContext
The `NetworkContext` exposes the following interface:
* `getAddress()` - the network address - this is the basis for all component addresses
* `getBroadcastAddress()` - the network broadcast address - this is the event bus
  address used by network [auditors](#auditors) to broadcast message statuses (acks/nacks)
* `getAuditors()` - returns a set of network [auditor](#auditors) addresses, each auditor is
  assigned its own unique event bus address
* `getComponents()` - a collection of all network component contexts

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
  [failed](#auditors) messages
* `isAutoRetry()` - indicates whether auto retry is enabled
* `setRetryAttempts(int attempts)` - sets the number of retries to attempt
  before explicitly failing the feed. To set an infinite number of retry
  attempts pass `-1` as the `attempts` argument
* `getRetryAttempts()` - indicate the number of automatic retry attempts

To start a feeder, call the `start()` method:
* `start(Handler<AsyncResult<BasicFeeder>> startHandler)`

Once a feeder has been started, you can feed messages using the `feed()` method:
* `feed(JsonObject body)`
* `feed(JsonObject body, String tag)`
* `feed(JsonObject body, Handler<AsyncResult<Void>> ackHandler)`
* `feed(JsonObject body, String tag, Handler<AsyncResult<Void>> ackHandler)`

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
* `auditor()` - the address of the [auditor](#auditors) for this message's message tree

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
* `emit(JsonObject body)` - emits a message body
* `emit(JsonObject body, String tag)` - emits a message body with a tag
* `emit(JsonObject body, JsonMessage parent)` - emits a message body as
  a child of the given `JsonMessage` instance
* `emit(JsonObject body, String tag, JsonMessage parent)` emits a message
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

For more information see
[how Vertigo guarantees message processing](#how-vertigo-guarantees-message-processing)

### Executors
Executors are components that execute part or all of a network essential
as a remote procedure invocation. Data emitted from executors is tagged
with a unique ID, and new messages received by the executor are correlated
with past emissions.

Each executor exposes the following `execute()` methods:
* `execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler)`
* `execute(JsonObject args, String tag, Handler<AsyncResult<JsonMessage>> resultHandler)`

#### Basic Executor
The `BasicExecutor` is a bare bones implementation of the executor API that
is synonymous with the `BasicFeeder` for feeders. The `BasicExecutor` exposes
the following configuration methods:

* `setReplyTimeout(long timeout)` - sets the message reply timeout
* `getReplyTimeout()` - gets the message reply timeout
* `setMaxQueueSize(long queueSize)` - sets the maximum execute queue size
* `getMaxQueueSize()` - gets the maximum execute queue size
* `queueFull()` - indicates whether the execute queue is full

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

#### Polling Executor
The `PollingExecutor` allows a handler to be registered with the executor.
Whenever the executor queue is prepared to accept new messages (i.e. the execute
queue is not full) the handler will be called. This allows flow to be controlled
by the executor.

The `PollingExecutor` exposes the following configuration methods:
* `setExecuteDelay(long delay)` - sets the amount of time to delay between polls
  when no executions occur during polling
* `getExecuteDelay()` - gets the execute delay period

Execute handlers are registered via the `executeHandler()` method:
* `executeHandler(Handler<PollingExecutor> handler)`

```java
vertigo.createPollingExecutor().start(new Handler<AsyncResult<PollingExecutor>>() {
  public void handle(AsyncResult<PollingExecutor> result) {
    if (result.succeeded()) {
      PollingExecutor executor = result.result();
      executor.executeHandler(new Handler<PollingExecutor>() {
        public void handle(PollingExecutor executor) {
          executor.execute(new JsonObject().putNumber("x", 10).putNumber("y", 45),
            new Handler<AsyncResult<JsonMessage>>() {
              public void handle(AsyncResult<JsonMessage> result) {
                if (result.succeeded()) {
                  container.logger().info("Result is " + result.result().body().getInteger("sum"));
                }
              }
            });
        }
      });
    }
  }
});
```

#### Stream Executor
The `StreamExecutor` is specifically designed to integrate with Vert.x `ReadStream`
APIs. To do so, the `StreamExecutor` exposes an interface similar to that of the
`WriteStream`. The `StreamExecutor` exposes the following methods:
* `fullHandler(Handler<Void> handler)` - sets a handler to be invoked when the
  execute queue is full
* `drainHandler(Handler<Void> handler)` - sets a handler to be invoked when a
  full execute queue has been drained

```java
vertigo.createStreamExecutor().start(new Handler<AsyncResult<StreamExecutor>>() {
  public void handle(AsyncResult<StreamExecutor> result) {
    if (result.succeeded()) {
      final StreamExecutor executor = result.result();

      NetServer server = vertx.createNetServer();

      server.connectHandler(new Handler<NetSocket>() {
        public void handle(final NetSocket sock) {

          // Set full and drain handlers on the executor.
          executor.fullHandler(new VoidHandler() {
            public void handle() {
              sock.pause();
            }
          });
          executor.drainHandler(new VoidHandler() {
            public void handle() {
              sock.resume();
            }
          });

          sock.dataHandler(new Handler<Buffer>() {
            public void handle(Buffer buffer) {
              executor.execute(new JsonObject().putString("body", buffer.toString()),
                new Handler<AsyncResult<JsonMessage>>() {
                  public void handle(AsyncResult<JsonMessage> result) {
                    if (result.failed()) {
                      container.logger().error("Failed to process message.");
                    }
                    else {
                      container.logger().error("Message result is " + result.result().body().encode());
                    }
                  }
                });
            }
          });
        }
      }).listen(1234, "localhost");
    }
  }
});
```

Networks that use remote procedure invocations must be designed in a very
specific manner. Remote procedure calls work by essentially creating circular
connections between network components. See
[defining remote procedure calls](#defining-remote-procedure-calls) for more
on how this works.

## Creating networks
Networks are defined in code using a `Network` instance. 

Some [examples](https://github.com/kuujo/vertigo/tree/master/examples/complex)
demonstrate how the network definition API works.

To define a network, create a new `Network` instance either using
a `Networks` factory method or instantiating a definition instance directly.

```java
Network network = new Network("test");
```

Each network must be given a *unique* name. Vertigo component addresses are
generated in a predictable manner, and this name is used to prefix all
component addresses and instance addresses.

The `Network` exposes the following configuration methods:
* `setAddress(String address)` - sets the network address, this is a unique event
  bus address at which the network coordinator will coordinate deployment and
  monitoring of network components
* `enableAcking()` - enables acking for the network
* `disableAcking()` - disabled acking for the network
* `isAckingEnabled()` - indicates whether acking is enabled for the network
* `setNumAuditors(int numAuditors)` - sets the number of network [auditors](#auditors) (ackers)
* `getNumAuditors()` - indicates the number of network [auditors](#auditors)
* `setAckExpire(long expire)` - sets the message ack expiration for the network
* `getAckExpire()` - indicates the ack expiration for the network
* `setAckDelay(long delay)` - sets an optional period of time to way after a message
  tree has been fully acked to ensure that more children will not be created. This
  is useful in cases where new message children may be created after some time
  has passed (messages are stored in memory in a component for a period of time).
  Defaults to `0`
* `getAckDelay()` - indicates the ack delay for the network

### Adding components
The `Network` class provides several methods for adding components
to the network.

* `addComponent(Component<?> component)`
* `addVerticle(String address)`
* `addVerticle(String address, String main)`
* `addVerticle(String address, String main, JsonObject config)`
* `addVerticle(String address, String main, int instances)`
* `addVerticle(String address, String main, JsonObject config, int instances)`
* `addModule(String address)`
* `addModule(String address, String moduleName)`
* `addModule(String address, String moduleName, JsonObject config)`
* `addModule(String address, String moduleName, int instances)`
* `addModule(String address, String moduleName, JsonObject config, int instances)`

Note that Vertigo supports both verticles and modules as network components.
The return value of each of these methods is a new `Component` instance
on which you can set the following properties:

* `setType(String type)` - sets the component type, *verticle* or *module*
  Two constants are also available, `Component.VERTICLE` or
  `Component.MODULE`
* `setConfig(JsonObject config)` - sets the component configuration. This is made available
  as the normal Vert.x configuration within a component instance
* `setInstances(int instances)` - sets the number of component instances

There are two specific types of components, `Verticle` and `Module`.
The `Verticle` class adds the `setMain(String main)` and `getMain()` methods.
The `Module` class adds the `setModule(String moduleName)` and `getModule()` methods.

### Adding inputs
Conncetions between components are created by adding an input to a
component definition. Inputs indicate which components a given component
is interested in receiving messages from. Vertigo uses a publish/subscribe
messaging system, so when a component is started, it will subscribe to
messages from other components according to its input configurations.

Inputs may be added to any component with the `addInput` method:
* `addInput(Input input)`
* `addInput(String address)`
* `addInput(String address, Grouping grouping)`
* `addInput(String address, Filter... filters)`
* `addInput(String address, Grouping grouping, Filter... filters)`

Each of these methods returns the added `Input` instance which exposes
the following methods:
* `groupBy(Grouping grouping)` - sets the input grouping
* `filterBy(Filter filter)` - adds an input filter

The input address should be the event bus address for a component to which
the input will subscribe to receive new output messages. Note that this
subscription pattern does not place any restrictions on the input address,
so users can subscribe a component to output from any component in any network.
See [nested networks](#nested-networks) for an example.

### Input Groupings
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

To set a component grouping, call the `groupBy()` method on a component input.

```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2).addInput("foo.bar").groupBy(new FieldsGrouping("type"));
```

When messages are emitted to instances of the component, the related grouping
*selector* will be used to determine to which component instance a given
message is sent.

Vertigo provides several grouping types:

* `RandomGrouping` - component instances receive messages in random order
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").groupBy(new RandomGroupig());
```

* `RoundGrouping` - component instances receive messages in round-robin fashion
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").groupBy(new RoundGrouping());
```

* `FieldsGrouping` - component instances receive messages according to basic
  consistent hashing based on a given field
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").groupBy(new FieldsGrouping("type"));
```

Consistent hashing supports multiple fields as well.

* `AllGrouping` - all component instances receive a copy of each message
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").groupBy(new AllGrouping());
```

### Input Filters
Vertigo messages contain metadata in addition to the message body. And just
as with grouping component instances, sometimes components may be only
interested in receiving messages containing specific metadata. For this,
Vertigo provides message filters which allow components to define the types
of messages they receive. As with groupings, custom filters may be provided.

Filters are abstracted from component implementations, so they can be added
when *defining* a network rather than within component verticles themselves.

To add a filter to a component, call the `filterBy()` method on a component
input, passing a filter instance. Multiple filters can be set on any
given component, in which case a message must pass *all* filters before being
sent to the component.

```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").filterBy(new TagsFilter("product"));
```

Vertigo provides several types of filters:

* `TagsFilter` - filters messages by tags
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").filterBy(new TagsFilter("product"));
```

* `FieldFilter` - filters messages according to a field/value
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").filterBy(new FieldFilter("type", "product"));
```

* `SourceFilter` - filters messages according to the source component name
```java
Network network = new Network("foo");
network.addComponent("foo.bar", "com.mycompany.myproject.MyFeederVerticle");
network.addComponent("foo.baz", "some_worder.py", 2)
  .addInput("foo.bar").filterBy(new SourceFilter("rabbit"));
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

```java
Network network = new Network("rpc");
network.addVerticle("rpc.executor", "executor.py").addInput("rpc.sum");
network.addVerticle("rpc.sum", "sum.py").addInput("rpc.executor");
```

### Defining networks in JSON
Since *networks*, *inputs*, *groupings*, and *filters* are all sent over
the Vert.x event bus at some point, each of these types are built
upon `JsonObject` instances. The JSON underlying the network definitions
API is designed to be user-readable, so networks can be created in JSON
configuration files.

The `Network` class uses the following underlying `JsonObject` structure:
* `address` - the `String` network address
* `broadcast` - the `String` network broadcast address. This is the address on which
  components listen for ack/fail messages from auditors. Defaults to
  `{network}.broadcast`
* `auditors` - the `int` number of network [auditors](#auditors) (ackers). Defaults to `1`
* `acking` - a `boolean` indicating whether acking is enabled for the network.
  Defaults to `true`
* `ack_expire` - a `long` auditor ack expiration. Note that auditors and feeders
  maintain *separate* ack timers, so this differs from those of feeders and
  executors
* `ack_delay` - a `long` ack delay. The ack delay indicates the amount of time an
  auditor will wait after a message tree has been acked to ensure no more child
  messages will be created. In most cases, this is unnecessary because of the
  pattern used by auditors to determine ack/fail statuses of message trees.
  However, in cases where components may hold messages in memory for a period
  of time before creating child messages, this option should be set to `> 0`.
  Defaults to `0` (no delay)
* `components` - a JSON object containing component definitions. Each component
  should be keyed by the unique component event bus address, with the following options:
   * `address` - the `String` component address. This will always be set to the object key
   * `type` - the `String` component type. This must be either `module` or `verticle`.
   * `module` - the `String` module name (for module type components)
   * `main` - the `String` verticle main (for verticle type components)
   * `config` - a JSON object component configuration. This will be available as
     the normal verticle configuration via `container.config()`
   * `instances` - the `int` number of component instances
   * `heartbeat_interval` - the `long` component heartbeat interval. This indicates
     the period at which component instances will send heartbeat messages to the
     network coordinator
   * `inputs` - a JSON array of component inputs, with each element of the array
     being a JSON object containing the following options:
     * `address` - the absolute input component address
     * `grouping` - a JSON object input grouping definition. Grouping options are
       dependent upon the grouping type. At a minimum, an input grouping must
       contain the following required options:
        * `type` - the `String` grouping class name. This must be a class that implements
          the `net.kuujo.vertigo.input.grouping.Grouping` interface
        * See [grouping options](#grouping-options) for grouping-specific options
     * `filters` - a JSON array of input filters, with each element of the array
       being a JSON object defining a single input filter. Input filter definition
       options are dependent upon the filter type. At a minimum, a filter must
       contain the following required options:
        * `type` - the `String` filter class name. This must be a class that implements the
          `net.kuujo.vertigo.input.filter.Filter` interface
        * See [filter options](#filter-options) for filter-specific options

#### Grouping options
* `AllGrouping` - none
* `RoundGrouping` - none
* `RandomGrouping` - none
* `FieldsGrouping`
   * `fields` - an array of field names on which to group

##### Filter options
* `FieldFilter`
   * `field` - the `String` field name
   * `value` - the `Object` required field value
* `TagsFilter`
   * `tags` - a JSON array of allowed tags
* `SourceFilter`
   * `source` - a `String` source component address on which to filter

### An example JSON network
Here's a simple example of a network defined in JSON:

```
{
  "address": "test",
  "broadcast": "test.broadcast",
  "auditors": 2,
  "ack_expire": 15000,
  "ack_delay": 1000,
  "components": {
    "test.feeder1": {
      "type": "verticle",
      "main": "com.mycompany.myproject.FeederVerticleOne",
      "config": {
        "foo": "bar"
      }
    },
    "test.feeder2": {
      "type": "module",
      "module": "com.mycompany~feeder-two~1.0",
      "instances": 2
    },
    "test.first_worker": {
      "type": "verticle",
      "main": "first_worker.js",
      "instances": 4,
      "inputs": [
        {
          "address": "test.feeder1",
          "grouping": {"type": "net.kuujo.vertigo.input.grouping.RandomGrouping"}
        },
        {
          "address": "test.feeder2",
          "grouping": {"type": "net.kuujo.vertigo.input.grouping.RandomGrouping"},
          "filters": [
            {
              "type": "net.kuujo.vertigo.input.filter.TagsFilter",
              "tags": ["order"]
            }
          ]
        }
      ]
    },
    "test.second_worker": {
      "type": "module",
      "module": "com.mycompany~second-worker~1.0",
      "config": {
        "wubz": "bubz"
      },
      "instances": 2,
      "inputs": [
        {
          "address": "test.first_worker",
          "grouping": {
            "type": "net.kuujo.vertigo.input.grouping.FieldsGrouping",
            "fields": ["type"]
          }
        }
      ]
    }
  }
}
```

### Instantiating networks from JSON
Given a valid JSON network definition, we can construct a new `Network` instance
using the `Networks` API.

Let's say we start a verticle with the previously defined network JSON structure
in its configuration file:

```
vertx run MyVerticle.java -conf network.json
```

Here's an example of how we can create a network from that JSON configuration:

```java
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.LocalCluster;
import net.kuujo.vertigo.network.Network;
import net.kuujo.vertigo.network.Networks;

import org.vertx.java.platform.Verticle;
import org.vertx.java.core.Handler;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.json.JsonObject;

public class MyVerticle extends Verticle {

  @Override
  public void start() {
    JsonObject config = container.config();
    Network network = Networks.fromJson(config);
    final Cluster cluster = new LocalCluster(vertx, container);
    cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
      @Override
      public void handle(AsyncResult<NetworkContext> result) {
        if (result.succeeded()) {
          cluster.shutdown(network);
        }
      }
    });
  }

}
```

## Network deployment
Once you have defined a network using the definition API, the network can
be deployed via the `Cluster` API. Vertigo provides two types of deployment
methods via `LocalCluster` and `ViaCluster`. Each implement the `Cluster`
interface:

* `deploy(Network network)`
* `deploy(Network network, Handler<AsyncResult<NetworkContext>> doneHandler)`
* `shutdown(NetworkContext context)`
* `shutdown(NetworkContext context, Handler<AsyncResult<Void>> doneHandler)`

When a network is deployed successfully, a `NetworkContext` instance may
be returned if a `doneHandler` was provided. The `NetworkContext` instance
contains information about the network components, including component
definitions, addresses, and connections.

### Deploying networks locally
To deploy a network locally (within a single Vert.x instance) use the
`LocalCluster`. The local cluster uses the core Vert.x `Container` API
to deploy network verticles and modules locally.

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

### Deploying networks across a cluster
Vertigo also supports deploying networks across a cluster of Vert.x instances.
This is supported using the [Via](https://github.com/kuujo/via) distributed
deployment module. Via allows Vertigo to essentially deploy network
verticles and modules by sending messages over the event bus to other
Vert.x instances. This can result in a much more stable network since
Via can reassign components to other Vert.x instances if a specific node
dies.

Via clusters use the same interface as local clusters, but with an additional
`address` argument to the constructor - the address to the Via *master*
through which the network's coordinator should deploy component modules
and verticles.

```java
final Cluster cluster = new ViaCluster(vertx, container, "via.master");
cluster.deploy(network, new Handler<AsyncResult<NetworkContext>>() {
  public void handle(AsyncResult<NetworkContext> result) {
    if (result.succeeded()) {
      NetworkContext context = result.result();
      cluster.shutdown(context);
    }
  }
});
```

## Events
Vertigo emits event messages over the Vert.x event bus when certain special
events occur. To listen for a Vertigo system event, simply register a handler
on the Vert.x event bus at the specified event address. Currently, Vertigo events
are limited, but more will be added in the future (and by request).

### Network events
* `vertigo.network.deploy` - triggered when a network is deployed
   * `address` - the network address
   * `network` - a JSON representation of the network
* `vertigo.network.start` - triggered when an entire network has been started
   * `address` - the network address
   * `context` - a JSON representation of the network context
* `vertigo.network.shutdown` - triggered when a network has been shutdown
   * `address` - the network address
   * `context` - a JSON representation of the network context

### Component events
* `vertigo.component.deploy` - triggered when a component instance is deployed
   * `address` - the component address
   * `context` - a JSON representation of the component instance context
* `vertigo.network.start` - triggered when a component instance has been started
   * `address` - the component address
   * `context` - a JSON representation of the component instance context
* `vertigo.network.shutdown` - triggered when a component instance has been shutdown
   * `address` - the component address
   * `context` - a JSON representation of the component instance context

## Advanced Features
The Vertigo communication system was intentionally designed so that no component
needs to know too much about who it is receiving messages from or who it is sending
messages to. This results in a flexible messaging system, allowing users to tap
into specific portions of networks or join multiple networks together.

### Wire taps
A Vertigo component's output is not strictly limited to components within its own
network. Vertigo uses a publish-subscribe style messaging scheme, so components can
send messages to anyone who's interested in listening. This means you can "tap in"
to any component on any network from any Vert.x verticle (as long as you know the
component's addresss).


For example, let's say we've deployed the following network:

```java
Network network = new Network("tap");
network.addVerticle("tap.first", "first.js", 2);
network.addVerticle("tap.second", "second.py", 2).addInput("tap.first");
network.addVerticle("tap.third", "Third.java", 4).addInput("tap.second");
```

We can tap into the output of any of the network's components using a `Listener` instance.
Listeners behave just as any other message receiving API in Vertigo - in fact, listeners
underly the `InputCollector` API. So, we simply create a new listener, assign a `messageHandler`
to the listener, and start it.

```java
Listener listener = new DefaultListener("tap.second", vertx);
listener.messageHandler(new Handler<JsonMessage>() {
  @Override
  public void handle(JsonMessage message) {
    System.out.println("This message came from tap.second:");
    System.out.println(message.body().encode());
  }
}).start();
```

When the listener is started, it will begin *publishing* heartbeat messages to
`tap.second` on the event bus. Heartbeat messages to `tap.second` will be received
by *both* of the component instances at `tap.second`, each of which will add the
listener (and its unique address) as an output channel. If this verticle is stopped
or the `stop()` method is called on the listener, the component instances at `tap.second`
will stop receiving heartbeats and will thus remove the listener as an output channel.

Note that the `Listener` constructor can also accept an `Input` instance as an argument,
so multiple instances of the same verticle can be supported.

### Nested networks
Just as any Vert.x verticle can request input from any network component, networks too
can receive input from any other network's component. This can be accomplished simply
by adding the component's address as an input to any network component.

Using the same network as before:

```java
Network network = new Network("tap");
network.addVerticle("tap.first", "first.js", 2);
network.addVerticle("tap.second", "second.py", 2).addInput("tap.first");
network.addVerticle("tap.third", "Third.java", 4).addInput("tap.second");
```

We can create another network that receives input from `tap.second`:

```java
Network network = new Network("tapper");
network.addVerticle("tapper.worker1", "tapper_worker1.js", 2)
  .addInput("tap.second").groupBy(new RandomGrouping());
network.addVerticle("tapper.worker2", "tapper.worker2.py", 4)
  .addInput("tapper.worker1").groupBy(new RoundGrouping());
```

This results in a network that has no feeders, but instead essentially adds workers to
the first network. In fact, since Vertigo's ack/fail system is similarly abstracted from
network details - auditors are independent of networks, and each message tree is assigned
a specific auditor - messages will behave as if the appended network is indeed a part
of the original network, and the original message source will not receive ack/fail
notification until the second network has completed processing of the received message.
This makes this a reliable method of expanding upon existing running networks.

# How it works

1. [How networks are defined](#how-networks-are-defined)
   * [Networks](#networks)
   * [Components](#components)
   * [Inputs](#inputs)
   * [Groupings](#groupings)
   * [Filters](#filters)
   * [Serialization](#serialization)
1. [How networks are deployed](#how-networks-are-deployed)
   * [Contexts](#contexts-1)
   * [Coordinators](#coordinators)
   * [Clustering](#clustering)
      * [Local clusters](#local-clusters)
      * [Remote clusters](#remote-clusters)
1. [How components communicate](#how-components-communicate)
   * [Messages](#messages)
   * [Input collectors](#input-collectors)
   * [Listeners](#listeners)
   * [Output collectors](#output-collectors)
1. [How Vertigo guarantees message processing](#how-vertigo-guarantees-message-processing)
   * [Auditors](#auditors)

## How networks are defined
Vertigo provides an API for defining network structures. Network APIs are
simply wrappers around Vert.x `JsonObject` instances, so networks may also
be defined in and constructed from pure JSON. This allows network structures
to be passed between verticles via the Vert.x event bus. All Vertigo data
structures follow this pattern.

### Networks
Each Vertigo network must have a unique address. This address is used by Vertigo
as the network's [coordinator](#coordinators) address. Additionally, each network
may contain any number of components, with components being represented as Vert.x
verticles or modules. Networks place no limitations on structure, as networks are
not interested in the relationships between components.

```java
Network network = new Network("network.address");
```

### Components
Vertigo is built on Vert.x, and thus network components are simply Vert.x verticles
or modules. This means Vertigo can support verticles in various languages within
the same network (any Vert.x supported language). Each component must be assigned
a *unique event bus address* - the address on which the component's
[output collector](#output-collectors) will listen for connections.

```java
network.addVerticle("first.component", "com.mycompany.myproject.FirstVerticle", 2);
network.addModule("second.component", "com.mycompany~second-verticle~1.0", 4);
```

### Inputs
Vertigo uses a publish-subscribe style messaging paradigm to manage communication
between network components. This system is represented in the networks API as
*inputs*. Essentially, an *input* defines a subscription to the *output* of
another component. In fact, this is represented in the [input](#input-collectors)
and [output](#output-collectors) collectors of component implementaitons. Each
Vertigo component may have any number of inputs, with each input indicating the
address to which the component is subscribing.

```java
Input input = network.addVerticle("some.address", "com.mycompany.myproject.MyVerticle").addInput("some.other.address");
```

Inputs are the API for defining relationships between components, but once
a component subscribes to another component's output, an input becomes an
[output](#outputs).

### Groupings
Publish-subscribe style messaging is simple to manage between two components,
but what do we do when dealing with multiple component instances? You may not
want all instances of a component to receive a copy of the same message. A
component may require that all messages with a specific field value are always
sent to the same component instance. For this, Vertigo provides a *groupings*
API that helps users manage how messages are distributed between multiple
component instances.

```java
network.addVerticle("some.address", "some_verticle.py", 2)
  .addInput("other.address").groupBy(new FieldsGrouping("type"));
```

When the `some.address` component sends a `listen` message to the component
at `other.address`, it will send along with its subscription request a
[selector](#selectors) definition. The component at `other.address` will then
use that selector information to select the appropriate component instance
to which to send each message.

See [component groupings](#component-groupings) for more examples

### Filters
In some cases a component may subscribe to output from a component that may
not always output messages that are of interest. For these cases, inputs
provide a filtering mechanism.

```java
network.addVerticle("some.address", "some_verticle.py", 2)
  .addInput("other.address").filterBy(new TagsFilter("foo"));
```

When the `some.address` component sends a `listen` message to the component
at `other.address`, it will send along with its subscription request a set
of [conditions](#conditions). The component at `other.address` will then use
that condition information to filter its output.

See [component filters](#component-filters) for more examples

### Serialization
All network definition APIs - networks, inputs, groupings, and filters - are
"serializable" to `JsonObject` instances via the Vertigo `Serializable`
interface. This allows network information to be passed between verticles on
the Vert.x event bus.

```java
eventBus.registerHandler("deserialize", new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> message) {
    try {
      Network network = Serializer.deserialize(message.body());
    }
    catch (SerializationException e) {
      e.printStackTrace();
    }
  }
});

Network network = new Network("test");
network.addVerticle("test.one", "one.js", 3);
JsonObject serialized = Serializer.serialize(network);
eventBus.send("deserialize", serialized);
```

This allows users to provide custom [groupings](#groupings) and
[filters](#filters) that can be reliably distributed around a Vertigo
cluster.

## How networks are deployed
Once a network is defined and deployed - whether within a single
Vert.x or a cluster of Vert.x instances - Vertigo handles a number of
processes to start and monitor the network.

### Contexts
The first step to deploying a Vertigo network is converting its structure
to a network context. Network contexts are simply immutable network
definitions which are used by Vertigo verticles to deploy, start, and
monitor various components.

### Coordinators
When a Vertigo network is deployed, the verticle from which the network
is deployed does not actually deploy component modules and verticles.
Instead, a single special verticle known as the *coordinator* is deployed.
As the name implies, the coordinators job is to coordinate deployment,
setup, and monitoring of component instances.

When the coordinator is deployed its first task is to deploy component
verticles and modules. How verticles and modules are deployed depends on
the [cluster](#clustering) type. If any verticle or module fails to deploy,
the network deployment will fail and the original deployer will be notified.

Once a component instance has been deployed by the coordinator it must then
connect back to the coordinator to send it heartbeat messages. Heartbeats
are the mechanism by which the coordinator ensures component verticles and
modules continue to run. If a heartbeat times out, the coordinator will
consider the component module or verticle to be failed and attempt to redeploy
it. Once all component instances have begun to send heartbeat messages to
the coordinator, the network is considered to be *deployed*.

But deployment isn't the only consideration in network coordination. Once
each component instance is deployed, it must register various handlers on
the event bus as well. It is critical that no feeder begin to send messages
before all components have completed setup, so the coordinator requires
that components send it *ready* messages once each component instance has
completed its respective setup as well. This allows the coordinator to
notify each component instance once the entire network has been setup so
no messages are lost.

### Clustering
Vertigo supports deploying networks within a single Vert.x instance or
across a cluster of Vert.x instances. To do so, two separate types of
coordinators are provided, supporting local and remote deployment
respectively.

#### Local clusters
Local clusters are deployed within a single Vert.x instance using the
standard Vert.x `Container` API.

#### Remote clusters
Remote clusters a deployed across multiple Vert.x instances using
[Via](https://github.com/kuujo/via).

## How components communicate
One of the primary responsibilities of Vertigo is managing communication between
network components in a consistent, predictable, and reliable manner. Vertigo
uses the Vert.x event bus for inter-component communication, but Vertigo also
provides many reliability features on top of the event bus.

Network components communicate with each other directly over the event bus
rather than being routed through a central message broker. In Vertigo, this
is accomplished using a publish-subscribe-like messaging scheme. When component
instance is started, the component sends a message to any other component from
which it is interested in receiving messages. Similarly, it listens for other
components that are interested in receiving messages. This allows components
to set up direct connections between one another, ensuring fast messaging
between them.

![Communication Channels](http://s7.postimg.org/unzwkrvgb/vertigo_channel.png)

The requirement for direct communication between components using a
publish-subscribe style system combined with the need for flexible messaging
schemes via [groupings](#groupings) and [filters](#filters) results in some
pretty interesting APIs - APIs which can be used to mimic Vertigo components
and tap into existing networks.

### Messages
Messages are sent over the event bus in the form of `JsonObject` instances.
Just as Vertigo networks have a structure, Vertigo messages can have structure
as well: trees. Messages can be emitted from components either as individuals
or as children of other messages. This hierarchical system integrates with the
message acking system to provide increased reliability - acking for entire
message trees, not just individual messages.

Vertigo messages also contain a number of metadata fields in addition to
the message body. These fields describe things like where the message came
from, who the message's parents and ancestors are, and other interesting
information. [Read more about message fields here](#jsonmessage)

### Input collectors
One of the core components of each Vertigo component is an *input collector*.
The input collector is tasked with sending `listen` messages to addresses from
which it is interested in receiving messages. Each input collector has a unique
address *even across multiple instances of the same component*. This unique
address is used by [selectors](#selectors) to choose between component instances
to which to send any given message.

### Listeners
Underlying a component's input collector is any number of *listeners*. Listeners
are essentially subsribers that are directly related to a single component input.
In fact, listeners can be used outside of components to tap into component output.
See [wire taps](#wire-taps) for a brief tutorial on listening to a component's output.

When a listener is started, it begins *publishing* periodic `listen` messages to
the interesting component's event bus address. This ensures that *each instance of
that component* receives the listen request.

### Output collectors
The other core component of each Vertigo component is the *output collector*.
The output collector is tasked with receiving `listen` requests from other components
and sending messages to those listening components. When the output collector is
started, it *binds to the component's address*.

## How Vertigo guarantees message processing
As messages make their way through networks Vertigo tracks them. Messages
can have hierarchical structures - [message trees](#messages) - and Vertigo
ensures that if any message at any point in a tree fails to be processed (times
out or is failed), the message source will be notified. To do so, Vertigo uses
special *auditor* verticles.

### Auditors
Auditor verticles are special verticles that are deployed along with
each network. Auditors are tasks with tracking message trees and handling
ack/fail/timeouts of each message.

![Network Auditor](http://s14.postimg.org/kkl297qo1/vertigo_acker.png)

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
