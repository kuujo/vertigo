---
layout: content
menu: user-manual
title: How it Works
---

# How it Works

* [Configurations](#configurations)
* [Cluster](#cluster)
* [Networks](#networks-2)
* [Components](#components-2)
* [Communication](#communication)

There are four essential components to Vertigo's design:
* [Configurations](#configurations)
* [Cluster](#cluster)
* [Networks](#networks-2)
* [Components](#components-2)
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
The Vertigo cluster is the component that manages deployment, undeployment,
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
in the cluster. On the other side, the manager watches the configuration key
for changes using Vertigo's internal [WatchableAsyncMap](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/data/WatchableAsyncMap.java).
Similarly, the network
[sets a status key](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/cluster/impl/ClusterAgent.java#L308)
once the configuration has been installed and the cluster watches the network's
status key to determine when the network has completed configuration. This
allows Vertigo's networks to be dynamically altered and network configurations
to be persisted in the cluster through crashes so they can easily recover.

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
being undeployed, but more on that in the next section. It's important to note that
[all configuration changes are queued in a task runner](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/util/TaskRunner.java)
that ensures that only one configuration change can ever be processed at any given
time.

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
once the component's input and output have been opened, the coordinator will
set the component's status key in the cluster, indicating that the component
has completed startup. However, even though the component has indicated to the
network that it has completed startup, the component won't actually start [until
the network has indicated](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/component/impl/DefaultComponent.java#L148)
that *all* the active components in the network have completed setup.

When the network's configuration changes, the network manager will set the
component's configuration key in the cluster. By watching the configuration key,
the component's internal configuration will be automatically updated if any
changes occur. With cluster-wide data events, since all contexts are
[Observable](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/util/Observable.java),
components can watch their configurations for changes that were made in cluster-wide
data structures. When a component configuration change occurs, each of the component's
internal input and output ports will *automatically* recognize the change and
[update their connections](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/port/impl/DefaultOutputPort.java#L83).
As with networks, components ensure that only one configuration change can
ever occur at any given time.

### Communication
One of the most important features in Vertigo is its messaging system. The
messaging framework has been completely redesigned in Vertigo 0.7 to be modeled
on ports. All Vertigo's messaging is performed over the Vert.x event bus, and
the messaging system is designed to provide strongly-ordered and exactly-once
semantics.

There are numerous components to the Vertigo communication framework. At the
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
[send a *fail* message](https://github.com/kuujo/vertigo/blob/master/core/src/main/java/net/kuujo/vertigo/io/connection/impl/DefaultInputConnection.java#L218)
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