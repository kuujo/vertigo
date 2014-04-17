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

### Connection groupings
When connecting together two components, often time one or both components at either
end of the connection will consist of several component instances. Normally, the Vert.x
event bus provides basic round-robin routing of messages to multiple handlers at the
same address, but Vertigo provides additional routing methods through its *grouping*
concept. When a message is sent on a component's output port to a component that
consists of more than one instance, Vertigo will use the grouping to determine which
instance(s) of that component will receive the message.

To set a grouping on a connection call one of the grouping specific methods:
* `roundGrouping()` - round-robin based grouping
* `randomGrouping()` - random grouping
* `hashGrouping()` - message hash based grouping
* `fairGrouping()` - sends the message to the connection with the shortest queue
* `allGrouping()` - sends a message to *all* instances of a connected component
* `customGrouping(CustomGrouping grouping)` - facilitates custom grouping implementations

```java
network.addVerticle("foo", "foo,js", 2)
    .addVerticle("bar", "bar.js", 4)
    .createConnection("foo", "out", "bar", "in")
    .hashGrouping();
```

### Logger ports
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
To deploy a Vertigo network simply call one of the `deployNetwork` methods on
a `Vertigo` instance.

```java
vertigo.deployNetwork(network);
```

The `Vertigo` API provides a couple of different methods for deploying networks.
The most common method is to deploy a network configuration.

```java
NetworkConfig network = vertigo.createNetwork("test");
vertigo.deployNetwork(network);
```

But Vertigo also supports deploying bare networks.

```java
vertigo.deployNetwork("test");
```

Deploying a bare network will simply create and deploy an empty network
configuration. Users can then add components and connections to the network
once it's deployed. For more on network reconfiguration see below.

### Network clustering
Vertigo supports either local or clustered network deployments. By default,
Vertigo will automatically detect the current Vert.x cluster status and deploy
networks either locally or remotely according to how the Vert.x instance was
started. Vertigo clustering requires [Xync](http://github.com/kuujo/xync) for
remote deployments. If the current Vert.x instance was started as a Xync
clustered Vert.x instance, networks will, by default, be deployed across the
Vert.x cluster. Otherwise, networks will be deployed locally.

The local/cluster option can optionally be overridde in the network configuration.
To force a network to be deployed locally even in a Xync cluster, set the
`scope` option on the network by calling `setScope`

```java
network.setScope(ClusterScope.LOCAL);
```

This will force Vertigo to deploy the network only within the local Vert.x
instance regardless of the cluster status. But there are some interesting
points to note about this feature. Even if a network is deployed locally,
Vertigo will still attempt to use the Xync cluster (if available) to coordinate
networks across the cluster. This means that even though a network was deployed
within the local Vert.x instance, other instances within the cluster can still
reference, change, or undeploy the network. This helps ensure that local networks
do not conflict with one another within a Xync-based Vert.x cluster.

### Undeploying networks
Networks can similarly be undeployed by calling the `undeployNetwork` method, passing
either a network configuration or network name to the cluster.

```java
vertigo.undeployNetwork("my_network", new Handler<AysncResult<Void>>() {
  public void handle(AsyncResult<Void> result) {
    if (result.succeeded()) {
      // Undeployment was successful.
    }
  }
});
```

When running Vert.x in a cluster, Vertigo automatically replicates network information
across the cluster, meaning networks don't have to be undeployed from the same instance
from which they were deployed.

### Network configuration changes
Another feature of Vertigo network deployment is that Vertigo networks can be reconfigured
at runtime. This means that verticles, modules, and the connections between them can be
deployed or undeployed without shutting down an entire network. This helps improve uptime
for Vertigo networks, but there are some very important caviats to this process.

Live network configuration changes should be used with caution for obvious reasons.
Modifying a network's structure at runtime can potentially cause unexpected side-effects
depending on your network's configuration. For this reason, Vertigo only allows complete
components and connections to be deployed and undeployed; it does not allow component
or connection configurations to be updated (for instance, adding component instances)
in order to ensure consistency is maintained for hash-based routing for example.

Network configuration changes are performed using the same network deployment API. *This
is where network names become important.* When a network configuration is deployed,
Vertigo will check whether any networks with the same name are running. If a network
with the same name is already running in the cluster, Vertigo will *merge* the new
configuration with the existing network's configuration and deploy any added components
or connections. Networks can be partially undeployed in the same manner. When a network
is undeployed using a configuration, Vertigo will unmerge the given configuration from
the running configuration and, if components or connections remain in the leftover
configuration, the network will continue to run.

Let's take a look at a brief example for clarity.

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

Expect a more in-depth explanation of how Vertigo coordinates network configuration
changes and monitors component statuses in the near future.

### Empty network deployment
With Vertigo's runtime network configuration support, it's possible also to deploy
empty networks and configure them after startup. To do so, simply pass a string
network name to the `deploy*` method.

```java
vertigo.deployNetwork("my_network", new Handler<AsyncResult<ActiveNetwork>>() {
  public void handle(AsyncResult<ActiveNetwork> result) {
    // Deploy a verticle to the already running "my_network" network.
    vertigo.deployNetwork(vertigo.createNetwork("my_network").addVerticle("foo", "foo.js"));
  }
});
```

### Active networks
You probably noticed the `ActiveNetwork` that is provided when deploying a Vertigo
network. The `ActiveNetwork` is essentially a `NetworkConfig` like object that
handles live network configuration changes for you. So, rather than constructing
and deploying or undeploying partial network configurations, users can use the
`ActiveNetwork` object returned by the initial deployment to reconfigure the network.

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

```java
output.port("out").send(12345, "foobar");
```

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
