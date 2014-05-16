---
layout: content
menu: user-manual
title: Components
---

# Components

* [Creating a component](#creating-a-component)
* [The elements of a Vertigo component](#the-elements-of-a-vertigo-component)

{% include lang-tabs.html %}

Networks are made up of any number of *components* which are simply Vert.x verticles or
modules that are connected together according to the network configuration. Each component
is a "black box" that receives input on named input ports and sends output to named output
ports. By their nature, components do not know from where they received messages or to where
they're sending messages.

### Creating a component
To create a Java component, extend the base `ComponentVerticle` class.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	public class MyComponent extends ComponentVerticle {
	  @Override
	  public void start() {
	  
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