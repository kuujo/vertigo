Vertigo
=======

**Need support? Check out the [Vertigo Google Group][google-group]**

**[Getting Started](#getting-started) | [Java User Manual](#java-user-manual) | [Javadoc](http://vertigo.kuujo.net/java)**

[Javascript][vertigo-js] | [Python][vertigo-python]

Vertigo is a durable polyglot event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Combining concepts of cutting-edge
[real-time systems](http://storm.incubator.apache.org/) and
[flow-based programming](http://en.wikipedia.org/wiki/Flow-based_programming),
Vertigo allows real-time problems to be broken down into smaller tasks (as
Vert.x verticles) and distributed across a Vert.x cluster.

* Manages multi-step event processing systems, from simple pipelines to
  **complex networks of Vert.x modules/verticles**
* Supports deployment of networks **within a single Vert.x instance or across a
  Vert.x cluster** and provides automatic **failover** for failed components
* **Coordinates startup and shutdown** of networks across the Vert.x cluster
* **Promotes reusability** by abstracting communication details from verticle implementations,
  allowing components to be arbitrarily connected to form complex networks
* Guarantees **strong ordering** and **exactly-once** processing of messages
* Handles **event bus flow control** and **automatic retries** on failures
* Supports `Serializable` Java messages on the Vert.x event bus
* Facilitates distribution of messages between multiple verticle instances using
  **random, round-robin, mod hashing, fair, or fanout** methods
* Provides **cluster-wide shared data** structures for synchronization
* Supports **live network configuration changes** so networks do not have to
  be shutdown in order to be reconfigured
* Provides a simple **batch API** for efficiently batching messages between components
* Support **command line deployment** of networks
* Components can be written in **any Vert.x supported language**, with
  APIs for Vertigo in [Javascript][vertigo-js] and [Python][vertigo-python]

For an in-depth explanation of how Vertigo works, see [how it works](#how-it-works)

YourKit is generously supporting Vertigo and other open-source projects with its
full-featured Java Profiler. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).

# Java User Manual

**Vertigo 0.7.0-beta2 has been released!**

**Note: Vertigo 0.7.0-beta2 is only compatible with Vert.x versions that use
Hazelcast 3.x**

1. [Getting Started](#getting-started)
   * [Setup](#setup)
      * [Adding Vertigo as a Maven dependency](#adding-vertigo-as-a-maven-dependency)
      * [Including Vertigo in a Vert.x module](#including-vertigo-in-a-vertx-module)
   * [Networks](#networks)
   * [Components](#components)
   * [The Vertigo cluster](#cluster)
   * [A simple network](#a-simple-network)
1. [Networks](#networks-1)
   * [Creating a new network](#creating-a-new-network)
   * [Adding components to a network](#adding-components-to-a-network)
   * [Creating connections between components](#)
   * [Routing messages between multiple component instances](#routing-messages-between-multiple-component-instances)
   * [Creating networks from JSON](#creating-networks-from-json)
1. [Components](#components-1)
   * [Creating a component](#creating-a-component)
   * [The elements of a Vertigo component](#the-elements-of-a-vertigo-component)
1. [Messaging](#messaging)
   * [Sending messages on an output port](#sending-messages-on-an-output-port)
   * [Receiving messages on an input port](#receiving-messages-on-an-input-port)
   * [Working with message groups](#working-with-message-groups)
   * [Working with message batches](#working-with-message-batches)
   * [Providing serializeable messages](#providing-serializeable-messages)
1. [Network Deployment and Clustering](#network-deployment-and-clustering)
   * [Starting a cluster from the command line](#starting-a-cluster-from-the-command-line)
   * [Starting a cluster programmatically](#starting-a-cluster-programmatically)
   * [Referencing a cluster programmatically](#referencing-a-cluster-programmatically)
   * [Accessing a cluster through the event bus](#accessing-a-cluster-through-the-event-bus)
   * [Deploying a network](#deploying-a-network)
   * [Deploying a network from json](#deploying-a-network-from-json)
   * [Undeploying a network](#undeploying-a-network)
   * [Checking if a network is deployed](#checking-if-a-network-is-deployed)
   * [Listing networks running in a cluster](#listing-networks-running-in-a-cluster)
   * [Deploying a bare network](#deploying-a-bare-network)
   * [Reconfiguring a network](#reconfiguring-a-network)
   * [Working with active networks](#working-with-active-networks)
   * [Deploying a network from the command line](#deploying-a-network-from-the-command-line)
1. [Cluster Management](#cluster-management)
   * [Accessing the cluster from within a component](#accessing-the-cluster-from-within-a-component)
   * [Deploying modules and verticles to the cluster](#deploying-modules-and-verticles-to-the-cluster)
   * [Undeploying modules and verticles from the cluster](#undeploying-modules-and-verticles-from-the-cluster)
   * [Checking if a module or verticle is deployed](#checking-if-a-module-or-verticle-is-deployed)
   * [Deploying modules and verticles with HA](#deploying-modules-and-verticles-with-ha)
   * [Working with HA groups](#working-with-ha-groups)
1. [Cluster-wide Shared Data](#cluster-wide-shared-data)
   * [AsyncMap](#asyncmap)
   * [AsyncSet](#asyncset)
   * [AsyncList](#asynclist)
   * [AsyncQueue](#asyncqueue)
   * [AsyncCounter](#asynccounter)
   * [Accessing shared data over the event bus](#accessing-shared-data-over-the-event-bus)
1. [Hooks](#hooks)
   * [InputHook](#inputhook)
   * [OutputHook](#outputhook)
   * [IOHook](#iohook)
   * [ComponentHook](#componenthook)
1. [Logging](#logging)
   * [Logging messages to output ports](#logging-messages-to-output-ports)
   * [Reading log messages](#reading-log-messages)
1. [How it works](#how-it-works)
   * [Configurations](#configurations)
   * [Cluster](#cluster)
   * [Networks](#networks-2)
   * [Components](#components-2)
   * [Communication](#communication)

# Getting Started
This is a brief tutorial that will help guide you through high-level Vertigo
concepts and go through a simple network example. Check out the repository
for more [examples](https://github.com/kuujo/vertigo/tree/master/examples).

## Setup
Vertigo can be added to your project as a Maven dependency or included in
your modules via the Vert.x module system.

### Adding Vertigo as a Maven dependency

```
<dependency>
  <groupId>net.kuujo</groupId>
  <artifactId>vertigo</artifactId>
  <version>0.7.0-beta2</version>
</dependency>
```

### Including Vertigo in a Vert.x module

To use the Vertigo Java API, you can include the Vertigo module in your module's
`mod.json` file. This will make Vertigo classes available within your module.

```
{
  "main": "com.mycompany.myproject.MyVerticle",
  "includes": "net.kuujo~vertigo~0.7.0-beta2"
}
```

## Networks
Networks are collections of Vert.x verticles and modules that are connected
together by input and output ports. Each component in a network contains processing
logic, and connections between components indicate how messages should be
passed between them. Networks can be created either in code or in JSON and can
be deployed in code or from the command line.

![Vertigo network](http://s21.postimg.org/ve93v28bb/Untitled_Diagram.png)

## Components

As with any Vert.x verticle, Vertigo components can be deployed with any number
of instances. Components communicate with each other over the Vert.x event bus.
But Vertigo doesn't use raw event bus addresses. Instead, components communicate
through named output and input ports. This allows components to be abstracted
from the relationships between them, making them reusable.

Messaging in Vertigo is inherently uni-directional. Components receive messages
from other components on input ports and send messages to other components on
output ports.

![Direct connections](http://s21.postimg.org/65oa1e3dj/Untitled_Diagram_1.png)

Messages are not routed through any central router. Rather, components
communicate with each other directly over the event bus.

```java
public class MyComponent extends ComponentVerticle {

  @Override
  public void start() {
    input.port("in").messageHandler(new Handler<String>() {
      public void handle(String message) {
        output.port("out").send(message);
      }
    });
  }

}
```

Ports do not have to be explicitly declared. Vertigo will lazily create ports
if they don't already exist. Messages can be of any type that is supported by the
Vert.x event bus. Vertigo guarantees that messages will always arrive in the order
in which they were sent.

In cases where a connection connects two components with multiple instances,
Vertigo facilitates special routing between the components with *selectors*.

![Selectors](http://s21.postimg.org/a3bjqsq6v/Untitled_Diagram_2.png)

While components receive messages on input ports and send messages to output ports,
the network configuration is used to define how ports on different components
relate to one another. Connections between components/ports in your network indicate
how messages will flow through the network.

```java
NetworkConfig network = vertigo.createNetwork("foo");
network.addComponent("bar", "bar.js", 2);
network.addComponent("baz", "baz.py", 4);
network.createConnection("bar", "out", "baz", "in");
```

## The Vertigo Cluster
Vertigo provides its own cluster abstraction within the Vert.x cluster. Vertigo
clusters are simple collections of verticles that manage deployment of networks,
allow modules and verticles to be deploye remotely (over the event bus)
and provide cluster-wide shared data structures. Clusters can be run either in
a single Vert.x instance (for testing) or across a Vert.x cluster.

![Cluster](http://s8.postimg.org/n535zhv9h/cluster.png)

Rather than communicating over the unreliable event bus, Vertigo uses Hazelcast
data structures to coordinate between the cluster and components.
This is one of the properties that makes Vertigo networks fault-tolerant. Even if
a failure occurs, the network configuration will remain in the cluster and Vertigo
will automatically failover any failed components.

## A Simple Network
Vertigo provides all its API functionality through a single `Vertigo` object.

```java
Vertigo vertigo = new Vertigo(this);
```

The `Vertigo` object supports creating and deploying networks. Each language
binding has an equivalent API.

```java
NetworkConfig network = vertigo.createNetwork("word-count");
network.addComponent("word-feeder", RandomWordCounter.class.getName());
network.addComponent("word-counter", WordCounter.class.getName(), 2);
network.createConnection("word-feeder", "word", "word-counter", "word", new HashSelector());
```

Vertigo components can be implemented in a variety of languages since they're
just Vert.x verticles. This network contains two components. The first component
is a Python component that will feed random words to its `word` out port. The second
component is a Javascript component that will count words received on its `word` in
port. The network therefore defines a connection between the `word-feeder` component's
`word` out port and the `word-counter` component's `word` in port.

Note that since we defined two instances of the `word-counter` component, it's
important that the same words always go to the same instance, so we use a
`HashSelector` on the connection to ensure the same word always goes to the
same component instance.

`random_word_feeder.py`

```python
import vertx
from vertigo import component, input

@component.start_handler
def start_handler(error=None):
  if not error:
    words = ['apple', 'banana', 'pear']
    def feed_random_word(timer_id):
      output.send('word', words[rand(len(words)-1)])
    vertx.set_periodic(1000, feed_random_word)
```

Here we simply send a random word to the `word` out port every second.

`word_counter.js`

```javascript
var input = require('vertigo/input');
var output = require('vertigo/output');

var words = {};
input.port('word').messageHandler(function(word) {
  if (words[word] === undefined) {
    words[word] = 0;
  }
  words[word]++;
  output.port('count').send({word: word, count: words[word]});
});
```

This component registers a message handler on the `word` in port, updates
an internal count for the word, and sends the updated word count on the
`count` out port.

In order for a network to be deployed, one or more nodes of a Vertigo cluster
must be running in the Vert.x cluster. Vertigo clusters are made up of simple
Vert.x verticles. To deploy a cluster node deploy the `vertigo-cluster` module.

```
vertx runmod net.kuujo~vertigo-cluster~0.7.0-beta2 -conf cluster.json
```

The cluster configuration requires a `cluster` name.

```
{
  "cluster": "test-cluster"
}
```

A test cluster can also be deployed locally through the `Vertigo` API.

```java
vertigo.deployCluster("test-cluster", new Handler<AsyncResult<ClusterManager>>() {
  public void handle(AsyncResult<ClusterManager> result) {
    ClusterManager cluster = result.result();
  }
});
```

Once the cluster has been deployed we can deploy a network to the cluster.

```java
cluster.deployNetwork(network);
```

Vertigo also supports deploying networks from the command line using simple
JSON configuration files.
See [deploying a network from the command line](#deploying-a-network-from-the-command-line)

## Networks
Vertigo networks are collections of Vert.x verticles and modules that are connected
together by the Vert.x event bus. Networks and the relationships therein are defined
externally to their components, promoting reusability.

Each Vertigo network must have a unique name within the Vert.x cluster in which it
is deployed. Vertigo uses the network name to coordinate deployments and configuration
changes for the network.

Networks are made up of any number of components which can be arbitrarily connected
by input and output ports. A Vertigo component is simple a Vert.x module or verticle,
and can thus have any number of instances associated with it.

### Creating a network
To create a new network, create a new `Vertigo` instance and call the `createNetwork` method.

```java
Vertigo vertigo = new Vertigo(this);
NetworkConfig network = vertigo.createNetwork("my-network");
```

All Vertigo networks have an explicit, unique name. This name is very important to
Vertigo as it can be used to reference networks from anywhere within a Vert.x cluster,
but more on that later.

### Adding components to a network
To add a component to the network, use one of the `addVerticle` or `addModule` methods.

```java
network.addVerticle("foo", "foo.js");
```

The `addVerticle` and `addModule` methods have the following signatures:

* `addModule(String name, String moduleName)`
* `addModule(String name, String moduleName, JsonObject config)`
* `addModule(String name, String moduleName, int instances)`
* `addModule(String name, String moduleName, JsonObject config, int instances)`
* `addVerticle(String name, String main)`
* `addVerticle(String name, String main, JsonObject config)`
* `addVerticle(String name, String main, int instances)`
* `addVerticle(String name, String main, JsonObject config, int instances)`

Just as with networks, Vertigo components are explicitly named. The component name
*must be unique within the network to which the component belongs*.

```java
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);
network.addModule("bar", "com.bar~bar~1.0", 4);
```

The `NetworkConfig` API also exposes an abstract `addComponent` method which detects
whether the added component is a module or a verticle based on module naming conventions.

* `addComponent(String name, String moduleOrMain)`
* `addComponent(String name, String moduleOrMain, JsonObject config)`
* `addComponent(String name, String moduleOrMain, int instances)`
* `addComponent(String name, String moduleOrMain, JsonObject config, int instances)`

```java
network.addComponent("foo", "foo.js", 2); // Adds a verticle component.
network.addComponent("bar", "com.bar~bar~1.0", 4); // Adds a module component.
```

Once a component has been added to the network, the component configuration will
be returned. Users can set additional options on the component configuration. The
most important of these options is the `group` option. When deploying networks within
a Vert.x cluster, the `group` indicates the HA group to which to deploy the module or
verticle.

### Creating connections between components
A set of components is not a network until connections are created between those
components. Vertigo uses a concept of *ports* to abstract input and output from
each component instance. When creating connections between components, you must
specify a component and port to which the connection connects. Each connection
binds one component's output port with another component's input port.

To create a connection between two components use the `createConnection` method.

```java
network.createConnection("foo", "out", "bar", "in");
```

The arguments to the `createConnection` method are, in order:
* The source component's name
* The source component's output port to connect
* The target component's name
* The target component's input port to connect

You may wonder why components and ports are specified by strings rather than
objects. Vertigo supports reconfiguring live networks with partial configurations,
so objects may not necessarily be available within the network configuration
when a partial configuration is created. More on partial network deployment
and runtime configuration changes in the [deployment](#deployment) section.

### Routing messages between multiple component instances
Just as with Vert.x verticles and modules, each Vertigo component can support
any number of instances. But connections are created between components and
not component instances. This means that a single connection can reference
multiple instances of each component. By default, the Vert.x event bus routes
messages to event bus handlers in a round-robin fashion. But Vertigo provides
additional routing methods known as *selectors*. Selectors indicate how messages
should be routed between multiple instances of a component.

![Selectors](http://s21.postimg.org/a3bjqsq6v/Untitled_Diagram_2.png)

Vertigo provides several selector types by default and supports custom selectors
as well.

* Round robin selector - selects targets in a round-robin fashion
* Random selector - selects a random target to which to send each message
* Hash selector - uses a simple mod hash algorithm to select a target for each message
* Fair selector - selects the target with the least number of messages in its send queue
* All selector - sends each message to all target instances
* Custom selector - user provided custom selector implementation

The `ConnectionConfig` API provides several methods for setting selectors
on a connection.
* `roundSelect()` - sets a round-robin selector on the connection
* `randomSelect()` - sets a random selector on the connection
* `hashSelect()` - sets a mod hash based selector on the connection
* `fairSelect()` - sets a fair selector on the connection
* `allSelect()` - sets an all selector on the connection
* `customSelect(Selector selector)` - sets a custom selector on the connection

### Creating networks from JSON
Vertigo supports creating networks from json configurations. To create a network
from json call the `Vertigo.createNetwork(JsonObject)` method.

```java
JsonObject json = new JsonObject().putString("name", "test-network");
vertigo.createNetwork(json);
```

The JSON configuration format is as follows:

* `name` - the network name
* `cluster` - the cluster to which to deploy the network. This option applies
  only when deploying the network from the command line
* `components` - an object of network components, keyed by component names
   * `name` - the component name
   * `type` - the component type, either `module` or `verticle`
   * `main` - the verticle main (if the component is a verticle)
   * `module` - the module name (if the component is a module)
   * `config` - the module or verticle configuration
   * `instances` - the number of component instances
   * `group` - the component deployment group (Vert.x HA group for clustering)
* `connections` - an array of network connections
   * `source` - an object defining the connection source
      * `component` - the source component name
      * `port` - the source component's output port
   * `target` - an object defining the connection target
      * `component` - the target component name
      * `port` - the target component's input port
   * `selector`- an object defining the connection selector
      * `type` - the selector type, e.g. `round-robin`, `random`, `hash`, `fair`, `all`, or `custom`
      * `selector` - for custom selectors, the selector class
      * `...` - additional selector options

For example...

```
{
  "name": "my-network",
  "cluster": "test-cluster",
  "components": {
    "foo": {
      "name": "foo",
      "type": "verticle",
      "main": "foo.js",
      "config": {
        "foo": "bar"
      },
      "instances": 2
    },
    "bar": {
      "name": "bar",
      "type": "module",
      "module": "com.foo~bar~1.0",
      "instances": 4
    }
  },
  "connections": [
    {
      "source": {
        "component": "foo",
        "port": "out"
      },
      "target": {
        "component": "bar",
        "port": "in"
      },
      "selector": {
        "type": "fair"
      }
    }
  ]
}
```

JSON network configurations can be used to deploy Vertigo networks from the command
line using the `vertx` command line tool. For more information see
[deploying networks from the command line](#deploying-networks-from-the-command-line)

## Components
Networks are made up of any number of *components* which are simply Vert.x verticles or
modules that are connected together according to the network configuration. Each component
is a "black box" that receives input on named input ports and sends output to named output
ports. By their nature, components do not know from where they received messages or to where
they're sending messages.

### Creating a component
To create a Java component, extend the base `ComponentVerticle` class.

```java
public class MyComponent extends ComponentVerticle {

  @Override
  public void start() {
  
  }

}
```

The `ComponentVerticle` base class is simply a special extension of the Vert.x `Verticle`
that synchronizes with other components in the network at startup and provides Vertigo
specific APIs. Once the component has completed startup it will call the `start()` method
just like any other verticle.

### The elements of a Vertigo component
The `ComponentVerticle` base class provides the following additional `protected` fields:
* `vertigo` - a `Vertigo` instance
* `cluster` - the Vertigo `Cluster` to which the component belongs
* `input` - the component's `InputCollector`, an interface to input ports
* `output`- the component's `OutputCollector`, an interface to output ports
* `logger` - the component's `PortLogger`, a special logger that logs messages to output ports

The most important of these variables is the `input` and `output` objects on which messages
are received and sent respectively. In Vertigo, messages flow in only one direction, so
messages can only be received on input ports and sent to output ports.

## Messaging
The Vertigo messaging API is simply a wrapper around the Vert.x event bus.
Vertigo messages are not sent through any central router. Rather, Vertigo uses
network configurations to create direct event bus connections between components.
Vertigo components send and receive messages using only output and input *ports*
and are hidden from event bus address details which are defined in network configurations.
This is the element that makes Vertigo components reusable.

Rather than routing messages through a central router, components communicate
directly with one another over the event bus, ensuring optimal performance.

![Direct connections](http://s21.postimg.org/65oa1e3dj/Untitled_Diagram_1.png)

Vertigo messages are guaranteed to arrive *in the order in which they were sent*
and to only be processed *exactly once*. Vertigo also provides an API
that allows for logical grouping and ordering of collections of messages known as
[groups](#working-with-message-groups). Groups are strongly ordered named batches
of messages that can be nested.

For more information on messaging see [how Vertigo handles messaging](#how-vertigo-handles-messaging)

### Sending messages on an output port
To reference an output port, use the `output.port(String name)` method.

```java
OutputPort port = output.port("out");
```

If the referenced output port is not defined in the network configuration, the
port will be lazily created, though it will not actually reference any connections.

Any message that can be sent on the Vert.x event bus can be sent on the output port.
To send a message on the event bus, simply call the `send` method.

```java
output.port("out").send("Hello world!");
```

Internally, Vertigo will route the message to any connections as defined in the
network configuration.

Output ports also support custom message serialization.
See [providing serializeable messages](#providing-serializeable-messages)

### Receiving messages on an input port
Input ports are referenced in the same was as output ports.

```java
InputPort port = input.port("in");
```

To receive messages on an input port, register a message handler on the port.

```java
input.port("in").messageHandler(new Handler<String>() {
  public void handle(String message) {
    output.port("out").send(message);
  }
});
```

Note that Vertigo messages arrive in plain format and not in any sort of `Message`
wrapper. This is because Vertigo messages are inherently uni-directional, and message
acking is handled internally.

### Working with message groups
Vertigo provides a mechanism for logically grouping messages appropriately
named *groups*. Groups are named logical collections of messages that are strongly
ordered by name. Before any given group can stat, each of the groups of the same
name at the same level that preceded it must have been completed. Additionally,
messages within a group are *guaranteed to be delivered to the same instance* of each
target component. In other words, routing is performed per-group rather than per-message.

![Groups](http://s30.postimg.org/655svvk3l/groups.png)

When a new output group is created, Vertigo will await the completion of all groups
of the same name that were created prior to the new group before sending the new group's
messages.

```java
output.port("out").group("foo", new Handler<OutputGroup>() {
  public void handle(OutputGroup group) {
    group.send("foo").send("bar").send("baz").end();
  }
});
```

Note that the group's `end()` method *must* be called in order to indicate completion of
the group. *Groups are fully asynchronous*, meaning they support asynchronous calls to other
APIs, and this step is crucial to that functionality.

```java
output.port("out").group("foo", new Handler<OutputGroup>() {
  public void handle(final OutputGroup group) {
    someObject.someAsyncApi(new Handler<AsyncResult<String>>() {
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          group.send(result.result()).end();
        }
      }
    });
  }
});
```

The `OutputGroup` API exposes the same methods as the `OutputPort`. That means that groups
can be nested and Vertigo will still guarantee ordering across groups.

```java
output.port("out").group("foo", new Handler<OutputGroup>() {
  public void handle(OutputGroup group) {
    group.group("bar", new Handler<OutputGroup>() {
      public void handle(OutputGroup group) {
        group.send(1).send(2).send(3).end();
      }
    });
    group.group("baz", new Handler<OutputGroup>() {
      public void handle(OutputGroup group) {
        group.send(4).send(5).send(6).end();
      }
    });
    // Since two child groups were created, this group will not be ended
    // until both children have been ended.
    group.end();
  }
});
```


As with receiving messages, to receive message groups register a handler on an
input port using the `groupHandler` method, passing a group name as the first
argument.

```java
input.port("in").groupHandler("foo", new Handler<InputGroup>() {
  public void handle(InputGroup group) {
    group.messageHandler(new Handler<String>() {
      public void handle(String message) {
        output.port("out").send(message);
      }
    });
  }
});
```

The `InputGroup` API also supports a `startHandler` and `endHandler`. The `endHandler`
can be particularly useful for aggregations. Vertigo guarantees that if a group's
`endHandler` is called then *all* of the messages sent for that group were received
by that group.

```java
input.port("in").groupHandler("foo", new Handler<InputGroup>() {
  public void handle(InputGroup group) {

    final Set<String> messages = new HashSet<>();

    group.messageHandler(new Handler<String>() {
      public void handle(String message) {
        messages.add(message);
      }
    });

    group.endHandler(new Handler<Void>() {
      public void handle(Void ignore) {
        System.out.println("Received " + messages.size() + " messages in group.");
      }
    });
  }
});
```

As with output groups, input groups can be nested, representing the same structure
sent by an output group.

```java
input.port("in").groupHandler("foo", new Handler<InputGroup>() {
  public void handle(InputGroup group) {
    group.group("bar", new Handler<InputGroup>() {
      public void handle(InputGroup group) {
        group.messageHandler(new Handler<Integer>() {
          public void handle(Integer number) {
            output.port("bar").send(number);
          }
        });
      }
    });
    group.group("baz", new Handler<InputGroup>() {
      public void handle(InputGroup group) {
        group.messageHandler(new Handler<String>() {
          public void handle(String string) {
            output.port("baz").send(string);
          }
        });
      }
    });
  }
});
```

### Working with message batches
Batches are similar to groups in that they represent collections of messages.
Batches even use a similar API to groups. However, batches differ from groups
in that they represent collections of output to all connections. In other words,
whereas groups are guaranteed to always be delivered to the same target component
instance, batches use normal selection routines to route each individual message.
Additionally, batches cannot be nested like groups, but groups can be contained
within batches. Batches simply represent windows of output from a port.

![Batches](http://s30.postimg.org/dwmiufo8x/groups_1.png)

The batch API works similarly to the group API, but batches are *not* named.
```java
output.port("out").batch(new Handler<OutputBatch>() {
  public void handle(OutputBatch batch) {
    batch.send("foo").send("bar").send("baz").end();
  }
});
```

Just as with groups, batches need to be explicitly ended. However, only one batch
can be open for any given connection at any given time, so that means that a new
batch will not open until the previous batch has been ended.

On the input port side, the batch API works similarly to the group API.

```java
input.port("in").batchHandler(new Handler<InputBatch>() {
  public void handle(InputBatch batch) {

    // Aggregate all messages from the batch.
    final JsonArray messages = new JsonArray();
    batch.messageHandler(new Handler<String>() {
      public void handle(String message) {
        messages.add(message);
      }
    });

    // Send the aggregated array once the batch is ended.
    batch.endHandler(new Handler<Void>() {
      public void handle(Void event) {
        output.port("out").send(messages);
      }
    });
  }
});
```

Batches cannot be nested, but they can contain groups.
```java
output.port("out").batch(new Handler<OutputBatch>() {
  public void handle(OutputBatch batch) {
    batch.group("fruits", new Handler<OutputGroup>() {
      public void handle(OutputGroup group) {
        group.send("apple").send("banana").send("peach").end();
      }
    });
    batch.end();
  }
});
```

Even if a batch is ended, it will not internally end and allow the next batch to
be created until any child groups have been successfully ended.

Groups within batches can be received in the same manner as they are with groups.

```java
input.port("in").batchHandler(new Handler<InputBatch>() {
  public void handle(InputBatch batch) {

    batch.groupHandler("fruits", new Handler<InputGroup>() {
      public void handle(InputGroup group) {

        final Set<String> fruits = new HashSet<>();
        group.messageHandler(new Handler<String>() {
          public void handle(String message) {
            fruits.add(message);
          }
        });

        group.endHandler(new Handler<Void>() {
          public void handle(Void event) {
            System.out.println("Got all the fruits!");
          }
        });
      }
    });

  }
});
```

### Providing serializable messages
In addition to types supported by the Vert.x event bus, the Vertigo messaging
framework supports any `Serializable` Java object.

```java
public class MyMessage implements Serializeable {
  private String foo;
  private int bar;

  public MyMessage(String foo, int bar) {
    this.foo = foo;
    this.bar = bar;
  }
}
```

## Network Deployment and Clustering
Vertigo provides its own cluster management framework on top of the Vert.x cluster.
Each Vertigo network will always be deployed in a Vertigo cluster. Vertigo clusters
can be deployed either within a single, non-clustered Vert.x instance or across a
Vert.x cluster. Clusters provide a logical separation between different applications
within a Vert.x cluster and provide additional features such as failover.

### Starting a cluster from the command line
Vertigo provides a special Vert.x module for starting a Vertigo cluster agent. To
start a cluster node simply start the `net.kuujo~vertigo-cluster~0.7.0-beta2` module.

```
vertx runmod net.kuujo~vertigo-cluster~0.7.0-beta2
```

The cluster agent accepts a few important configuration options:
* `cluster` - the event bus address of the cluster to which the node belongs. Defaults
   to `vertigo`
* `group` - the HA group to which the node belongs. For simplicity, the Vertigo HA
  mechanism is modeled on the core Vert.x HA support.
* `address` - the event bus address of the node. Defaults to a `UUID` based string.
* `quorum` - the HA quorum size. See the Vert.x HA documentation on quorums.

### Starting a cluster programmatically
Vertigo also provides an API for deploying clusters or individual nodes through the
Vert.x `Container`.

```java
Vertigo vertigo = new Vertigo(this);
vertigo.deployCluster("test-cluster", new Handler<AsyncResult<ClusterManager>>() {
  public void handle(AsyncResult<ClusterManager> result) {
    ClusterManager cluster = result.result();
  }
});
```

There are several methods for deploying nodes or clusters within the current
Vert.x instance.

* `deployCluster(String address)`
* `deployCluster(String address, Handler<AsyncResult<ClusterManager>> doneHandler)`
* `deployCluster(String address, int nodes)`
* `deployCluster(String address, int nodes, Handler<AsyncResult<ClusterManager>> doneHandler)`

Users should use this API rather than deploying the `ClusterAgent` verticle directly
because the cluster agent is pluggable. To override the default cluster agent
set the system property `net.kuujo.vertigo.cluster`.

### Referencing a cluster programmatically
Network deployments are performed through the `ClusterManager` API. To get a
`ClusterManager` instance for a running Vertigo cluster call the `getCluster` method

```java
ClusterManager cluster = vertigo.getCluster("test-cluster");
```

### Accessing a cluster through the event bus
The cluster system is built on worker verticles that are accessed over the event bus.
Cluster agents expose an event bus API that can be used as an alternative to the
Java API. Since all Java interface methods simply wrap the event bus API, you can
perform all the same operations as are available through the API over the event bus.

To send a message to a cluster simply send the message to the cluster address.

```java
JsonObject message = new JsonObject()
    .putString("action", "check")
    .putString("network", "test");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObejct>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      boolean isDeployed = reply.body().getBoolean("result");
    }
  }
});
```

Each message must contain an `action` indicating the action to perform. Each API
method has an action associated with it, and the actions and their arguments will
be outline in the following documentation.

### Deploying a network
To deploy a network use the `deployNetwork` methods on the `ClusterManager` for
the cluster to which the network should be deployed.

```java
NetworkConfig network = vertigo.createNetwork("test");
network.addComponent("foo", "foo.js", 2);
network.addComponent("bar", "bar.py", 4);
network.createConnection("foo", "out", "bar", "in");

cluster.deployNetwork(network);
```

When the network is deployed, the cluster will check to determine whether a
network of the same name is already running in the cluster. If a network of
the same name is running, the given network configuration will be *merged*
with the running network's configuration and the missing components will be
deployed. This is very important to remember. Deployment will *not* fail if
you deploy a network with the same name of a network that already running
in the given cluster.

To determine when the network has been successfully deployed pass an `AsyncResult`
handler to the `deployNetwork` method.

```java
cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwor>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    if (result.succeeded()) {
      ActiveNetwork network = result.result();
    }
  }
});
```

You can also deploy the network from the `Vertigo` API by naming the cluster
to which to deploy the network.

```java
vertigo.deployNetwork("test-cluster", network, new Handler<AsyncResult<ActiveNetwor>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    if (result.succeeded()) {
      ActiveNetwork network = result.result();
    }
  }
});
```

### Deploying a network from JSON
Networks can be deployed programmatically from JSON configurations. To deploy
a network from JSON configuration simply pass the `JsonObject` configuration
in place of the `NetworkConfig`

```java
JsonObject network = new JsonObject()
    .putString("name", "test")
    .putObject("components", new JsonObject()
        .putObject("foo", new JsonObject()
            .putString("type", "verticle").putString("main", "foo.js")));

cluster.deployNetwork(network);
```

JSON configurations can also be used to deploy networks to a cluster over the
event bus. To deploy a network over the event bus send a `deploy` message to
the cluster address with a `network` type, specifying the `network` configuration
as a `JsonObject`.

```
{
  "action": "deploy",
  "type": "network",
  "network": {
    "name": "test",
    "components": {
      "foo": {
        "type": "verticle",
        "main": "foo.js"
      }
    }
  }
}
```

If successful, the cluster will reply with a `status` of `ok`.

```java
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      // Network was successfully deployed!
    }
  }
});
```

For information on the JSON configuration format see
[creating networks from json](#creating-networks-from-json)

### Undeploying a network
To undeploy a *complete* network from a cluster call the `undeployNetwork`
method, passing the network name as the first argument.

```java
cluster.undeployNetwork("test", new Handler<AsyncResult<Void>>() {
  public void handle(AsyncResult<Void> result) {
    if (result.succeeded()) {
      // Network has been undeployed.
    }
  }
});
```

The `AsyncResult` handler will be called once all the components within the network
have been undeployed from the cluster.

The `undeployNetwork` method also supports a `NetworkConfig`. The configuration based
undeploy method behaves similarly to the `deployNetwork` method in that the given
configuration will be *unmerged* from the configuration that's running in the cluster.
If the configuration lists all the components that are present in the running network
then the network will be completely undeployed, otherwise only the listed components
will be undeployed and the network will continue to run. For this reason it is
*strongly recommended* that you undeploy a network by name if you intend to undeploy
the entire network.

Like the `deployNetwork` method, `undeployNetwork` has an equivalent event bus based
action. To undeploy a network over the event bus use the `undeploy` action, specifying
`network` as the `type` to undeploy along with the `network` to undeploy.

```
{
  "action": "undeploy",
  "type": "network",
  "network": "test"
}
```

The `network` field can also contain a JSON configuration to undeploy. If the undeployment
is successful, the cluster will reply with a `status` of `ok`.

```java
JsonObject message = new JsonObject()
  .putString("action", "undeploy")
  .putString("type", "network")
  .putString("network", "test");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      // Network was successfully undeployed!
    }
  }
});
```

### Checking if a network is deployed
To check if a network is deployed in the cluster use the `isDeployed` method.

```java
cluster.isDeployed("test", new Handler<AsyncResult<Boolean>>() {
  public void handle(AsyncResult<Boolean> result) {
    if (result.succeeded()) {
      boolean deployed = result.result(); // Whether the network is deployed.
    }
  }
});
```

When checking if a network is deployed, a `check` message will be sent to the cluster along
with the name of the network to check. If the network's configuration is available in
the cluster then the network will be considered deployed. This is the same check that
the cluster uses to determine whether a network is already deployed when deploying a
new network configuration.

To check whether a network is deployed directly over the event bus, send a `check` message
to the cluster specifying a `network` type along with the `network` name.

```
{
  "action": "check",
  "type": "network",
  "network": "test"
}
```

Send the `check` message to the cluster event bus address. If successful, the cluster will
reply with a boolean `result` indicating whether the network is deployed in the cluster.

```java
JsonObject message = new JsonObject()
  .putString("action", "check")
  .putString("type", "network")
  .putString("network", "test");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      boolean deployed = reply.body().getBoolean("result");
    }
  }
});
```

### Listing networks running in a cluster
To list the networks running in a cluster call the `getNetworks` method.

```java
cluster.getNetworks(new Handler<AsyncResult<Collection<ActiveNetwork>>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    ActiveNetwork network = result.result();
  }
});
```

Note that the method returns an `ActiveNetwork`. The active network can be used
to reconfigure the running network, but more on that later. The current network
configuration can be retrieved from the `ActiveNetwork` by calling the `getConfig`
method.

```java
NetworkConfig config = network.getConfig();
```

To list the networks running in the cluster over the event bus, use the `list`
action.

```
{
  "action": "list"
}
```

If successful, the cluster will reply with a `result` containing an array of
configuration objects for each network running in the cluster.

```java
JsonObject message = new JsonObject()
  .putStrign("action", "list");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      JsonArray networks = reply.body().getArray("result");
    }
  }
});
```

### Deploying a bare network
Vertigo networks can be reconfigured after deployment, so sometimes it's useful
to deploy an empty network with no components or connections.

```java
cluster.deployNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    ActiveNetwork network = result.result();
  }
});
```

When a bare network is deployed, Vertigo simply deploys a network manager
verticle with no components. Once the network is reconfigured, the manager
will automatically update the network with any new components.

To deploy a bare network over the event bus, pass a `String` network name
in the `network` field rather than a JSON network configuration.

```java
JsonObject message = new JsonObject()
  .putString("action", "deploy")
  .putString("type", "network")
  .putString("network", "test");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {
      // Network was successfully deployed!
    }
  }
});
```

### Reconfiguring a network
Vertigo provides several methods to reconfigure a network after it has been
deployed. After a network is deployed users can add or remove components or
connections from the network. To reconfigure a running network simply deploy
or undeploy a network configuration of the same name.

```java
// Create and deploy a two component network.
NetworkConfig network = vertigo.createNetwork("test");
network.addComponent("foo", "foo.js", 2);
network.addComponent("bar", "bar.py", 4);

vertigo.deployNetwork("test-cluster", network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    // Create and deploy a connection between the two components.
    NetworkConfig network = vertigo.createNetwork("test");
    network.createConnection("foo", "out", "bar", "in");
    vertigo.deployNetwork("test-cluster", network);
  }
});
```

When a network is deployed, the cluster will check to see if any other networks
of the same name are already deployed. If a network of the same name is deployed
then the new configuration will be merged with the running configuration.
Similarly, when undeploying a network from configuration, the cluster will undeploy
only the components and connections listed in a connection. The network will only
be completely undeployed if the configuration lists all the components deployed in
the network.

Vertigo queues configuration changes internally to ensure that only one configuration
change can occur at any given time. So if you separately deploy two connections,
the second connection will not be added to the network until the first has been
added and connected on all relevant components. To deploy more than one component
or connection to a running network simultaneously just list them in the same
configuration.

Just as networks can be deployed and undeployed over the event bus, they can also
be reconfigured by sending `deploy` and `undeploy` messages to the cluster.

### Working with active networks
Vertigo provides a special API for reconfiguring running networks known as the
*active network*. The `ActiveNetwork` API mimics the network configuration API,
except changes to an `ActiveNetwork` instance will be immediately deployed to
the running network in the appropriate cluster.

To load an active network you can call `getNetwork` on a cluster.

```java
cluster.getNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    ActiveNetwork network = result.result();
    network.createConnection("foo", "out", "bar", "in");
  }
});
```

The active network API also supports `AsyncResult` handlers so you can determine
when the network has been updated with the new configuration.

```java
network.createConnection("foo", "out", "bar", "in", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    // Connection has been added and connected.
  }
});
```

Each `ActiveNetwork` also contains an internal `NetworkConfig` which can be
retrieved by the `getConfig` method.

```java
NetworkConfig config = network.getConfig();
```

The active network's internal `NetworkConfig` will be automatically updated when
the running network configuration is updated.

### Deploying a network from the command line
Vertigo provides a special facility for deploying networks from json confguration files.
This feature is implemented as a Vert.x language module, so the network deployer must
be first added to your `langs.properties` file.

```
network=net.kuujo~vertigo-deployer~0.7.0-beta2:net.kuujo.vertigo.NetworkFactory
.network=network
```

You can replace the given extension with anything that works for you. Once the language
module has been configured, simply run a network configuration file like any other
Vert.x verticle.

```
vertx run my_network.network
```

The `NetworkFactory` will construct the network from the json configuration file and
deploy the network to the available cluster.

## Cluster Management
Vertigo clusters support remote deployments over the event bus through
[Xync](http://github.com/kuujo/xync). Users can use the Vertigo cluster API to
remotely deploy Vert.x modules and verticles from Vertigo components.

### Accessing the cluster from within a component
The Xync cluster is made available to users through the `cluster` field within
the `ComponentVerticle` class. The `cluster` within any given component will always
reference the Vertigo cluster to which the component's parent network belongs. This
means that deployments made through the `cluster` will be separated in the same
way that networks are separated from each other across clusters.

### Deploying modules and verticles to the cluster
The Vertigo cluster supports remote deployment of modules and verticles over the
event bus. The `Cluster` API wraps the event bus API and mimics the core Vert.x
`Container` interface. To deploy a module or verticle simply call the appropriate
method on the component `Cluster` instance:

```java
public class MyComponent extends ComponentVerticle {

  @Override
  public void start() {
    cluster.deployVerticle("foo", "foo.js", new Handler<AsyncResult<String>>() {
      public void handle(AsyncResult<String> result) {
        if (result.succeeded()) {
          // Successfully deployed the verticle!
        }
      }
    });
  }

}
```

The `Cluster` API differs in one important aspect from the `Container` API. Because
cluster deployments are remote, users must provide an *explicit* deployment ID for
each deployment. This ensures that even if the instance from which the module/verticle
was deployed fails, the deployment can still be referenced from different Vert.x
instances. If the deployment ID of a deployment already exists then the deployment
will fail.

The internal component cluster is the same cluster to which the component's parent network
belongs. That means that deployment IDs are unique to each cluster. You can deploy
a module with the deployment ID `foo` in two separate clusters at the same time.

### Undeploying modules and verticles from the cluster
To undeploy a module or verticle from the cluster call the `undeployModule` or
`undeployVerticle` method, using the user-defined deployment ID.

```java
cluster.undeployVerticle("foo");
```

### Deploying modules and verticles with HA
Like Vert.x clusters, the Vertigo clusters supports HA deployments. By default, modules
and verticles are not deployed with HA enabled.

```java
cluster.deployVerticle("foo", "foo.js", null, 1, true);
```

The last argument in the arguments list indicates whether to deploy the deployment
with HA. When a Vertigo cluster node fails, any deployments deployed with HA enabled
on that node will be taken over by another node within the same group within the cluster.

### Checking if a module or verticle is deployed
Since deployment IDs in Vertigo clusters are user-defined, users can determine whether
a module or verticle is already deployed with a specific deployment ID. To check if
a deployment is already deployed in the cluster use the `isDeployed` method.

```java
cluster.isDeployed("foo", new Handler<AsyncResult<Boolean>>() {
  public void handle(AsyncResult<Boolean> result) {
    boolean deployed = result.result(); // Whether the module or verticle is deployed
  }
});
```

To check if a module or verticle is deployed over the event bus send a `check` message
to the cluster, specifying the `module` or `verticle` as the check `type`.

```
{
  "action": "check",
  "type": "verticle",
  "id": "foo"
}
```

If the request is successful, the cluster will reply with a `result` containing a
boolean indicating whether the deployment ID exists in the cluster.

### Working with HA groups
Vertigo's HA grouping mechanism is intentionally designed to mimic the core HA behavior.
Each Vertigo node can be assigned to a specific HA group, and when a node fails its HA
deployments will be taken over by another node in the same group.

Modules or verticles can also be deployed directly to a specific HA group. To deploy
a module or verticle to an HA group call the `deployModuleTo` or `deployVerticleTo`
methods respectively, passing the target HA group as the second argument (after the
deployment ID).

```java
cluster.deployVerticleTo("foo", "my-group", "foo.js");
```

By default, all deployments are deployed to the `__DEFAULT__` HA group.

## Cluster-wide shared data
Cluster-wide shared data structures are made available via the same API as clustering.
If the current Vert.x instance is a Hazelcast clustered instance then cluster-wide
shared data structures will be backed by Hazelcast data structures. If the current
Vert.x instance is not clustered then data structures will be backed by Vert.x
`SharedData` structures.

The cluster API is available in all components via the `cluster` field of the
`ComponentVerticle`.

### AsyncMap
The `AsyncMap` interface closely mimics the interface of the Java `Map` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

```java
final AsyncMap<String, String> map = cluster.getMap("foo");
map.put("foo", "bar", new Handler<AsyncResult<String>>() {
  public void handle(AsyncResult<String> result) {
    if (result.succeeded()) {
      map.get("foo", new Handler<AsyncResult<String>>() {
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            String foo = result.result();
          }
        }
      });
    }
  }
});
```

If the Vert.x instance is not clustered then Vertigo maps will be backed by
the Vert.x `ConcurrentSharedMap`. If the Vert.x instance is clustered then maps
will be backed by Hazelcast maps that are accessed over the event bus in a Xync
worker verticle to prevent blocking the event loop.

### AsyncSet
The `AsyncSet` interface closely mimics the interface of the Java `Set` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

```java
final AsyncSet<String> set = cluster.getSet("foo");
set.add("bar", new Handler<AsyncResult<Boolean>>() {
  public void handle(AsyncResult<Boolean> result) {
    if (result.succeeded()) {
      set.remove("bar");
    }
  }
});
```

If the Vert.x instance is not clustered then Vertigo sets will be backed by
the Vert.x `SharedData` sets. If the Vert.x instance is clustered then sets
will be backed by Hazelcast sets that are accessed over the event bus in a Xync
worker verticle to prevent blocking the event loop.

### AsyncList
The `AsyncList` interface closely mimics the interface of the Java `List` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

```java
AsyncList<String> list = cluster.getList("foo");
list.add("bar", new Handler<AsyncResult<Boolean>>() {
  public void handle(AsyncResult<Boolean> result) {
    if (result.succeeded()) {
      list.remove(0);
    }
  }
});
```

If the Vert.x instance is not clustered then Vertigo lists will be backed by
a custom list implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then lists will be backed by Hazelcast lists that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### AsyncQueue
The `AsyncQueue` interface closely mimics the interface of the Java `Queue` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

```java
final AsyncQueue<String> queue = cluster.getQueue("foo");
queue.add("bar", new Handler<AsyncResult<Boolean>>() {
  public void handle(AsyncResult<Boolean> result) {
    if (result.succeeded()) {
      queue.poll(new Handler<AsyncResult<String>>() {
        public void handle(AsyncResult<String> result) {
          if (result.succeeded()) {
            String value = result.result();
          }
        }
      });
    }
  }
});
```

If the Vert.x instance is not clustered then Vertigo queues will be backed by
a custom queue implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then queues will be backed by Hazelcast queues that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### AsyncCounter
The `AsyncCounter` facilitates generating cluster-wide counters.

```java
AsyncCounter counter = cluster.getCounter("foo");
counter.incrementAndGet(new Handler<AsyncResult<Long>>() {
  public void handle(AsyncResult<Long> result) {
    if (result.succeeded()) {
      long value = result.result();
    }
  }
});
```

If the Vert.x instance is not clustered then Vertigo counters will be backed by
a custom counter implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then counters will be backed by Hazelcast maps that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### Accessing shared data over the event bus
As with network and module/verticle deployments, cluster-wide shared data structures
can be accessed directly over the event bus. Data actions relate directly to their
API methods. Each shared data message must contain a `type` and the `name` of the
data structure to which the message refers. For example, to `put` a value in the
`foo` map we do the following:

```java
// Put key "bar" to "baz" in map "foo"
JsonObject message = new JsonObject()
  .putString("type", "map")
  .putString("name", "foo")
  .putString("action", "put")
  .putString("key", "bar")
  .putString("value", "baz");
vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
  public void handle(Message<JsonObject> reply) {
    if (reply.body().getString("status").equals("ok")) {

      // Get the value of key "bar" in map "foo"
      JsonObject message = new JsonObject()
        .putString("type", "map")
        .putString("name", "foo")
        .putString("action", "get")
        .putString("key", "bar");
      vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
        public void handle(Message<JsonObject> reply) {
          if (reply.body().getString("status").equals("ok")) {
            String value = reply.body().getString("result");
          }
        }
      });

    }
  }
});
```

## Hooks
Vertigo provides a special mechanism for hooking into component and messaging
events. Hooks are objects that are added to the network configuration and receive
notifications when certain events occur. All hooks implement the `JsonSerializable`
interface which handles JSON serialization with Jackson. In most cases, users
can simply implement the relevant hook interface and Vertigo will handle serialization
of basic fields automatically. You can use Jackson annotations to ignore certain
fields or provide custom serialization as necessary.

### InputHook
Input hooks can be used to hook into message events related to the target port
on a specific connection within a network configuration. Input hooks are added
to the `target` element of a `ConnectionConfig`.

To define an input hook implement the `InputHook` interface.

```java
public class MyInputHook implements InputHook {

  @Override
  public void handleReceive(Object message) {
  
  }

}
```

The `InputHook` interface requires only a single `handleReceive` method which
will be called whenever a message is received on the connection.

To add the hook to a connection use the `addHook` method on a `ConnectionConfig.Target`
instance.

```java
network.createConnection("foo", "out", "bar", "in").getTarget().addHook(new MyInputHook());
```

### OutputHook
Output hooks can be used to hook into message events related to the source port
on a specific connection within a network configuration. Output hooks are added
to the `source` element of a `ConnectionConfig`.

To define an output hook implement the `OutputHook` interface.

```java
public class MyOutputHook implements OutputHook {

  @Override
  public void handleSend(Object message) {
  
  }

}
```

The `OutputHook` interface requires only a single `handleSend` method which
will be called whenever a message is sent on the connection.

To add the hook to a connection use the `addHook` method on a `ConnectionConfig.Source`
instance.

```java
network.createConnection("foo", "out", "bar", "in").getSource().addHook(new MyOutputHook());
```

### IOHook
I/O hooks are a combination of the `InputHook` and `OutputHook` interfaces. When an
I/O hook is added to a connection, its methods will be called when an event occurs
on either side of the connection. For the sending side of the connection, the hook
will be called when a message is sent, and for the receiving side of the connection,
the hook will be called when a message is received.

```java
public class MyIOHook implements IOHook {

  @Override
  public void handleSend(Object message) {
  
  }

  @Override
  public void handleReceive(Object message) {
  
  }

}
```

To add the hook to a connection use the `addHook` method on a `ConnectionConfig` instance.

```java
network.createConnection("foo", "out", "bar", "in").addHook(new MyIOHook());
```

### ComponentHook
Components hooks are component-level hooks that implement both the `InputHook`
and `OutputHook` interfaces along with some additional component-level hook methods.
Since component hooks are added at the component level, the `handleSend` and
`handleReceive` methods will be called each time a message is sent or received
on *any* port. Additionally, the component hook can receive notifications of when
the component has started and stopped as well.

```java
public class MyComponentHook implements ComponentHook {

  @Override
  public void handleStart(Component component) {
  
  }

  @Override
  public void handleSend(Object message) {
  
  }

  @Override
  public void handleReceive(Object message) {
  
  }

  @Override
  public void handleStop(Component component) {
  
  }

}
```

The `handleStart` and `handleStop` will be passed the internal Vertigo
`Component` instance which contains all fields available to the Java `ComponentVerticle`
along with additional information about the component configuration.

Component hooks are added to `ComponentConfig` instances within the network configuration.

```java
network.addComponent("foo", "foo.js", 2).addHook(new MyComponentHook());
```

## Logging
Each Vertigo component contains a special `PortLogger` which logs messages
to component output ports in addition to standard Vert.x log files. This allows
other components to listen for log messages on input ports.

The `PortLogger` logs to ports named for each logger method:
* `fatal`
* `error`
* `warn`
* `info`
* `debug`
* `trace`

### Logging messages to output ports
The `PortLogger` simple implements the standard Vert.x `Logger` interface.
So, to log a message to an output port simply call the appropriate log method:

```java
public class MyComponent extends ComponentVerticle {

  @Override
  public void start() {
    logger.info("Component started successfully!");
  }

}
```

### Reading log messages
To listen for log messages from a component, simply add a connection to a network
configuration listening on the necessary output port. For instance, you could
aggregate and count log messages from one component by connecting each log port to
a single input port on another component.

```java
NetworkConfig network = vertigo.createNetwork("log-test");
network.addVerticle("logger", "logger.js", 2);
network.addVerticle("log-reader", LogReader.class.getName(), 2);
network.createConnection("logger", "fatal", "log-reader", "log").hashSelect();
network.createConnection("logger", "error", "log-reader", "log").hashSelect();
network.createConnection("logger", "warn", "log-reader", "log").hashSelect();
network.createConnection("logger", "info", "log-reader", "log").hashSelect();
network.createConnection("logger", "debug", "log-reader", "log").hashSelect();
network.createConnection("logger", "trace", "log-reader", "log").hashSelect();
```

With a hash selector on each connection, we guarantee that the same log message
will always go to the same `log-reader` instance.

Log messages will arrive as simple strings:

```java
public class LogReader extends ComponentVerticle {
  private final Map<String, Integer> counts = new HashMap<>();

  @Override
  public void start() {
    input.port("log").messageHandler(new Handler<String>() {
      public void handle(String message) {
        // Update the log message count.
        if (!counts.containsKey(message)) {
          counts.put(message, 1);
        } else {
          counts.put(message, counts.get(message) + 1);
        }
        output.port("count").send(counts.get(message)); // Send the updated count.
      }
    });
  }

}
```

## How it works
There are four essential components to Vertigo's design:
* [Configurations](#configurations)
* [Cluster](#clusters)
* [Networks](#networks)
* [Components](#components)
* [Communication](#communication)

This section outlines how each of the components of Vertigo is designed and how
they interact with one another in order to support advanced features such as
fault-tolerant deployments, runtime configuration changes, strongly-ordered
messaging, and exactly-once processing.

### Configurations
At the core of Vertigo's networks are immutable configurations called contexts.
Every element of a network has an associated context. When a network is first
deployed, Vertigo constructs a version-controlled context from the network's
configuration. This is the point at which Vertigo generates things like unique
IDs and event bus addresses.

[ContextBuilder](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/impl/ContextBuilder.java)

The context used by each element for setup tasks such as connecting to the
cluster, creating ports and connections, and registering hooks.

### Cluster
The Vertigo cluster is the component that manaages deployment, undeployment,
and monitoring of networks and their components. Vertigo clusters consist of
one or more special verticles that expose an event bus interface to deploying
modules, verticles, and complete networks as well as cluster-wide shared data.

The verticle implementation that handles clustering is the
[ClusterAgent](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java)
The cluster agent is an extension of the [Xync](http://github.com/kuujo/xync)
verticle. Remote module and verticle deployments, failover, and cluster-wide
shared data are provided by the Xync verticle, while network-specific logic
is implemented in the Vertigo `ClusterAgent`.

The Vertigo cluster makes heavy use of cluster-wide shared data for coordination.
This is the element of the cluster that supports deploying/undeploying partial
network configurations. When a network is deployed to the cluster, the cluster
will first [determine whether a network of the same name is already deployed in
the cluster](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java#L267).
If the network is running in the cluster, the cluster will load the running network's
configuration and [merge the new configuration with the existing configuration](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java#L258),
otherwise the network will be completely deployed.

### Networks
But the cluster doesn't ever actually deploy any of the network's components.
Instead, the cluster simply deploys a special verticle called the
[network manager](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/network/manager/NetworkManager.java)
which handles deployment/undeployment of components and coordinates startup
and shutdown of networks. Rather than communicating over the event bus, the
cluster and the network communicate using data-driven events through shared
data structures. When the cluster wants to update a network, it [sets the
network's configuration key](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java#L333)
in the cluster. Similarly, the network
[sets a status key](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java#L308)
once the configuration has been installed. This allows Vertigo's network
configurations to be persisted in the cluster through crashes and thus means
that networks can easily recover.

Since each network manager always monitors the network's configuration for
changes, it is automatically notified when the cluster updates the configuration.
When a network configuration change occurs, the manager will first
[unset the network's status key](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/network/manager/NetworkManager.java#L427)
to indicate that the network is not currently completely set up. This gives the
network's components an opportunity to pause if necessary. Once the status key
has been unset the network will [undeploy any removed components](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/network/manager/NetworkManager.java#L323)
and then [deploy any new components](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/network/manager/NetworkManager.java#L354).
While new components are being added, the manager will also update each component's
configuration in the cluster. With components also watching their own configurations
for changes, this allows components to update their internal connections without
being undeployed, but more on that in the next section.

### Components
One of the challenges when starting up multiple verticles across a cluster is
coordinating startup. If a component begins sending messages on a connection
before the other side is listening, messages will be lost. It is the responsibility
of the [component coordinator](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/component/impl/DefaultComponentCoordinator.java)
to notify the cluster once a component has completed startup.

To do so, coordinators use the same mechanism that clusters and network managers
use to communicate status information - cluster-wide shared data. When a component
first starts up, it immediately
[loads its current context from the cluster](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/component/impl/DefaultComponentCoordinator.java#L100)
and watches its configuration key for changes. Once its definite context has
been loaded, the component will open its input and output collectors. Finally,
once the components input and outputs have been opened, the coordinator will
set the component's status key in the cluster, indicating that the component
has completed startup. However, even though the component has indicated to the
network that it has completed startup, the component won't actually start [until
the network has indicated](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/component/impl/DefaultComponent.java#L148)
that *all* the active components in the network have completed setup.

### Communication
One of the most important features in Vertigo is its messaging system. The
messaging framework has been completely redesigned in Vertigo 0.7 to be modeled
on ports. All Vertigo's messaging is performed over the Vert.x event bus, and
the messaging system is designed to provide strongly-ordered and exactly-once
semantics.

There are numerous components to the Vertigo communcation framework. At the
highest level, each component has an `InputCollector` and an `OutputCollector`.

Internally, Vertigo uses *streams* to model connections between an output port
on one set of component instances and an input port on another set of component
instances. Each output port can contain any number of output streams, and each
output stream can contain any number of output connections (equal to the number
of instances of the target component). Connections represent a single event bus
address connection between two instances of two components on a single Vertigo
connection. Connection selectors are used at the stream level to select a set
of connections to which to send each message for the stream.

Vertigo provides strong ordering and exactly-once semantics through a unique,
high-performance algorithm wherein messages are essentially batched between
connections. When a message is sent on an output connection, the connection
tags the message with a monotonically increasing number and the message is
stored in an internal `TreeMap` with the ID as the key. Since Vertigo ensures
that each output connection will only ever communicate with a single input
connection, this monotonically increasing number can be used to check the
order of messages received. Input connections simply store the ID of the
last message they received. When a new message is received, if the ID is
not one plus the last seen ID, the input connection will immediately
[send a *fail* message](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultConnectionInputGroup.java#L90)
back to the output connection, indicating the last message
that the input connection received in order. The output connection will then begin
[resending all stored messages in order](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultOutputConnection.java#L342)
after that point. If no messages are received out of order, the input
connection will periodically
[send an *ack* message](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultInputConnection.java#L205)
to the output connection indicating the last message received.
The output connection will then
[purge its internal storage](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultOutputConnection.java#L328)
of all messages before the indicated identifier. This simple algorithm
allows Vertigo to guarantee strongly-order/exactly-once processing without
the use of event bus reply handlers.

The Vertigo communication framework also supports a couple of different
forms of batching - *batches* and *groups*.

Batches are unique collections of messages *emitted* from a given component
instance. Batches are represented on *all* streams within a given port
during their lifespan. Alternatively, groups are collections of messages
*received* by a given component. That is, groups relate only to a single
stream on a given output port. Additionally, each output port may only
have a single batch open at any given time whereas multiple groups can
be open at any given time.

When a batch is created, since batches relate to all connections in all
streams, *each output stream* will send a `startBatch` message to the other
side of [every connection](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/stream/impl/DefaultOutputStream.java#L139).
However, the batch is not then immediately created. Instead, the other side of
the connection will wait to respond to the start message
[until a message handler has actually been registered](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultConnectionInputBatch.java#L93)
for the batch. This creates a brief paused between the time the batch is created
and the time the batch is started, but it also ensures that no messages can be
sent on the batch until a handler is ready to receive them.

Batches keep track of the number of groups that are created within them. When
a batch is ended, it will not actually send an `endBatch` message to the other
side of the connection until all its child groups (if any) have been completed.

When a group is created, each output stream [selects a single connection with
its internal selector](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/stream/impl/DefaultOutputStream.java#L162).
As with batches, groups will not actually complete creation
[until a message handler has actually be registered](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultConnectionInputGroup.java#L90)
on the other side of the connection. And like batches, groups keep track of the
child groups created within them and cannot be successfully ended until all
child groups have been ended.

**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
