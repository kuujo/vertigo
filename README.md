Vertigo
=======

**Need support? Check out the [Vertigo Google Group][google-group]**

**[Java User Manual](https://github.com/kuujo/vertigo/wiki/Java-User-Manual) | [Javadoc](http://vertigo.kuujo.net/java/)**

**[Javascript API][vertigo-js]**

**[Python API][vertigo-python]**

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
* **Guarantees message processing** through ack/fail/timeout mechanisms, tracking
  complex message trees through multiple Vert.x verticle instances and providing
  data sources with feedback on the status of processing.
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

#### Fault-tolerant data streams
Vertigo 0.7 will automatically batch and replicate data streams so that data sources
no longer have to be fault-tolerant/persistent in order to ensure reliability within
networks. Fault-tolerant data streams will be handled entirely within Vertigo.

#### Stateful components
In conjuction with stream batching, Vertigo will support stateful components through
per-batch checkpointing.

#### Cluster-wide shared data structures
Vertigo 0.7 supports cluster-wide shared data structures like `map`, `list`, `set`,
`queue`, `lock`, and `id` (a unique ID generator). These data structures can be used
for coordination between components and are available both regarless of whether Vert.x
is in clustering mode.

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

**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
