Vertigo
=======

**Need support? Check out the [Vertigo Google Group][google-group]**

**[Java User Manual](#java-user-manual) | Javadoc (Unavailable for Vertigo 0.7.x)**

**[Javascript API][vertigo-js] for Vertigo 0.7.x is still under development**

**[Python API][vertigo-python] for Vertigo 0.7.x is still under development**

Vertigo is a fast, reliable, fault-tolerant event processing framework built on
the [Vert.x](http://vertx.io/) application platform. Combining concepts of
cutting-edge [real-time systems](http://storm.incubator.apache.org/) and
[flow-based programming](http://en.wikipedia.org/wiki/Flow-based_programming),
Vertigo allows real-time problems to be broken down into smaller tasks (as
Vert.x verticles) and distributed across a Vert.x cluster. Vertigo provides
fault-tolerance for data streams and components, allowing developers to spend more
time focusing on application code.

* Manages multi-step event processing systems, from simple pipelines to
  **complex networks of Vert.x modules/verticles**, including **remote procedure
  calls spanning multiple Vert.x verticle instances**
* Supports deployment of networks within a single Vert.x instance or across a Vert.x cluster.
* **Promotes reusability** by abstracting communication details from verticle implementations,
  allowing components to be arbitrarily connected to form complex networks.
* Handles buffering of messages and automatic retries on failures.
* Facilitates distribution of messages between multiple verticle instances using
  **random, round-robin, mod hashing, or fanout** methods.
* Network components can be written in **any Vert.x supported language**, with
  APIs for Vertigo 0.6 in [Javascript][vertigo-js] and [Python][vertigo-python]

### New in Vertigo 0.7
Vertigo 0.7 represents a significant directional shift for Vertigo. In my view, Vertigo
is finally gaining an identity of its own. We have now adopted many concepts of
[flow-based programming](http://en.wikipedia.org/wiki/Flow-based_programming),
essentially merging modern reliable stream-processing features with more abstract component
APIs and communication models. Vertigo 0.7 is not yet stable, but we do have a comprehensive
list of the changes that have been or will be made prior to a 0.7 release.

#### Vertigo UI
Contributors to Vertigo have been developing a UI for Vertigo that will allow users to
visually design Vertigo networks. Significant work is being done to ensure that Vertigo
will integrate seamlessly with the new UI. See the Vertigo Google Group for more info.

#### A single abstract component
Vertigo itself no longer provides high level component implementations - e.g. feeders
and workers. Instead, Vertigo provides a single "black box" `Component` which can both
receive and send messages. This means that components can be arbitrarily connected to
one another without component type limitations.

#### Redesigned network configuration API
The network configuration API has been completely redesigned for Vertigo 0.7. The design is
loosely based on common flow-based programming APIs, but more importantly it allows for
partial network configurations to be interpreted by Vertigo in order to support runtime
network configuration changes (see below).

```java
NetworkConfig network = vertigo.createNetwork("wordcount")
  .addComponent("foo", "foo.js", 2)
  .addComponent("bar", "bar.js", 4)
  .createConnection("foo", "out", "bar", "in");
```

#### Runtime network configuration changes
One of the primary focuses in Vertigo 0.7 was the ability to support adding and removing
components and connections from networks while running. To accomplish this, the entire
coordination framework underlying Vertigo was rewritten to use a distributed implementation
of the observer pattern to allow components and networks to watch their own configurations
for changes and update themselves asynchronously. This allows Vertigo networks to be
restructured at runtime by simply deploying and undeploying partial networks.

#### Active networks
In conjunction with support for runtime network configuration changes, Vertigo now provides
an `ActiveNetwork` object that allows networks to be reconfigured by directly altering
network configurations.

```java
vertigo.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    ActiveNetwork network = result.result();
    network.addComponent("baz", "baz.py", 3); // Add a component.
    network.removeComponent("foo"); // Remove a component.
    network.createConnection("bar", "baz"); // Create a connection.
    network.destroyConnection("foo", "bar"); // Destroy a connection.
  }
});
```

#### Reliable connections
Vertigo's internal messaging framework has been redesigned to remove potential reliability
issues in cases where the Vert.x event loop is blocked. Previously, Vertigo's pub-sub style
messaging was dependent upon a timout mechanism which could fail under certain circumstances,
but Vertigo is now guaranteed not to temporarily drop connections in those cases.

#### Fault-tolerant components
Vertigo's clustering support is now backed by [Xync](http://github.com/kuujo/xync), a
custom Vert.x platform manager that integrates remote deployments with the Vert.x HA
mechanism. This allows Vertigo to deploy components across a Vert.x cluster and still
failover those deployments when a Vert.x instance dies.

#### Cluster-wide shared data structures
Vertigo 0.7 supports cluster-wide shared data structures like `map`, `list`, `set`,
`queue`, `lock`, and `id` (a unique ID generator). These data structures can be used
for coordination between components and are available both regarless of whether Vert.x
is in clustering mode.

#### Stateful components
Vertigo provides an API for component state persistence through several facilities,
e.g. Hazelcast, Redis, or Vert.x shared data. Component data storage facilities can
be defined in the network configuration, so storage details are separate from component
implementations.

#### Streams to ports
Vertigo 0.7 has adopted the concept of ports from the flow-based programming model. This
is a rather insignificant change which means that rather than one component subscribing
to an output stream from another component, connections are created on named output
and input ports between two components. This allows for components to be more easily reused
since connections can be created between arbitrary ports on two components.

#### Network deployment from JSON files
Finally, Vertigo 0.7 supports deployment of networks directly from configuration files
with the `vertigo-deployer` module. This module essentially behaves like a standard
Vert.x language module, allowing `*.network.json` files to be deployed directly using
the Vert.x command line interface.

```
vertx run wordcount.network.json
```

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


# Java User Manual
1. [Introduction](#introduction)
1. [Networks](#networks)
  * [Creating a new network](#creating-a-new-network)
  * [Adding components to a network](#adding-components-to-a-network)
  * [Creating connections between components](#)
  * [Routing messages between multiple component instances](#routing-messages-between-multiple-component-instances)
  * [Creating networks from JSON](#creating-networks-from-json)
1. [Deployment](#deployment)
  * [Deploying a network](#deploying-a-network)
  * [Undeploying a network](#undeploying-a-network)
  * [Reconfiguring a network](#reconfiguring-a-network)
  * [Deploying a bare network](#deploying-a-bare-network)
  * [Working with active networks](#working-with-active-networks)
1. [Clustering](#clustering)
  * [Configuring cluster scopes](#configuring-cluster-scopes)
1. [Components](#components)
  * [Creating a component](#creating-a-component)
  * [The elements of a Vertigo component](#the-elements-of-a-vertigo-component)
1. [Messaging](#messaging)
  * [Sending messages on an output port](#sending-messages-on-an-output-port)
  * [Receiving messages on an input port](#receiving-messages-on-an-input-port)
  * [Working with message groups](#working-with-message-groups)
  * [Providing serializeable messages](#providing-serializeable-messages)
1. [Logging](#logging)
  * [Logging messages to output ports](#logging-messages-to-output-ports)
  * [Reading log messages](#reading-log-messages)

## Introduction
Vertigo is a multi-step event processing framework built on Vert.x. It exposes a
very simple yet powerful API defines networks of Vert.x verticles and the relationships
between them in a manner that abstracts communication details from implementations, making
Vertigo components reusable. It supports deployment of networks within a single Vert.x
instance or across a cluster of Vert.x instances and performs setup and coordination
internally. Vertigo also provides for advanced messaging requirements such as strong
ordering and exactly-once semantics.

## Networks
Vertigo networks are collections of Vert.x verticles and modules that are connected
together by the Vert.x event bus. Networks and the relationships therein are defined
externally to their components, promoting reusability.

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

The `addVerticle` and `addModuld` methods have the following signatures:

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

The `NetworkConfig` API also exposes an abstract `addComponent` method which detects
whether the added component is a module or a verticle based on module naming conventions.

* `addComponent(String name, String moduleOrMain)`
* `addComponent(String name, String moduleOrMain, JsonObject config)`
* `addComponent(String name, String moduleOrMain, int instances)`
* `addComponent(String name, String moduleOrMain, JsonObject config, int instances)`

Once a component has been added to the network, the component configuration will
be returned. Users can set additional options on the component configuration. The
most important of these options is the `group` option. When deploying networks within
a Vert.x cluster, the `group` indicates the HA group to which to deploy the module or verticle.

### Creating connections between components
A set of components is not a network until connections are created between those
components. Vertigo uses a concept of *ports* to abstract input and output from
each component instance. When creating connections between components, you must
specify a component and port to which the connection connects. Each connection
binds one component's output port with another component's input port.

To create a connectoin between two components use the `createConnection` method.

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
additional routing methods known as *groupings*. Groupings indicate how messages
should be routed between multiple instances of a component.

Vertigo provides several grouping types by default and supports custom groupings
as well.

* Round grouping - selects targets in a round-robin fashion
* Random grouping - selects a random target to which to send each message
* Hash grouping - uses a simple mod hash algorithm to select a target for each message
* Fair grouping - selects the target with the least number of messages in the queue
* All grouping - sends each message to all target instances
* Custom grouping - user provided custom grouping implementation

The `ConnectionConfig` API provides several methods for setting groupings
on a connection.
* `roundGrouping()` - sets a round-robin grouping on the connection
* `randomGrouping()` - sets a random grouping on the connection
* `hashGrouping()` - sets a mod hash based grouping on the connection
* `fairGrouping()` - sets a fair grouping on the connection
* `allGrouping()` - sets an all grouping on the connection
* `customGrouping(CustomGrouping grouping)` - sets a custom grouping on the connection

### Creating networks from JSON
Vertigo supports creating networks from json configurations. To create a network
from json call the `Vertigo.createNetwork(JsonObject)` method.

The JSON configuration format is as follows:

* `name` - the network name
* `scope` - the network cluster scope, e.g. `local` or `cluster`. See [configuring cluster scopes](#configuring-cluster-scopes)
* `components` - an object of network components, keyed by component names
   * `name` - the component name
   * `type` - the component type, either `module` or `verticle`
   * `main` - the verticle main (if the component is a verticle)
   * `module` - the module name (if the component is a module)
   * `config` - the module or verticle configuration
   * `instances` - the number of component instances
   * `group` - the component deployment group (Vert.x HA group for clustering)
   * `storage` - an object defining the component data storage facility
      * `type` - the component data storage type. This is a fully qualified `DataStore` class name
      * `...` - additional data store configuration options
* `connections` - an array of network connections
   * `source` - an object defining the connection source
      * `component` - the source component name
      * `port` - the source component's output port
   * `target` - an object defining the connection target
      * `component` - the target component name
      * `port` - the target component's input port
   * `grouping` - an object defining the connection grouping
      * `type` - the connection grouping type, e.g. `round`, `random`, `hash`, `fair`, or `all`
   * `custom-grouping` - an object defining a custom grouping for the connection

For example...

```
{
  "name": "my-network",
  "scope": "local",
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
      "grouping": {
        "type": "fair"
      }
    }
  ]
}
```

## Deployment
One of the most important tasks of Vertigo is to support dpeloyment and startup
of networks in a consistent and reliable manner. Vertigo supports network deployment
either within a single Vert.x instance (local) or across a cluster of Vert.x instances.
When a Vertigo network is deployed, a special verticle known as the *network manager*
is deployed. The network manager is tasked with managing and monitoring components
within the network, handling runtime configuration changes, and coordinating startup
and shutdown of networks.

Vertigo clustering is supported by [Xync](http://github.com/kuujo/xync)

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
cluster scope. If the current Vert.x instance is a member of a Xync cluster then
the network will be deployed across the Xync cluster. Otherwise, the network will
be deployed within the current Vert.x instance using the Vert.x `Container`.

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

Note that this method can also be used to reference an existing network and retrieve
an `ActiveNetwork` instance (more on active networks below):

```java
vertigo.deployNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    NetworkConfig network = result.result().getConfig();
  }
});
```

### Working with active networks
Vertigo provides a helper API for reconfiguring netowrks known as *active networks*.
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

## Clustering
Vertigo clustering is currently only supported by [Xync](http://github.com/kuujo/xync).
Xync allows Vertigo to remotely deploy Vert.x verticles and modules and provides
cluster-wide data structures for synchronization. This allows Vertigo to spread deployments
out across the cluster and synchronize local and clustered networks that are deployed
within the same Vert.x cluster.

When deploying a network, Vertigo will *automatically detect the current cluster state*.
If the current Vert.x cluster is a Xync-backed cluster, Vertigo networks will be deployed
across the cluster by default. In Vertigo, this is known as the cluster scope.

### Configuring cluster scopes
Users can optionally configure the cluster scope for individual Vertigo networks.
To configure the cluster scope for a network simple use the `setScope` method on the
network configuration.

```java
NetworkConfig network = vertigo.createNetwork("test");
network.setScope(ClusterScope.LOCAL);
```

The network scope defaults to `CLUSTER`, but if the current Vert.x cluster is
not a Xync cluster then the network will automatically fall back to `LOCAL`.

It's important to note that while configuring the cluster scope on a network will
cause the network to be *deployed* in that scope, the network's scope configuration
*does not impact Vertigo's synchronization*. In other words, even if a network is
deployed locally, if the current Vert.x cluster is a Xync cluster, Vertigo will still
coordinate with other Vert.x instances using Xync. This allows locally deployed networks
to be referenced and reconfigured event outside of the instance in which it was deployed.
For instance, users can deploy one component of the `foo` network locally in one Vert.x
instance and deploy a separate component of the `foo` network locally in another Vert.x
instance and both components will still become a part of the same network event though
the network is `LOCAL`.

## Components
Components are "black box" Vert.x verticles that communicate with other components within
the same network through named *input* and *output* ports.

### Creating a component
Components are defined by extending the base `ComponentVerticle` class.

```java
public class MyComponent extends ComponentVerticle {

  @Override
  public void start() {
  
  }

}
```

Components behave exactly like normal Vert.x verticles. Once the component has been
started and synchronized with other components within its network, the `start()` method
will be called.

### The elements of a Vertigo component
Each Java component has several additional fields:
* `vertigo` - a `Vertigo` instance
* `cluster` - the Vertigo `Cluster` to which the component belongs
* `input` - the component's `InputCollector`, an interface to input ports
* `output`- the component's `OutputCollector`, an interface to output ports
* `logger` - the component's `PortLogger`, a special logger that logs messages to output ports
* `storage` - the component's storage facility. This is configured in the component configuration

## Messaging
Messaging between Vertigo components is done directly on the Vert.x event bus.
Vertigo messages are not sent through any central router. Rather, Vertigo uses
network configurations to create direct event bus connections between components.
Vertigo components send and receive messages using only output and input *ports*
and are hidden from event bus address details which are defined in network configurations.
This is the element that makes Vertigo components reusable.

While Vertigo does use an acking mechanism internally, Vertigo messages are
not guaranteed to arrive in order. However, Vertigo does provide an API for
logical grouping and ordering of messages called [groups](#working-with-message-groups).

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
The base Vertigo messaging system does not guarantee ordering of messages.
But Vertigo does provide a mechanism for logically grouping and ordering
messages known as *groups*. Groups are named logical collections of messages.
Groups can be nested and groups of the same name are guaranteed to be delivered
in order. Before any given group can start, each of the groups of the same name
at the same level that preceeded it must have been received by the target.
Additionally, messages within a group are guaranteed to be delivered to the
same instance of each target component. In other words, routing is performed
per-group rather than per-message.

When a new output group is created, Vertigo will await the completion of all groups
that were created prior to the new group before sending the new group's messages.

```java
output.port("out").group("foo", new Handler<OutputGroup>() {
  public void handle(OutputGroup group) {
    group.send("foo").send("bar").send("baz").end();
  }
});
```

Note that the group's `end()` method *must* be called in order to indicate completion of
the group. Groups are fully asynchronous, meaning they support asynchronous calls to other
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

### Providing serializeable messages
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

## Logging
Ecah Vertigo component contains a special `PortLogger` which logs messages
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
network.createConnection("logger", "fatal", "log-reader", "log").hashGrouping();
network.createConnection("logger", "error", "log-reader", "log").hashGrouping();
network.createConnection("logger", "warn", "log-reader", "log").hashGrouping();
network.createConnection("logger", "info", "log-reader", "log").hashGrouping();
network.createConnection("logger", "debug", "log-reader", "log").hashGrouping();
network.createConnection("logger", "trace", "log-reader", "log").hashGrouping();
```

With a hash grouping on each connection, we guarantee that the same log message
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

**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
