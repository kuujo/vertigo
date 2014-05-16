---
layout: content
menu: user-manual
title: Logging
---

# Logging

* [Logging messages to output ports](#logging-messages-to-output-ports)
* [Reading log messages](#reading-log-messages)

{% include lang-tabs.html %}

Each Vertigo component contains a special `PortLogger` which logs messages
to component output ports in addition to standard Vert.x log files. This allows
other components to listen for log messages on input ports.

The `PortLogger` logs to ports named for each logger method:
* `fatal`
* `error`
* `warn`
* `info`
* `debug`
* `trace`

## Logging messages to output ports

The `PortLogger` simple implements the standard Vert.x `Logger` interface.
So, to log a message to an output port simply call the appropriate log method:

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	public class MyComponent extends ComponentVerticle {
	  @Override
	  public void start() {
	    logger.info("Component started successfully!");
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

## Reading log messages

To listen for log messages from a component, simply add a connection to a network
configuration listening on the necessary output port. For instance, you could
aggregate and count log messages from one component by connecting each log port to
a single input port on another component.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	NetworkConfig network = vertigo.createNetwork("log-test");
	network.addVerticle("logger", "logger.js", 2);
	network.addVerticle("log-reader", LogReader.class.getName(), 2);
	network.createConnection("logger", "fatal", "log-reader", "log").hashSelect();
	network.createConnection("logger", "error", "log-reader", "log").hashSelect();
	network.createConnection("logger", "warn", "log-reader", "log").hashSelect();
	network.createConnection("logger", "info", "log-reader", "log").hashSelect();
	network.createConnection("logger", "debug", "log-reader", "log").hashSelect();
	network.createConnection("logger", "trace", "log-reader", "log").hashSelect();
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

With a hash selector on each connection, we guarantee that the same log message
will always go to the same `log-reader` instance.

Log messages will arrive as simple strings:

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
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
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>