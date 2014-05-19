---
layout: content
menu: user-manual
title: Networks
---

# Networks

* [Creating a new network](#creating-a-new-network)
* [Adding components to a network](#adding-components-to-a-network)
* [Creating connections between components](#)
* [Routing messages between multiple component instances](#routing-messages-between-multiple-component-instances)
* [Creating networks from JSON](#creating-networks-from-json)

{% include lang-tabs.html %}

Vertigo networks are collections of Vert.x verticles and modules that are connected
together by the Vert.x event bus. Networks and the relationships therein are defined
externally to their components, promoting reusability.

Each Vertigo network must have a unique name within the Vert.x cluster in which it
is deployed. Vertigo uses the network name to coordinate deployments and configuration
changes for the network.

Networks are made up of any number of components which can be arbitrarily connected
by input and output ports. A Vertigo component is simple a Vert.x module or verticle,
and can thus have any number of instances associated with it.

## Creating a network

To create a new network, create a new `Vertigo` instance and call the `createNetwork` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	Vertigo vertigo = new Vertigo(this);
	NetworkConfig network = vertigo.createNetwork("my-network");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

All Vertigo networks have an explicit, unique name. This name is very important to
Vertigo as it can be used to reference networks from anywhere within a Vert.x cluster,
but more on that later.

## Adding components to a network

To add a component to the network, use one of the `addVerticle` or `addModule` methods.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	network.addVerticle("foo", "foo.js");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The `addVerticle` and `addModule` methods have the following signatures:

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	addModule(String name, String moduleName);
	addModule(String name, String moduleName, JsonObject config);
	addModule(String name, String moduleName, int instances);
	addModule(String name, String moduleName, JsonObject config, int instances);
	addVerticle(String name, String main);
	addVerticle(String name, String main, JsonObject config);
	addVerticle(String name, String main, int instances);
	addVerticle(String name, String main, JsonObject config, int instances);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Just as with networks, Vertigo components are explicitly named. The component name
*must be unique within the network to which the component belongs*.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	NetworkConfig network = vertigo.createNetwork("test");
	network.addVerticle("foo", "foo.js", 2);
	network.addModule("bar", "com.bar~bar~1.0", 4);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The `NetworkConfig` API also exposes an abstract `addComponent` method which detects
whether the added component is a module or a verticle based on module naming conventions.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	addComponent(String name, String moduleOrMain);
	addComponent(String name, String moduleOrMain, JsonObject config);
	addComponent(String name, String moduleOrMain, int instances);
	addComponent(String name, String moduleOrMain, JsonObject config, int instances);

	network.addComponent("foo", "foo.js", 2); // Adds a verticle component.
	network.addComponent("bar", "com.bar~bar~1.0", 4); // Adds a module component.
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Once a component has been added to the network, the component configuration will
be returned. Users can set additional options on the component configuration. The
most important of these options is the `group` option. When deploying networks within
a Vert.x cluster, the `group` indicates the HA group to which to deploy the module or
verticle.

## Creating connections between components

A set of components is not a network until connections are created between those
components. Vertigo uses a concept of *ports* to abstract input and output from
each component instance. When creating connections between components, you must
specify a component and port to which the connection connects. Each connection
binds one component's output port with another component's input port.

To create a connection between two components use the `createConnection` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	network.createConnection("foo", "out", "bar", "in");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

## Routing messages between multiple component instances

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

## Creating networks from JSON

Vertigo supports creating networks from json configurations. To create a network
from json call the `Vertigo.createNetwork(JsonObject)` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane java">

{:.prettyprint .lang-java}
	JsonObject json = new JsonObject().putString("name", "test-network");
	vertigo.createNetwork(json);

</div>	
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

{:.prettyprint .lang-json}
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

JSON network configurations can be used to deploy Vertigo networks from the command
line using the `vertx` command line tool. For more information see
[deploying networks from the command line](#deploying-networks-from-the-command-line)