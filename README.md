Vine
====

Vine is a real-time computation engine built on the Vert.x application platform.
Similar to Storm, work can be distributed across clusters with a central module
assigning work to Vert.x instances in the cluster. Messages are tracked as they
work their way through a *vine*, and messages that do not complete processing
through a *vine* are re-transmitted through the vine.

Vine instances can be easily integrated with your Vert.x application. Users can
deploy a *vine* across a Vert.x cluster with a single method call.

## Concepts

### Vine
A *vine* is a collection of Vert.x worker verticles that are linked together
by a reliable `EventBus` implementation. Vines are started by creating a
`VineDefinition` and deploying the vine using a `Root`.

Vine supports two differen modes, local and remote.

#### Local Vines
Local vines are *vines* that are contained within a single Vert.x instance.
When running a vine in local mode, all one needs to do is instantiate a
`LocalRoot` instance and deploy the vine. Workers will be deployed using
the local Vert.x `Container` instance.

#### Remote Vines
Remote vines are *vines* that are distributed across multiple machines. Running
remote vines requires significantly more setup as bus modules must be running
on each node in order to deploy modules on a given machine.

### Root
The Vine *root* is the API that handles deploying of vines. In local mode, the
*root* uses the local Vert.x `Container` to deploy vine workers. But Vine also
provides a *vine-root* module - a bus module that monitors a cluster and handles
assigning tasks to machines.

#### Root Commands

`deploy`

```
{
  "action": "deploy",
  "definition": {
    "address": "my.vine",
    ...
  }
}
```

`undeploy`

```
{
  "action": "undeploy",
  "address": "my.vine"
}
```

### Stem
*Stems* are only relevant in the context of *remote vines*. In the case of local
vines, the `LocalRoot` handles deploying worker verticles. However, in remote
vines the *root* runs on a single machine and it assigns work to *stems*. So, in
remote Vine systems the *stems* jobs are to start, stop, and restart worker
verticles as necessary.

### Seed
A *seed* is a representation of a single verticle in a *vine*. Each seed has a
unique name and can contain any number of *workers*.

### Worker
A *worker* is an instance of a *seed*. Each worker has a unique eventbus address.

## Communication
Vine has a number of different methods for transmitting messages between
workers in a Vine. Each *worker* verticle maintains a connection to all of the
*worker* verticles to which it is connected - this collection is called a channel -
with each channel supporting a custom dispatch method, for instance round-robin,
random, or consistent hashing.

## How it works

### Vine Definitions
Vines are defined in JSON format - a format that is native to Vert.x and
is understood by all its language implementations. Vine provides helper
classes for defining vines.

```java
VineDefinition vine = Vine.createDefinition("my.vine.address");
vine.feed(Seed.createDefinition("seed1").setMain("com.mycompany.SomeVerticle").setWorkers(2).groupBy("random"));
vine.feed("seed2", "com.mycompany.SomeOtherVerticle", 2).groupBy("round");
new LocalRoot("vine.root").deploy(vine, new Handler<AsyncResult<Feeder>>() ...);
```

Vine definition helpers create an internal JSON representation of the
vine definition that might look something like this:

```
{
  "address": "my.vine.address",
  "connections": ["seed1"],
  "seeds": {
    "seed1": {
      "name": "seed1",
      "main": "com.mycompany.SomeVerticle",
      "workers": 2,
      "grouping": "random",
      "connections": ["seed2"]
    },
    "seed2": {
      "name": "seed2",
      "main": "com.mycompany.SomeOtherVerticle",
      "workers": 2,
      "grouping": "round",
      "connections": []
    }
  }
}
```

### Vine Contexts
Internally, when a vine is being prepared for deployment the vine definition
is converted to a context. The context assigns addresses to each seed
worker, and resolves connections between seeds. Again, Vine provides helpers
for working with contexts, and contexts are ultimately structured as JSON:

```
{
  "my.vine.address",
  "connections": {
    "seed1": {
      "grouping": "random",
      "addresses": [
        "my.vine.address.seed1.1",
        "my.vine.address.seed1.2"
      ]
    }
  },
  "definition": {
    "address": "my.vine.address",
    "connections": ["seed1"],
    "seeds": {
      "seed1": {
        "name": "seed1",
        "main": "com.mycompany.SomeVerticle",
        "workers": 2,
        "grouping": "random",
        "connections": ["seed2"]
      },
      "seed2": {
        "name": "seed2",
        "main": "com.mycompany.SomeOtherVerticle",
        "workers": 2,
        "grouping": "round",
        "connections": []
      }
    }
  },
  # List of seed contexts.
  "seeds": {
    
  }
}
```

#### Example Vine Definition

## A Brief Example

### Java
```java
public class CountVerticle extends Verticle {

  @Override
  public void start() {
    // Create a new vine definition.
    VineDefinition definition = Vine.createDefinition("my.vine");

    // Feed all messages into the vine to seed1 *and* seed2.
    SeedDefinition seed1 = definition.feed(Seed.createDefinition("seed1").setMain("SeedOne.java"));
    SeedDefinition seed2 = definition.feed(Seed.createDefinition("seed2").setMain("SeedTwo.java"));

    // Feed messages from seed1 and seed2 to seed3.
    SeedDefinition seed3 = Seed.createDefinition("seed3").setMain("SeedThree.java").setWorkers(2);
    seed1.to(seed3);
    seed2.to(seed3);

    // Create a new local root. The local root will deploy seed workers within
    // the local Vert.x instance, so there's no additional setup required.
    Root root = new LocalRoot(vertx, container);

    // Deploy the vine. Once the vine is deployed, the result handler will be
    // invoked with a feeder to the vine.
    root.deploy(definition, new Handler<AsyncResult<Feeder>>() {
      @Override
      public void handle(AsyncResult<Feeder> result) {
        // If the vine failed to deploy the log the failure and return.
        if (result.failed()) {
          container.logger().info("Failed to deploy vine.");
          return;
        }

        Feeder feeder = result.result();

        // Create an asynchronous result handler for vine results.
        Handler<AsyncResult<Integer>> resultHandler = new Handler<AsyncResult<Integer>>() {
          @Override
          public void handle(Integer count) {
            container.logger().info("Word count is " + count);
          }
        };

        // Create a netserver that feeds data to the vine.
        vertx.createNetServer().connectHandler(new Handler<NetSocket>() {
          @Override
          public void handle(NetSocket socket) {
            socket.dataHandler(new Handler<Buffer>() {
              @Override
              public void handle(Buffer buffer) {
                if (!feeder.feedQueueFull()) {
                  feeder.feed(buffer, resultHandler);
                }
                else {
                  // If the feeder queue is full then pause the socket and
                  // set a drain handler on the feeder.
                  socket.pause();
                  feeder.drainHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void result) {
                      socket.resume();
                    }
                  });
                }
              }
            });
          }
        });
      }
    });
  }

}
```

#### Defining a seed verticle
Seed verticles are defined by extending the abstract `com.blankstyle.vine.SeedVerticle`
class and overriding the protected `process()` method. All Vine data is passed as
`JsonObject` instances for interoperability with other languages through Vert.x. Once
data has been processed, it should be emitted by calling the `emit()` method. Note that
*all messages must be emitted from all seed verticles at least once.* This is important
because messages are tracked as they make their way through the vine, and failing to
emit messages *will result in the messages being replayed through the vine.*

```java
import com.blankstyle.vine.SeedVerticle;

class SeedOne extends SeedVerticle {

  @Override
  protected void process(JsonObject data) {
    JsonObject result = new JsonObject();
    result.putNumber("count", data.getString("body").length());
    emit(result);
  }

}
```

### Javascript
```javascript
var vertx = require('vertx');
var container = require('vertx/container');
var vine = require('vine');
var vine_local = require('vine/local');

var definition = vertx.createVineDefinition('my.vine');
...

var root = new vine_local.LocalRoot(vertx, container);
root.deploy(definition, function(error, feeder) {
  if (!error) {
    feeder.feed('Hello world!');
  }
});
```

### Python
```python
import vine;
import vine.local;

definition = vine.create_vine_definition('my.vine')
seed1 = definition.feed(vine.create_seed_definition('seed1', main='SeedOne.java'))
seed2 = definition.feed(vine.create_seed_definition('seed2', main='SeedTwo.java'))

seed3 = vine.create_seed_definition('seed3', main='SeedThree.java', workers=2)

seed1.to(seed3)
seed2.to(seed3)

root = vine.local.LocalRoot()

def deploy_handler(error, feeder):
  if not error:
    feeder.feed(u'Hello world!')
```

### PHP
```php
use Vine\Vine;
use Vine\Seed;
use Vine\Local\LocalRoot;

$definition = Vine::createDefinition('my.vine');

$seed1 = $definition->feed(Seed::createDefinition('seed1')->setMain('SeedOne.java'));
$seed2 = $definition->feed(Seed::createDefinition('seed2')->setMain('SeedTwo.java'));

$seed3 = Seed::createDefinition('seed3')->setMain('SeedThree.java')->setWorkers(2);
$seed1->to($seed3);
$seed2->to($seed3);

$root = new LocalRoot();

$root->deploy($definition, function($feeder, $error) {
  if (empty($error)) {
    $feeder->feed('Hello world!');
  }
});
```
