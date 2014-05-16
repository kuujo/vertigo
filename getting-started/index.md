---
layout: content
menu: getting-started
title: Getting Started
---

# Getting Started
This is a brief tutorial that will help guide you through high-level Vertigo
concepts and go through a simple network example. Check out the repository
for more [examples](https://github.com/kuujo/vertigo/tree/master/examples).

{% include lang-tabs8.html %}

## Setup
Vertigo can be added to your project as a Maven dependency or included in
your modules via the Vert.x module system.

### Adding Vertigo as a Maven dependency

{:.prettyprint .lang-xml}
	<dependency>
	  <groupId>net.kuujo</groupId>
	  <artifactId>vertigo</artifactId>
	  <version>0.7.0-beta2</version>
	</dependency>

### Including Vertigo in a Vert.x module

To use the Vertigo Java API, you can include the Vertigo module in your module's
`mod.json` file. This will make Vertigo classes available within your module.

{:.prettyprint .lang-json}
	{
	  "main": "com.mycompany.myproject.MyVerticle",
	  "includes": "net.kuujo~vertigo~0.7.0-beta2"
	}

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

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	public class MyComponent extends ComponentVerticle {
	  @Override
	  public void start() {
	    input.port("in").messageHandler((message) -> output.port("out").send(message));
	  }
	}
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	NetworkConfig network = vertigo.createNetwork("foo");
	network.addComponent("bar", "bar.js", 2);
	network.addComponent("baz", "baz.py", 4);
	network.createConnection("bar", "out", "baz", "in");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

## The Vertigo Cluster
Vertigo provides its own cluster abstraction within the Vert.x cluster. Vertigo
clusters are simple collections of verticles that manage deployment of networks,
allow modules and verticles to be deploy remotely (over the event bus)
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

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	Vertigo vertigo = new Vertigo(this);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The `Vertigo` object supports creating and deploying networks. Each language
binding has an equivalent API.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	NetworkConfig network = vertigo.createNetwork("word-count");
	network.addComponent("word-feeder", RandomWordCounter.class.getName());
	network.addComponent("word-counter", WordCounter.class.getName(), 2);
	network.createConnection("word-feeder", "word", "word-counter", "word", new HashSelector());
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

TODO
	
</div>
<div class="tab-pane python">
  
{:.prettyprint .lang-python}
	import vertx
	from vertigo import component, input
	
	@component.start_handler
	def start_handler(error=None):
	  if not error:
	    words = ['apple', 'banana', 'pear']
	    def feed_random_word(timer_id):
	      output.send('word', words[rand(len(words)-1)])
	    vertx.set_periodic(1000, feed_random_word)
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Here we simply send a random word to the `word` out port every second.

`word_counter.js`

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

TODO
	
</div>
<div class="tab-pane python">
  
{:.prettyprint .lang-javascript}
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
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

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

{:.prettyprint .lang-json}
	{
	  "cluster": "test-cluster"
	}

A test cluster can also be deployed locally through the `Vertigo` API.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	vertigo.deployCluster("test-cluster", new Handler<AsyncResult<ClusterManager>>() {
	  public void handle(AsyncResult<ClusterManager> result) {
	    ClusterManager cluster = result.result();
	  }
	});
	
</div>
<div class="tab-pane java8">

{:.prettyprint .lang-java}
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

Once the cluster has been deployed we can deploy a network to the cluster.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	cluster.deployNetwork(network);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

Vertigo also supports deploying networks from the command line using simple
JSON configuration files. See [deploying a network from the command line](#deploying-a-network-from-the-command-line).