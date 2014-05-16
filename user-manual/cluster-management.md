---
layout: content
menu: user-manual
title: Cluster Management
---

# Cluster Management

* [Accessing the cluster from within a component](#accessing-the-cluster-from-within-a-component)
* [Deploying modules and verticles to the cluster](#deploying-modules-and-verticles-to-the-cluster)
* [Undeploying modules and verticles from the cluster](#undeploying-modules-and-verticles-from-the-cluster)
* [Checking if a module or verticle is deployed](#checking-if-a-module-or-verticle-is-deployed)
* [Deploying modules and verticles with HA](#deploying-modules-and-verticles-with-ha)
* [Working with HA groups](#working-with-ha-groups)

{% include lang-tabs8.html %}

Vertigo clusters support remote deployments over the event bus through
[Xync](http://github.com/kuujo/xync). Users can use the Vertigo cluster API to
remotely deploy Vert.x modules and verticles from Vertigo components.

### Accessing the cluster from within a component
The Xync cluster is made available to users through the `cluster` field within
the `ComponentVerticle` class. The `cluster` within any given component will always
reference the Vertigo cluster to which the component's parent network belongs. This
means that deployments made through the `cluster` will be separated in the same
way that networks are separated from each other across clusters.

### Deploying modules and verticles to the cluster
The Vertigo cluster supports remote deployment of modules and verticles over the
event bus. The `Cluster` API wraps the event bus API and mimics the core Vert.x
`Container` interface. To deploy a module or verticle simply call the appropriate
method on the component `Cluster` instance:

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">
  
{:.prettyprint .lang-java}
	public class MyComponent extends ComponentVerticle {
	  @Override
	  public void start() {
	    cluster.deployVerticle("foo", "foo.js", new Handler<AsyncResult<String>>() {
	      public void handle(AsyncResult<String> result) {
	        if (result.succeeded()) {
	          // Successfully deployed the verticle!
	        }
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
	    cluster.deployVerticle("foo", "foo.js", (r) -> {
	      if (result.succeeded()) {
	        // Successfully deployed the verticle!
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

The `Cluster` API differs in one important aspect from the `Container` API. Because
cluster deployments are remote, users must provide an *explicit* deployment ID for
each deployment. This ensures that even if the instance from which the module/verticle
was deployed fails, the deployment can still be referenced from different Vert.x
instances. If the deployment ID of a deployment already exists then the deployment
will fail.

The internal component cluster is the same cluster to which the component's parent network
belongs. That means that deployment IDs are unique to each cluster. You can deploy
a module with the deployment ID `foo` in two separate clusters at the same time.

### Undeploying modules and verticles from the cluster
To undeploy a module or verticle from the cluster call the `undeployModule` or
`undeployVerticle` method, using the user-defined deployment ID.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">
  
{:.prettyprint .lang-java}
	cluster.undeployVerticle("foo");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

### Deploying modules and verticles with HA
Like Vert.x clusters, the Vertigo clusters supports HA deployments. By default, modules
and verticles are not deployed with HA enabled.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">
  
{:.prettyprint .lang-java}
	cluster.deployVerticle("foo", "foo.js", null, 1, true);
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

The last argument in the arguments list indicates whether to deploy the deployment
with HA. When a Vertigo cluster node fails, any deployments deployed with HA enabled
on that node will be taken over by another node within the same group within the cluster.

### Checking if a module or verticle is deployed
Since deployment IDs in Vertigo clusters are user-defined, users can determine whether
a module or verticle is already deployed with a specific deployment ID. To check if
a deployment is already deployed in the cluster use the `isDeployed` method.

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java">
  
{:.prettyprint .lang-java}
	cluster.isDeployed("foo", new Handler<AsyncResult<Boolean>>() {
	  public void handle(AsyncResult<Boolean> result) {
	    boolean deployed = result.result(); // Whether the module or verticle is deployed
	  }
	});
	
</div>
<div class="tab-pane java8">
  
{:.prettyprint .lang-java}
	cluster.isDeployed("foo", (r) -> {
	  boolean deployed = result.result(); // Whether the module or verticle is deployed
	});
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

To check if a module or verticle is deployed over the event bus send a `check` message
to the cluster, specifying the `module` or `verticle` as the check `type`.

{:.prettyprint .lang-json}
	{
	  "action": "check",
	  "type": "verticle",
	  "id": "foo"
	}

If the request is successful, the cluster will reply with a `result` containing a
boolean indicating whether the deployment ID exists in the cluster.

### Working with HA groups
Vertigo's HA grouping mechanism is intentionally designed to mimic the core HA behavior.
Each Vertigo node can be assigned to a specific HA group, and when a node fails its HA
deployments will be taken over by another node in the same group.

Modules or verticles can also be deployed directly to a specific HA group. To deploy
a module or verticle to an HA group call the `deployModuleTo` or `deployVerticleTo`
methods respectively, passing the target HA group as the second argument (after the
deployment ID).

{::options parse_block_html="true" /}
<div class="tab-content">
<div class="tab-pane active java java8">
  
{:.prettyprint .lang-java}
	cluster.deployVerticleTo("foo", "my-group", "foo.js");
	
</div>
<div class="tab-pane python">
  
TODO
	
</div>
<div class="tab-pane javascript">
  
TODO
	
</div>
</div>

By default, all deployments are deployed to the `__DEFAULT__` HA group.