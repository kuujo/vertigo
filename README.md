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
vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
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

## Introduction
Vertigo is a multi-step event processing framework built on top of the Vert.x application
platform. It provides an API that abstracts event bus details from Vert.x verticle implementations,
promoting reusability of your verticles. Vertigo handles messaging between components, ensuring
messages are delivered in particular order.

Vertigo networks - collections of Vert.x modules and verticles and the relationships between
them - are defined and deployed externally to individual verticle implementations. Vertigo
*components* communicate using named input and output ports rather than explicit event bus
addresses.

## The core API
In most cases, users should operate using an instance of the `Vertigo` class. The `Vertigo` class
is the primary API for creating and deploying Vertigo networks.

```java
public class MyVerticle extends Verticle {

  @Override
  public void start() {
    Vertigo vertigo = new Vertigo(this);
  }

}
```

## Networks
Networks are collections of Vert.x verticles and modules between which connections are defined
externally to the verticle code. Users define and deploy networks using the network configuration
API.

### Creating a network
To create a new network, call the `createNetwork` method on a `Vertigo` instance:

```java
Vertigo vertigo = new Vertigo(this);
NetworkConfig network = vertigo.createNetwork("test");
```

Note that the network is given a name. This name is very important as Vertigo as it can be
used to reference running networks from anywhere within a Vert.x cluster, but more on that
later.

Networks can also be constructed from a `JsonObject`.

```java
NetworkConfig network = vertigo.createNetwork(json);
```

### Adding components
Networks can consist of any number of Vert.x verticles or modules, known as *components*
in Vertigo. To add a component to the network simply call the `addVerticle` or `addModule`
method on the network configuraiton. These methods have the following signatures:

* `addVerticle(String name, String main)`
* `addVerticle(String name, String main, JsonObject config)`
* `addVerticle(String name, String main, int instances)`
* `addVerticle(String name, String main, JsonObject config, int instances)`
* `addModule(String name, String main)`
* `addModule(String name, String main, JsonObject config)`
* `addModule(String name, String main, int instances)`
* `addModule(String name, String main, JsonObject config, int instances)`

Once you add the module or verticle, a `ModuleConfig` or `VerticleConfig` instance
will be returned respectively.

```java
network.addVerticle("foo", "foo.js").setConfig(new JsonObject().putString("foo", "bar"));
```

The `config` and `instances` options are the same as those expected by the Vert.x
`Container` interface - verticle configuration and number of instances to deploy.

### Creating connections
In order to facilitate messaging between components within the network, you must create
connections between them. Each component may have any number of named input and output
ports, but input and output ports should agree with the code within each component.

To create a connection use the `createConnection` method.
* `createConnection(String source, String outPort, String target, String inPort)`

Note that the connection API uses string component and port names rather than object
references because it's possible to create connections between components that
either don't exist or are not available in the current scope, but more on that later.

```java
network.createConnection("foo", "out", "bar", "in");
```

The connection port names default to `out` and `in` respectively if no port names
are specified.

### Logger port names
Vertigo reserves some port names for logging. Reserved logging port
names match the methods of the Vert.x logger interface:
* `fatal`
* `error`
* `warn`
* `info`
* `debug`
* `trace`

You can use these ports to receive log messages from other components. Each
Vertigo component contains a logger that logs to these special output ports.

## Network deployment
Vertigo supports two forms of network deployment - *local* and *remote*.
Remote deployment requires [Xync](http://github.com/kuujo/xync) and supports deployment
of Vertigo networks across a cluster of Vert.x instances.
* `deployLocalNetwork(NetworkConfig network)`
* `deployLocalNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler)`
* `deployRemoteNetwork(NetworkConfig network)`
* `deployRemoteNetwork(NetworkConfig network, Handler<AsyncResult<ActiveNetwork>> doneHandler)`

### Active networks
When deploying a network, the asynchronous handler will be called once the network
has completed startup. The handler will be called with an `ActiveNetwork` object
that exposes an interface similar to that of the `NetworkConfig`. The `ActiveNetwork`
object can be used to alter the configuration of the network in real time.

```java
// Create a network with a single component.
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);

// Deploy the network.
vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    if (result.succeeded()) {
      // Add another component to the network and create a connection between the two components.
      ActiveNetwork network = result.result();
      network.addVerticle("bar", "bar.js", 2);
      network.createConnection("foo", "out", "bar", "in");
    }
  }
});
```

### Partial network deployment
Vertigo also supports partial network deployments. When a network is deployed,
Vertigo checks to see if a network with the same name is already running in the
cluster. *This is where network names become important.* If a network with the
same name is running in the cluster, Vertigo will *merge* the new network configuration
with the deployed network configuration and automatically update the network's deployed
modules, verticles, and their connections.

We can acheive the same results as the `ActiveNetwork` example above by doing the following:

```java
// Create a network with a single component.
NetworkConfig network = vertigo.createNetwork("test");
network.addVerticle("foo", "foo.js", 2);

// Deploy the network.
vertigo.deployLocalNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {

    // Create a second network with the same name and deploy it.
    NetworkConfig network = vertigo.createNetwork("test");
    network.addVerticle("bar", "bar.js", 2);
    network.createConnection("foo", "out", "bar", "in");

    // Deploy the new configuration, causing the two configurations to be merged.
    vertigo.deployLocalNetwork(network);

  }
});
```

## Components
Vertigo components are simply Vert.x verticle instances that extend the base
`ComponentVerticle` class. Each component instance contains the following
members that are relevant to component operation:

* `VertigoCluster cluster` - the Vertigo cluster to which the component belongs.
  This can be used to deploy modules or verticles within the same cluster as the network.
* `Logger logger` - a special component logger that logs messages to output ports
* `InputCollector input` - component input ports API
* `OutputCollector output` - component output ports API

Most important of these fields are the `input` and `output` fields.

### Managing ports
Components don't have to do anything special to create ports. Simply call
the `port(String name)` method on an input or output and the input or output
collector will lazy create the port if it doesn't already exist. There is always
potential that messages will never be received on an input port or never sent
on an output port - that's a detail that is left to the network configuration.

```java
OutputPort port = output.port("out");
```

### Sending messages
Vertigo supports sending any message that is supported by the Vert.x event bus.

```java
output.port("out").send("Hello world!");
output.port("out").send(12345);
output.port("out").send(new JsonObject().putString("foo", "bar"));
```

If a message fails to be delivered to a receiving connection, Vertigo will automatically
re-attempt to send the failed message. This means that Vertigo messages are not guaranteed
to be delivered in any specific order. Internally, Vertigo uses adaptive event bus timeouts
to help detect failures quickly by monitoring the response time for each individual event
bus address. From a user's perspective, you need only send the message on an output port
and Vertigo will handle routing and delivering it to the appripriate component(s).

### Message groups
While Vertigo messages are not guaranteed to be delivered in any specific order, Vertigo
does provide an API for logically grouping and ordering sets of messages. Message groups
are named logical collections of messages. When a component send a set of messages as
part of an output group, the same messages will be received as part of an input group
of the same name, with Vertigo maintaining order across groups.

### Working with output groups
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

### Receiving messages
To receive messages on an input port, register a message handler using the
`messageHandler` method. Remember, Vertigo supports any type that is supported
by the Vert.x event bus.

```java
input.port("in").messageHandler(new Handler<String>() {
  public void handle(String message) {
    output.port("out").send(message);
  }
});
```

### Working with input groups
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






















**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
