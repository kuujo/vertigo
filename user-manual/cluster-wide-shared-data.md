---
layout: content
menu: user-manual
title: Cluster Wide Shared Data
---

# Cluster Wide Shared Data

* [AsyncMap](#asyncmap)
* [AsyncSet](#asyncset)
* [AsyncList](#asynclist)
* [AsyncQueue](#asyncqueue)
* [AsyncCounter](#asynccounter)
* [Accessing shared data over the event bus](#accessing-shared-data-over-the-event-bus)

{% include lang-tabs8.html %}

Cluster-wide shared data structures are made available via the same API as clustering.
If the current Vert.x instance is a Hazelcast clustered instance then cluster-wide
shared data structures will be backed by Hazelcast data structures. If the current
Vert.x instance is not clustered then data structures will be backed by Vert.x
`SharedData` structures.

The cluster API is available in all components via the `cluster` field of the
`ComponentVerticle`.

### AsyncMap
The `AsyncMap` interface closely mimics the interface of the Java `Map` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">
  
{:.prettyprint .lang-java}
	final AsyncMap<String, String> map = cluster.getMap("foo");
	map.put("foo", "bar", new Handler<AsyncResult<String>>() {
	  public void handle(AsyncResult<String> result) {
	    if (result.succeeded()) {
	      map.get("foo", new Handler<AsyncResult<String>>() {
	        public void handle(AsyncResult<String> result) {
	          if (result.succeeded()) {
	            String foo = result.result();
	          }
	        }
	      });
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	final AsyncMap<String, String> map = cluster.getMap("foo");
	map.put("foo", "bar", (result) -> {
	  if (result.succeeded()) {
	    map.get("foo", (result1) -> {
	      if (result1.succeeded()) {
	        String foo = result1.result();
	      }
	    });
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

If the Vert.x instance is not clustered then Vertigo maps will be backed by
the Vert.x `ConcurrentSharedMap`. If the Vert.x instance is clustered then maps
will be backed by Hazelcast maps that are accessed over the event bus in a Xync
worker verticle to prevent blocking the event loop.

### AsyncSet
The `AsyncSet` interface closely mimics the interface of the Java `Set` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	final AsyncSet<String> set = cluster.getSet("foo");
	set.add("bar", new Handler<AsyncResult<Boolean>>() {
	  public void handle(AsyncResult<Boolean> result) {
	    if (result.succeeded()) {
	      set.remove("bar");
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	final AsyncSet<String> set = cluster.getSet("foo");
	set.add("bar", (result) -> {
	  if (result.succeeded()) {
	    set.remove("bar");
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

If the Vert.x instance is not clustered then Vertigo sets will be backed by
the Vert.x `SharedData` sets. If the Vert.x instance is clustered then sets
will be backed by Hazelcast sets that are accessed over the event bus in a Xync
worker verticle to prevent blocking the event loop.

### AsyncList
The `AsyncList` interface closely mimics the interface of the Java `List` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	AsyncList<String> list = cluster.getList("foo");
	list.add("bar", new Handler<AsyncResult<Boolean>>() {
	  public void handle(AsyncResult<Boolean> result) {
	    if (result.succeeded()) {
	      list.remove(0);
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	AsyncList<String> list = cluster.getList("foo");
	list.add("bar", (result) -> {
	  if (result.succeeded()) {
	    list.remove(0);
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

If the Vert.x instance is not clustered then Vertigo lists will be backed by
a custom list implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then lists will be backed by Hazelcast lists that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### AsyncQueue
The `AsyncQueue` interface closely mimics the interface of the Java `Queue` interface,
but uses `Handler<AsyncResult<T>>` rather than return values.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	final AsyncQueue<String> queue = cluster.getQueue("foo");
	queue.add("bar", new Handler<AsyncResult<Boolean>>() {
	  public void handle(AsyncResult<Boolean> result) {
	    if (result.succeeded()) {
	      queue.poll(new Handler<AsyncResult<String>>() {
	        public void handle(AsyncResult<String> result) {
	          if (result.succeeded()) {
	            String value = result.result();
	          }
	        }
	      });
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	final AsyncQueue<String> queue = cluster.getQueue("foo");
	queue.add("bar", (result) -> {
	  if (result.succeeded()) {
	    queue.poll((result1) -> {
	      if (result.succeeded()) {
	        String value = result.result();
	      }
	    });
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

If the Vert.x instance is not clustered then Vertigo queues will be backed by
a custom queue implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then queues will be backed by Hazelcast queues that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### AsyncCounter
The `AsyncCounter` facilitates generating cluster-wide counters.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">

{:.prettyprint .lang-java}
	AsyncCounter counter = cluster.getCounter("foo");
	counter.incrementAndGet(new Handler<AsyncResult<Long>>() {
	  public void handle(AsyncResult<Long> result) {
	    if (result.succeeded()) {
	      long value = result.result();
	    }
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	AsyncCounter counter = cluster.getCounter("foo");
	counter.incrementAndGet((result) -> {
	  if (result.succeeded()) {
	    long value = result.result();
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

If the Vert.x instance is not clustered then Vertigo counters will be backed by
a custom counter implementation on top of the Vert.x `ConcurrentSharedMap`. If the
Vert.x instance is clustered then counters will be backed by Hazelcast maps that are
accessed over the event bus in a Xync worker verticle to prevent blocking the event loop.

### Accessing shared data over the event bus
As with network and module/verticle deployments, cluster-wide shared data structures
can be accessed directly over the event bus. Data actions relate directly to their
API methods. Each shared data message must contain a `type` and the `name` of the
data structure to which the message refers. For example, to `put` a value in the
`foo` map we do the following:

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">

{:.prettyprint .lang-java}
	// Put key "bar" to "baz" in map "foo"
	JsonObject message = new JsonObject()
	  .putString("type", "map")
	  .putString("name", "foo")
	  .putString("action", "put")
	  .putString("key", "bar")
	  .putString("value", "baz");
	vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
	  public void handle(Message<JsonObject> reply) {
	    if (reply.body().getString("status").equals("ok")) {
	      // Get the value of key "bar" in map "foo"
	      JsonObject message = new JsonObject()
	        .putString("type", "map")
	        .putString("name", "foo")
	        .putString("action", "get")
	        .putString("key", "bar");
	      vertx.eventBus().send("test-cluster", message, new Handler<Message<JsonObject>>() {
	        public void handle(Message<JsonObject> reply) {
	          if (reply.body().getString("status").equals("ok")) {
	            String value = reply.body().getString("result");
	          }
	        }
	      });
	    }
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