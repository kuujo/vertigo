Vertigo
=======

**Need support? Check out the [Vertigo Google Group][google-group]**

**[Java User Manual](#java-user-manual) | [Javadoc](http://vertigo.kuujo.net/java)**

**[Javascript API][vertigo-js]**

**[Python API][vertigo-python]**

Vertigo is a fast, fault-tolerant, polyglot event processing framework built on the
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

# Getting Started
This is a brief tutorial that describes the basic components of Vertigo
along with a simple example.

## Networks
Networks are collections of Vert.x verticles and modules that are connected
together by input and output ports. Each component in a network contains processing
logic, and connections between components indicate how messages should be
passed between them. Networks can be created either in code or in JSON and
can be deployed in code or from the command line.

## Ports
Components in Vertigo communicate via input and output ports. Messaging in Vertigo
is inherently uni-directional, so each component has a unique set of input and
output ports. Input ports are interfaces to which other components can connect
to send messages, and output ports are interfaces to which other components can
connect to receive messages.

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

Vertigo components are "black boxes," meaning all components can send or receive
messages. Vertigo makes no distinction between the behaviors of different components.

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

Vertigo doesn't route messages through any central router. Rather, messages are sent
between components directly on the Vert.x event bus.

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

This network contains two components. The first component is a Python component
that will feed random words to its `word` out port. The second component is a
Javascript component that will count words received on its `word` in port. The
network therefore defines a connection between the `word-feeder` component's
`word` out port and the `word-counter` component's `word` in port. Vertigo
components can be implemented in a variety of languages since they're just
Vert.x verticles.

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

Once the network has been configured and components have been created,
we deploy the network with the same `Vertigo` API that created it.

```java
vertigo.deployNetwork(network);
```

We can also configure and deploy the network in JSON.

```
{
  "name": "word-count",
  "components": {
    "word-feeder": {
      "type": "verticle",
      "main": "random_word_feeder.py",
    },
    "word-counter": {
      "type": "verticle",
      "main": "word_counter.js",
      "instances": 2
    }
  },
  "connections": [
    {
      "source": {
        "component": "word-feeder",
        "port": "word"
      },
      "target": {
        "component": "word-counter",
        "port": "word"
      }
    }
  ]
}
```

This is a JSON network configuration that is equivalent to the Java
configuration we defined above. The JSON configuration can be deployed
directly from a JSON configuration file using the `vertx` command line
tool.

```
vertx run word_count_network.json
```

# Java User Manual
1. [Introduction](#introduction)
1. [Setup](#setup)
   * [Adding Vertigo as a Maven dependency](#adding-vertigo-as-a-maven-dependency)
   * [Including Vertigo in a Vert.x module](#including-vertigo-in-a-vertx-module)
1. [Networks](#networks)
   * [Creating a new network](#creating-a-new-network)
   * [Adding components to a network](#adding-components-to-a-network)
   * [Creating connections between components](#)
   * [Routing messages between multiple component instances](#routing-messages-between-multiple-component-instances)
   * [Creating networks from JSON](#creating-networks-from-json)
1. [Components](#components)
   * [Creating a component](#creating-a-component)
   * [The elements of a Vertigo component](#the-elements-of-a-vertigo-component)
1. [Messaging](#messaging)
   * [Sending messages on an output port](#sending-messages-on-an-output-port)
   * [Receiving messages on an input port](#receiving-messages-on-an-input-port)
   * [Working with message groups](#working-with-message-groups)
   * [Providing serializeable messages](#providing-serializeable-messages)
1. [Deployment](#deployment)
   * [Deploying a network](#deploying-a-network)
   * [Undeploying a network](#undeploying-a-network)
   * [Reconfiguring a network](#reconfiguring-a-network)
   * [Deploying a bare network](#deploying-a-bare-network)
   * [Working with active networks](#working-with-active-networks)
   * [Deploying networks from the command line](#deploying-networks-from-the-command-line)
1. [Clustering](#clustering)
   * [Configuring cluster scopes](#configuring-cluster-scopes)
   * [Cluster-wide shared data](#cluster-wide-shared-data)
      * [AsyncMap](#asyncmap)
      * [AsyncSet](#asyncset)
      * [AsyncList](#asynclist)
      * [AsyncQueue](#asyncqueue)
      * [AsyncCounter](#asynccounter)
1. [Logging](#logging)
   * [Logging messages to output ports](#logging-messages-to-output-ports)
   * [Reading log messages](#reading-log-messages)
1. [How it works](#how-it-works)
   * [How Vertigo handles messaging](#how-vertigo-handles-messaging)
   * [How Vertigo performs deployments](#how-vertigo-performs-deployments)
   * [How Vertigo coordinates networks](#how-vertigo-coordinates-networks)

## Introduction
Vertigo is a multi-step event processing framework built on Vert.x. It exposes a
very simple yet powerful API defines networks of Vert.x verticles and the relationships
between them in a manner that abstracts communication details from implementations, making
Vertigo components reusable. It provides for advanced messaging requirements such as
strong ordering and exactly-once processing and supports deployment of networks within a
single Vert.x instance or across a cluster of Vert.x instances and performs setup and
coordination internally.

## Setup
To use Vertigo simply add the library as a Maven dependency or as a Vert.x module include.

### Adding Vertigo as a Maven dependency

```
<dependency>
  <groupId>net.kuujo</groupId>
  <artifactId>vertigo</artifactId>
  <version>0.7.0-SNAPSHOT</version>
</dependency>
```

### Including Vertigo in a Vert.x module

To use the Vertigo Java API, you can include the Vertigo module in your module's
`mod.json` file. This will make Vertigo classes available within your module.

```
{
  "main": "com.mycompany.myproject.MyVerticle",
  "includes": "net.kuujo~vertigo~0.7.0-SNAPSHOT"
}
```

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
* `cluster` - the network's cluster configuration
   * `address` - the event bus address to the cluster manager. This only applies
     to networks with a `cluster` scope
   * `scope` - the network's cluster scope, e.g. `local` or `cluster` See [configuring cluster scopes](#configuring-cluster-scopes)
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
  "cluster": {
    "scope": "local"
  },
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

## Components
Networks are made up of any number of *components* which are simply Vert.x verticles or
modules that are connected together according to the network configuration. Each component
is a "black box" that receives input on named input ports and sends output to named output
ports. By their nature, components do not know from where they received messages or from where
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

Vertigo messages are guaranteed to arrive in order. Vertigo also provides an API
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
named *groups*. Groups are logical collections of messages that are strongly
ordered by name. Before any given group can stat, each of the groups of the same
name at the same level that preceded it must have been completed. Additionally,
messages within a group are *guaranteed to be delivered to the same instance* of each
target component. In other words, routing is performed per-group rather than per-message.

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

### Providing serializable messages
The Vertigo messaging system supports custom serialization of messages for
Java. Serializable messages must implement the `JsonSerializeable` interface.

```java
public class MyMessage implements JsonSerializeable {
  private String foo;
  private int bar;

  // An empty constructor must be provided for serialization.
  public MyMessage() {
  }

  public MyMessage(String foo, int bar) {
    this.foo = foo;
    this.bar = bar;
  }
}
```

In most cases, Vertigo's Jackson-based serializer will work with no custom
configuration necessary. Vertigo's default serializer automatically serializes
any basic fields (primitive types, strings, and collections), but Jackson annotations
can be used to provide custom serialization of `JsonSerializeable` objects.

## Deployment
One of the most important tasks of Vertigo is to support deployment and startup
of networks in a consistent and reliable manner. Vertigo supports network deployment
either within a single Vert.x instance (local) or across a cluster of Vert.x instances.
When a Vertigo network is deployed, a special verticle known as the *network manager*
is deployed. The network manager is tasked with managing and monitoring components
within the network, handling runtime configuration changes, and coordinating startup
and shutdown of networks.

Networks can be deployed and configured from any verticle within any node in a Vert.x
cluster. Even if a network is deployed from another verticle, the network can still be
referenced and updated from anywhere in the cluster. Vertigo's internal coordination
mechanisms ensure consistency for deployments across all nodes in a cluster.

For more information on network deployment and coordination see [how it works](#how-it-works)

### Deploying a network
To deploy a network, simply use the `deployNetwork` method.

```java
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);
network.addVerticle("bar", "bar.py", 4);
network.createConnection("foo", "out", "bar", "in");

vertigo.deployNetwork(network);
```

When Vertigo deploys the network, it will automatically detect the current Vert.x
cluster scope. If the current Vert.x instance is a Hazelcast clustered instance,
Vertigo will attempt to deploy the network across the cluster. This behavior can
be configured in the network's configuration.

If the current Vert.x instance is not clustered then all network deployment will
automatically fall back to the Vert.x `Container`. Even if a network is configured
to be deployed locally, Vertigo will still coordinate using Hazelcast if it's
available.

Note that in order to support remote component deployment you must use a
[Xync](http://github.com/kuujo/xync) node or some other facility that supports
event bus deployments.

### Undeploying a network
To undeploy a network, use the `undeployNetwork` method.

```java
vertigo.undeployNetwork("test", new Handler<AsyncResult<Void>>() {
  public void handle(AsyncResult<Void> result) {
    if (result.succeeded()) {
      // Successfully undeployed the network
    }
  }
});
```

Passing a string network name to the method will cause the entire network to
be undeployed. The method also supports a network configuration which can be
used to undeploy portions of the network without undeploying the entire network.
More on that in a minute.

### Reconfiguring a network
Vertigo networks can be reconfigured even after deployment. This is where network
names become particularly important. When a user deploys a network, Vertigo first
determines whether the network is already deployed within the current Vertigo cluster.
If a network with the same name is already deployed, *the given network configuration
will be merged with the existing network configuration* and Vertigo will update
components and connections within the network rather than deploying a new network.
This means that Vertigo networks can be deployed one component or connection at
a time.

```java
// Create and deploy a two component network.
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);
network.addVerticle("bar", "bar.py", 4);

// Deploy the network to the cluster.
vertigo.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {

    // Create and deploy a connection between the two components.
    NetworkConfig network = vertigo.createNetwork("test"); // Note the same network name.
    network.createConnection("foo", "out", "bar", "in");
    vertigo.deployNetwork(network);

  }
});
```

### Deploying a bare network
Since networks can be reconfigured *after* deployment, Vertigo provides a simple
helper method for deploying empty networks that will be reconfigured after deployment.
To deploy an empty network simply deploy a string network name.

```java
vertigo.deployNetwork("test");
```

### Working with active networks
Vertigo provides a helper API for reconfiguring networks known as *active networks*.
The `ActiveNetwork` is a `NetworkConfig` like object that exposes methods that directly
update the running network when called. Obviously, the name is taken from the active
record pattern.

When deploying any network an `ActiveNetwork` instance for the deployed network will
be returned once the deployment is complete.

```java
// Create a network with a single component.
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);

// Deploy the network.
vertigo.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    if (result.succeeded()) {
      // Add another component to the network and create a connection between the two components.
      ActiveNetwork network = result.result();
      network.addVerticle("bar", "bar.py", 2);
      network.createConnection("foo", "out", "bar", "in");
    }
  }
});
```

The `ActiveNetwork` instance contains a reference to the *entire* network configuration,
even if the configuration that was deployed was only a partial network configuration.

You can load an `ActiveNetwork` for an already running network with the `getNetwork` method.

```java
vertigo.getNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    if (result.succeeded()) {
      ActiveNetwork network = result.result();
    }
  }
});
```

### Deploying networks from the command line
Vertigo provides a special facility for deploying networks from json confguration files.
This feature is implemented as a Vert.x language module, so the network deployer must
be first added to your `langs.properties` file.

```
network=net.kuujo~vertigo-deployer~0.7.0-SNAPSHOT:net.kuujo.vertigo.NetworkFactory
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

## Clustering
Vertigo currently provides two different clustering methods. When networks are deployed,
Vertigo will automatically detect the current Vert.x cluster type and adjust its behavior
based on available features. The available cluster scopes are as follows:
* `local` - *Vert.x is not clustered*. Vertigo will use Vert.x `SharedData` for coordination
  of networks and deployments will be performed via the Vert.x `Container`.
* `cluster` - *Vert.x is clustered using the default `HazelcastClusterManager`*. Vertigo will
  coordinate networks through Hazelcast and, for network confgured for the `cluster` scope,
  will attempt to deploy components remotely over the event bus.

For more information on Vertigo clustering see [how Vertigo coordinates networks](#how-vertigo-coordinates-networks)

### Configuring cluster scopes
Users can optionally configure the cluster scope for individual Vertigo networks.
To configure the cluster scope for a network simply use the `setScope` method on the
network configuration.

```java
NetworkConfig network = vertigo.createNetwork("test");
network.getClusterConfig().setScope(ClusterScope.LOCAL);
```

The network scope defaults to `CLUSTER` which is considered the highest level scope.
If the current Vert.x is not a clustered Vert.x instance, the cluster scope will
automatically fall back to `LOCAL`. This allows for networks to be easily tested
in a single Vert.x instance.

It's important to note that while configuring the cluster scope on a network will
cause the network to be *deployed* in that scope, the network's scope configuration
*does not impact Vertigo's synchronization*. In other words, even if a network is
deployed locally, if the current Vert.x instance is a cluster member, Vertigo will still
use Hazelcast to coordinate networks. This allows locally deployed networks
to be referenced and reconfigured even outside of the instance in which it was deployed.
For instance, users can deploy one component of the `foo` network locally in one Vert.x
instance and deploy a separate component of the `foo` network locally in another Vert.x
instance and both components will still become a part of the same network event though
the network is `LOCAL`.

### Cluster-wide shared data
Cluster-wide shared data structures are made available via the same API as clustering.
If a network is deployed in `LOCAL` scope then Vert.x `SharedData` based data structures
will be made available in component instances. If a network is deployed in `CLUSTER`
scope then shared data structures will be accessed over the event bus.

The cluster API is available in all components via the `cluster` field of the
`ComponentVerticle`.

#### AsyncMap
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

#### AsyncSet
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

#### AsyncList
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

#### AsyncQueue
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

#### AsyncCounter
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
This section is a more in-depth examination of how Vertigo deploys and manages
networks and the communication between them. It is written with the intention
of assisting users in making practical decisions when working with Vertigo.

### How Vertigo handles messaging
All Vertigo messaging is done over the Vert.x event bus. Vertigo messaging is
designed to provide guaranteed ordering and exactly-once processing semantics.
Internally, Vertigo uses *streams* to model connections between an output port
on one set of component instances and an input port on another set of component
instances. Each output port can contain any number of output streams, and each
output stream can contain any number of output connections (equal to the number
of instances of the target component). Connections represent a single event bus
address connection between two instances of two components on a single Vertigo
connection. Connection selectors are used at the stream level to select a set
of connections to which to send each message for the stream.

(See `net.kuujo.vertigo.io`)

Vertigo ensures exactly-once semantics by batching messages for each connection.
Each message that is sent on a single output connection will be tagged with a
monotonically increasing ID for that connection. The input connection that receives
messages from the specific output connection will keep track of the last seen
monotonically increasing ID for the connection. When a new message is received,
the input connection checks to ensure that it is the next message in the sequence
according to its ID. If a message is received out of order, the input connection
immediately sends a message to the output connection indicating the last sequential
ID that it received. The output connection will then begin resending messages from
that point. Even if a message is not received out of order, input connections will
periodically send a message to their corresponding output connection notifying it
of the last message received. This essentially acts as a *ack* for a batch of
messages and allows the output connection to clear its output queue.

In the future, this batching algorithm will be the basis for state persistence.
By coordinating batches between multiple input connections, components can
checkpoint their state after each batch and notify data sources that it's safe
to clear persisted messages.

### How Vertigo performs deployments
Vertigo provides two mechanisms for deployment - local and cluster. The *local*
deployment method simply uses the Vert.x `Container` for deployments. However, Vertigo's
internal deployment API is designed in such a way that each deployment is *assigned*
a unique ID rather than using Vert.x's internal deployment IDs. This allows Vertigo
to reference and evaluate deployments after failures. In the case of local deployments,
deployment information is stored in Vert.x's `SharedData` structures.

Vertigo also supports clustered deployments using Xync. Xync exposes user-defined
deployment IDs in its own API.

(See `net.kuujo.vertigo.cluster.Cluster` and `net.kuujo.vertigo.cluster.ClusterManager`)

When Vertigo begins deploying a network, it first determines the current cluster scope.
If the current Vert.x instance is a Hazelcast clustered instance, Vertigo will perform
all coordination through the Hazelcast cluster. Once the cluster scope is determined,
Vertigo will check the cluster's shard data structures to determine whether the network
is already deployed. If the network is already deployed then Vertigo will load the
network's cluster scope - which may differ from the actual cluster scope - and deploy
the network's manager. Actual component deployments are performed by the manager.
For more information on the network manager and coordination see
[how vertigo coordinates networks](#how-vertigo-coordinates-networks).

### How Vertigo coordinates networks
Vertigo uses a very unique and flexible system for coordinating network deployment,
startup, and configuration. The Vertigo coordination system is built on a distributed
observer implementation. Vertigo will always use the highest cluster scope available
for coordination. That is, if the current Vert.x cluster is a Hazelcast cluster then Vertigo
will use Hazelcast for coordination. This ensures that Vertigo can coordinate
all networks within a cluster, even if they are deployed as local networks.

The distributed observer pattern is implemented as map events for both Vert.x `SharedData`
and Hazelcast-based maps. Events for any given key in a Vertigo cluster can be
watched by simply registering an event bus address to which to send events. The Vertigo
`NetworkManager` and components both use this mechanism for coordination with one another.

(See `net.kuujo.vertigo.data.WatchableAsyncMap`)

The `NetworkManager` is a special verticle that is tasked with starting, configuring,
and stopping a single network and its components. When a network is deployed, Vertigo
simply deploys a network manager and sets the network configuration in the cluster. The
network manager completes the rest of the process.

When the network manager first starts up, it registers to receive events for the
network's configuration key in the cluster. Once the key has been set, the manager will
be notified of the configuration change through the event system, load the network
configuration, and deploy the necessary components.

(See `net.kuujo.vertigo.network.manager.NetworkManager`)

This is the mechanism that makes live network configurations possible in Vertigo.
Since the network manager already receives notifications of configuration changes for
the network, all we need to do is set the network's configuration key to a new configuration
and the network will be automatically notified and updated asynchronously.

But deployment is only one part of the equation. Often times network reconfigurations
may consist only of new connections between components. For this reason, each Vertigo
component also watches its own configuration key in the cluster. When the network
configuration changes, the network manager will update each component's key in the
cluster, causing running components to be notified of their new configurations.
Whenever such a configuration is detected by a component, the component will automatically
update its internal input and output connections asynchronously.

(See `net.kuujo.vertigo.component.ComponentCoordinator`)

Finally, cluster keys are used to coordinate startup, pausing, resuming, and shutdown
of all components within a network. When a component is deployed and completes setting
up its input and output connections, it will set a special status key in the cluster.
The network manager watches status keys for each component in the network. Once the
status keys have been set for all components in the cluster, the network will be
considered ready to start. The network manager will then set a special network-wide
status key which each component in turn watches. Once the components see the network
status key has been set they will finish startup and call the `start()` method.

During configuration changes, the network manager will unset the network-wide status
key, causing components to optionally pause during the configuration change.

It's important to note that each of these updates is essentially atomic. The network
manager, components, and connections each use internal queues to enqueue and process
updates atomically in the order in which they occur. This has practically no impact on
performance since configuration changes should be rare and it ensures that rapid configuration
changes (through an `ActiveNetwork` object for instance) do not cause race conditions.

One of the most important properties of this coordination system is that it is completely
fault-tolerant. Since configurations are stored in the cluster, even if a component fails
it can reload its last existing configuration from the cluster once failover occurs.
If the network manager fails, the rest of the network can continue to run as normal.
Only configuration changes will be unavailable. Once the manager comes back online, it
will fetch the last known configuration for the network and continue normal operation.

**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
