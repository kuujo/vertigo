Vitis
=====

Vitis is a distributed event processing framework built on the
[Vert.x](http://vertx.io/) application platform. Similar to Storm, work
can be distributed across clusters with a central module assigning work
to Vert.x instances in the cluster.

## Features
* Built on the [Vert.x](http://vertx.io/) application platform, so core Vert.x
  APIs, languages, and contributed modules can be used seamlessly with Vine.
* Easy to learn and integrate with existing applications using the familiar
  Vert.x handler system and Vert.x' native support for JSON.
* Provides limited reliability features such as timeouts, heartbeats, and
  message acking.
* Allows networks to be deployed locally within a single Vert.x instance or across
  a cluster of Vert.x instances.
* Supports remote-procedure calls, allowing for real-time request-response
  spanning multiple Vert.x instances.

### [See the documentation for tutorials and examples](https://github.com/kuujo/vitis/wiki/Vitis)
