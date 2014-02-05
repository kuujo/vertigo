Vert.igo
========

**Need support? Check out the [Vertigo Google Group][google-group]**

**[Java User Manual](https://github.com/kuujo/vertigo/wiki/Java-User-Manual) | [Javadoc](http://vertigo.kuujo.net/java/)**

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
  **random, round-robin, hashing, or fanout** approaches
* Supports **distribution of verticle/modules instances across a cluster** of Vert.x
  instances
* **Monitors networks for failures** and automatically reassigns/redeploys failed
  verticles and modules
* Network components can be written in **any Vert.x supported language**, with
  APIs for Vertigo 0.6 in [Javascript][vertigo-js]
  and [Python][vertigo-python]
* Integrates seemlessly with existing Vert.x applications

### New in Vertigo 0.6

* Smaller, **simpler core APIs** with explicitly typed components - feeder, worker, and executor
* Consolidated network deployment API in a `Vertx` like object
* Automatic construction of component instances within component Vert.x verticles
* Improved Java API with component verticle implementations
* New **stream abstraction**. Emit messages to specific output streams and subscribe
to messages from specific input streams:
* Improved ack/fail/timeout feedback mechanisms
* Network deployment events via the Vert.x event bus
* Component event hooks via Java classes or the event bus
* Completely redesigned **Jackson-based serializer** which supports automated serialization
  of most obejcts
* Improved message tracking algorithm with significantly smaller memory footprint
  (able to track huge numbers of messages efficiently)
* [Complete Python API][vertigo-python]
* [Complete Javascript API][vertigo-js]

### Upcoming in Vertigo 0.6.3
* Improved component logging
* Support for worker verticle components and related options
* Network Json configuration bug fixes
* Distributed coordination of network components (removed a central point of failure)

### Roadmap for Vertigo 0.7
Vertigo 0.7 will provide many new advanced reliability features. I'd like to
outline some of those features and the reasoning behind them.

#### Fault-tolerance
Vertigo's clustering support is being refactored to provide for fault-tolerant
networks. Currently, Vertigo supports deploying networks within a cluster of
Vert.x instances using [Via](https://github.com/kuujo/via). But Via is only a
basic cluster management system that cannot provide any reliability guarantees
whatsoever. This is an issue for users who want to ensure their Vertigo networks
remain alive. So, I've begun work on redesigning Via to provide fault-tolerant
cluster management. What this means is that once a Vertigo component is deployed
to a Via cluster, if any node in the cluster dies then that node's assignments
will be automatically reassigned to a node that is still alive. Indeed, this is
the current behavior of Via, but the Via master node represents a single point
of failure that means if the master dies then the entire cluster stops working.

The way to remedy this is by building a decentralized cluster management system.
That means using a consensus algorithm to get disparate nodes to agree on values
regardless of their location in time and space. So, to that end I've created an
implementation of the
[Raft consensus algorithm](https://ramcloud.stanford.edu/wiki/download/attachments/11370504/raft.pdf)
on top of the Vert.x event bus. [CopyCat](https://github.com/kuujo/copycat) is a
simple framework for replicating state across a Vert.x cluster, and it will
soon be used as the basis for cluster and state management in Vertigo.

#### Strong ordering
Currently, Vertigo provides no guarantees on the order of messages arriving at
any given component. If a descendant of a message emitted from any feeder times
out at any time, Vertigo will re-emit the message from the feeder, meaning that
while the message was originally created in a certain order, it may not necessarily
arrive at any given component in that same order. But some applications require
that message be strongly ordered, so Vertigo will provide stream-based ordering
options.

#### Exactly-once processing semantics
If a Vertigo message is taking too long to process, Vertigo may automatically
re-emit that message from the original feeder. This can mean that any given
component in any network may in fact end up seeing the same message twice. But
sometimes the internal state of components may require that messages not be
received more than once. Vertigo will provide stream-based options for exactly-once
processing semantics.

#### Support for deploying networks from the command line
Finally, a popular feature of Vertigo has been the ability to create network
configurations from json data. While Vert.x modules and verticles can be deployed
from the command line, custom verticles must be written in order to define and
deploy Vertigo networks. Vertigo will build upon its deserialization features
to support deployment of Vertigo networks from the command line using json
configuration files.

---

Vertigo is not a replacement for [Storm](https://github.com/nathanmarz/storm).
Rather, Vertigo is a lightweight alternative that is intended to be embedded
within larger Vert.x applications.

For an in-depth look at the concepts underlying Vertigo, check out
[how it works](https://github.com/kuujo/vertigo/wiki/How-it-works).

**[Javascript API][vertigo-js]**

**[Python API][vertigo-python]**

### Adding Vertigo as a Maven dependency

```
<dependency>
  <groupId>net.kuujo</groupId>
  <artifactId>vertigo</artifactId>
  <version>0.6.2</version>
</dependency>
```

### Including Vertigo in a Vert.x module
To use the Vertigo Java API, you can include the Vertigo module in your module's
`mod.json` file. This will make Vertigo classes available within your module.

```
{
  "main": "com.mycompany.myproject.MyVerticle",
  "includes": "net.kuujo~vertigo~0.6.2"
}
```

**Need support? Check out the [Vertigo Google Group][google-group]**

[vertigo-python]: https://github.com/kuujo/vertigo-python
[vertigo-js]: https://github.com/kuujo/vertigo-js
[google-group]: https://groups.google.com/forum/#!forum/vertx-vertigo
