---
layout: content
menu: user-manual
title: Network Deployment and Clustering
---

# Network Deployment and Clustering

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

{% include lang-tabs8.html %}

Vertigo provides its own cluster management framework on top of the Vert.x cluster.
Each Vertigo network will always be deployed in a Vertigo cluster. Vertigo clusters
can be deployed either within a single, non-clustered Vert.x instance or across a
Vert.x cluster. Clusters provide a logical separation between different applications
within a Vert.x cluster and provide additional features such as failover.

## Starting a cluster from the command line

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

## Starting a cluster programmatically

Vertigo also provides an API for deploying clusters or individual nodes through the
Vert.x `Container`.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	Vertigo vertigo = new Vertigo(this);
	vertigo.deployCluster("test-cluster", new Handler<AsyncResult<ClusterManager>>() {
	  public void handle(AsyncResult<ClusterManager> result) {
	    ClusterManager cluster = result.result();
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	Vertigo vertigo = new Vertigo(this);
	vertigo.deployCluster("test-cluster", (result) -> {
	    ClusterManager cluster = result.result();
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

There are several methods for deploying nodes or clusters within the current
Vert.x instance.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8c">

{:.prettyprint .lang-java}
	deployCluster(String address);
	deployCluster(String address, Handler<AsyncResult<ClusterManager>> doneHandler);
	deployCluster(String address, int nodes);
	deployCluster(String address, int nodes, Handler<AsyncResult<ClusterManager>> doneHandler);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Users should use this API rather than deploying the `ClusterAgent` verticle directly
because the cluster agent is pluggable. To override the default cluster agent
set the system property `net.kuujo.vertigo.cluster`.

## Referencing a cluster programmatically

Network deployments are performed through the `ClusterManager` API. To get a
`ClusterManager` instance for a running Vertigo cluster call the `getCluster` method

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	ClusterManager cluster = vertigo.getCluster("test-cluster");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Accessing a cluster through the event bus

The cluster system is built on worker verticles that are accessed over the event bus.
Cluster agents expose an event bus API that can be used as an alternative to the
Java API. Since all Java interface methods simply wrap the event bus API, you can
perform all the same operations as are available through the API over the event bus.

To send a message to a cluster simply send the message to the cluster address.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	    .putString("action", "check")
	    .putString("network", "test");
	vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
	  if (reply.body().getString("status").equals("ok")) {
	    boolean isDeployed = reply.body().getBoolean("result");
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Each message must contain an `action` indicating the action to perform. Each API
method has an action associated with it, and the actions and their arguments will
be outline in the following documentation.

## Deploying a network

To deploy a network use the `deployNetwork` methods on the `ClusterManager` for
the cluster to which the network should be deployed.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	NetworkConfig network = vertigo.createNetwork("test");
	network.addComponent("foo", "foo.js", 2);
	network.addComponent("bar", "bar.py", 4);
	network.createConnection("foo", "out", "bar", "in");
	
	cluster.deployNetwork(network);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

When the network is deployed, the cluster will check to determine whether a
network of the same name is already running in the cluster. If a network of
the same name is running, the given network configuration will be *merged*
with the running network's configuration and the missing components will be
deployed. This is very important to remember. Deployment will *not* fail if
you deploy a network with the same name of a network that already running
in the given cluster.

To determine when the network has been successfully deployed pass an `AsyncResult`
handler to the `deployNetwork` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	clusterManager.deployNetwork(network, new Handler<AsyncResult<ActiveNetwor>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    if (result.succeeded()) {
	      ActiveNetwork network = result.result();
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
    clusterManager.deployNetwork(network, (result) -> {
      if (result.succeeded()) {
        ActiveNetwork network = result.result();
      }
    });
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

You can also deploy the network from the `Vertigo` API by naming the cluster
to which to deploy the network.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	vertigo.deployNetwork("test-cluster", network, new Handler<AsyncResult<ActiveNetwor>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    if (result.succeeded()) {
	      ActiveNetwork network = result.result();
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	vertigo.deployNetwork("test-cluster", network, (result) -> {
	  if (result.succeeded()) {
	    ActiveNetwork network = result.result();
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Deploying a network from JSON

Networks can be deployed programmatically from JSON configurations. To deploy
a network from JSON configuration simply pass the `JsonObject` configuration
in place of the `NetworkConfig`

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	JsonObject network = new JsonObject()
	    .putString("name", "test")
	    .putObject("components", new JsonObject()
	        .putObject("foo", new JsonObject()
	            .putString("type", "verticle").putString("main", "foo.js")));
	
	cluster.deployNetwork(network);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

JSON configurations can also be used to deploy networks to a cluster over the
event bus. To deploy a network over the event bus send a `deploy` message to
the cluster address with a `network` type, specifying the `network` configuration
as a `JsonObject`.

{:.prettyprint .lang-json}
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

If successful, the cluster will reply with a `status` of `ok`.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
	  public void handle(Message<JsonObject> reply) {
	    if (reply.body().getString("status").equals("ok")) {
	      // Network was successfully deployed!
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
    vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
      if (reply.body().getString("status").equals("ok")) {
        // Network was successfully deployed!
      }
    });
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

For information on the JSON configuration format see
[creating networks from json](#creating-networks-from-json)

## Undeploying a network

To undeploy a *complete* network from a cluster call the `undeployNetwork`
method, passing the network name as the first argument.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	clusterManager.undeployNetwork("test", new Handler<AsyncResult<Void>>() {
	  public void handle(AsyncResult<Void> result) {
	    if (result.succeeded()) {
	      // Network has been undeployed.
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	clusterManager.undeployNetwork("test", (result) -> {
	  if (result.succeeded()) {
	    // Network has been undeployed.
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

{:.prettyprint .lang-json}
	{
	  "action": "undeploy",
	  "type": "network",
	  "network": "test"
	}

The `network` field can also contain a JSON configuration to undeploy. If the undeployment
is successful, the cluster will reply with a `status` of `ok`.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	  .putString("action", "undeploy")
	  .putString("type", "network")
	  .putString("network", "test");
	vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
	  if (reply.body().getString("status").equals("ok")) {
	    // Network was successfully undeployed!
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Checking if a network is deployed

To check if a network is deployed in the cluster use the `isDeployed` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	cluster.isDeployed("test", new Handler<AsyncResult<Boolean>>() {
	  public void handle(AsyncResult<Boolean> result) {
	    if (result.succeeded()) {
	      boolean deployed = result.result(); // Whether the network is deployed.
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	cluster.isDeployed("test", (result) -> {
	  if (result.succeeded()) {
	    boolean deployed = result.result(); // Whether the network is deployed.
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

When checking if a network is deployed, a `check` message will be sent to the cluster along
with the name of the network to check. If the network's configuration is available in
the cluster then the network will be considered deployed. This is the same check that
the cluster uses to determine whether a network is already deployed when deploying a
new network configuration.

To check whether a network is deployed directly over the event bus, send a `check` message
to the cluster specifying a `network` type along with the `network` name.

{:.prettyprint .lang-json}
	{
	  "action": "check",
	  "type": "network",
	  "network": "test"
	}

Send the `check` message to the cluster event bus address. If successful, the cluster will
reply with a boolean `result` indicating whether the network is deployed in the cluster.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	  .putString("action", "check")
	  .putString("type", "network")
	  .putString("network", "test");
	vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
	  if (reply.body().getString("status").equals("ok")) {
	    boolean deployed = reply.body().getBoolean("result");
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Listing networks running in a cluster

To list the networks running in a cluster call the `getNetworks` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	clusterManager.getNetworks(new Handler<AsyncResult<Collection<ActiveNetwork>>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    ActiveNetwork network = result.result();
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	clusterManager.getNetworks((result) -> {
	  ActiveNetwork network = result.result();
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Note that the method returns an `ActiveNetwork`. The active network can be used
to reconfigure the running network, but more on that later. The current network
configuration can be retrieved from the `ActiveNetwork` by calling the `getConfig`
method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	NetworkConfig config = network.getConfig();
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

To list the networks running in the cluster over the event bus, use the `list`
action.

{:.prettyprint .lang-json}
{
  "action": "list"
}

If successful, the cluster will reply with a `result` containing an array of
configuration objects for each network running in the cluster.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	  .putStrign("action", "list");
	vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
	  public void handle(Message<JsonObject> reply) {
	    if (reply.body().getString("status").equals("ok")) {
	      JsonArray networks = reply.body().getArray("result");
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	  .putStrign("action", "list");
	vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
	  if (reply.body().getString("status").equals("ok")) {
	    JsonArray networks = reply.body().getArray("result");
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Deploying a bare network

Vertigo networks can be reconfigured after deployment, so sometimes it's useful
to deploy an empty network with no components or connections.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	clusterManager.deployNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    ActiveNetwork network = result.result();
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	clusterManager.deployNetwork("test", (result) -> {
	  ActiveNetwork network = result.result();
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

When a bare network is deployed, Vertigo simply deploys a network manager
verticle with no components. Once the network is reconfigured, the manager
will automatically update the network with any new components.

To deploy a bare network over the event bus, pass a `String` network name
in the `network` field rather than a JSON network configuration.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	JsonObject message = new JsonObject()
	  .putString("action", "deploy")
	  .putString("type", "network")
	  .putString("network", "test");
	vertx.eventBus().send("test-cluster", message, (Message<JsonObject> reply) -> {
	  if (reply.body().getString("status").equals("ok")) {
	    // Network was successfully deployed!
	  }
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## Reconfiguring a network

Vertigo provides several methods to reconfigure a network after it has been
deployed. After a network is deployed users can add or remove components or
connections from the network. To reconfigure a running network simply deploy
or undeploy a network configuration of the same name.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

## Working with active networks

Vertigo provides a special API for reconfiguring running networks known as the
*active network*. The `ActiveNetwork` API mimics the network configuration API,
except changes to an `ActiveNetwork` instance will be immediately deployed to
the running network in the appropriate cluster.

To load an active network you can call `getNetwork` on a cluster.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	clusterManager.getNetwork("test", new Handler<AsyncResult<ActiveNetwork>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    ActiveNetwork network = result.result();
	    network.createConnection("foo", "out", "bar", "in");
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	clusterManager.getNetwork("test", (result) -> {
	  ActiveNetwork network = result.result();
	  network.createConnection("foo", "out", "bar", "in");
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The active network API also supports `AsyncResult` handlers so you can determine
when the network has been updated with the new configuration.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	network.createConnection("foo", "out", "bar", "in", new Handler<AsyncResult<ActiveNetwork>>() {
	  public void handle(AsyncResult<ActiveNetwork> result) {
	    // Connection has been added and connected.
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
	network.createConnection("foo", "out", "bar", "in", (result) -> {
	  // Connection has been added and connected.
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Each `ActiveNetwork` also contains an internal `NetworkConfig` which can be
retrieved by the `getConfig` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	NetworkConfig config = network.getConfig();
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The active network's internal `NetworkConfig` will be automatically updated when
the running network configuration is updated.

## Deploying a network from the command line

Vertigo provides a special facility for deploying networks from json configuration files.
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
