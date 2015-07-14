/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

if (typeof __vertxload === 'string') {
  throw "Use require() to load Vert.x API modules";
}

/** 
 * A DeploymentId is used to identify a specific verticle deployment.
 * @see module:vertx/container
 * @typedef {string} DeploymentId 
 * */

/**
 * The vert.x container. The container handles deploying and undeploying
 * modules, and overall control of the runtime.
 *
 * @exports vertx/container
 */

var container = {};

var VERTICLE = 0;
var WORKER = 1;
var MODULE = 2;

var helpers = require("vertx/helpers.js");

function deploy(deployType, name, args) {
  var doneHandler = helpers.getArgValue('function', args);
  var multiThreaded = helpers.getArgValue('boolean', args);
  var instances = helpers.getArgValue('number', args);
  var config = helpers.getArgValue('object', args);
  if (config !== null) {
    // Convert to Java Json Object
    var str = JSON.stringify(config);
    java.lang.System.err.println("GOT CONFIG: " + str);
    config = new org.vertx.java.core.json.JsonObject(str);
  }
  doneHandler = helpers.adaptAsyncResultHandler(doneHandler);
  if (multiThreaded === null) {
    multiThreaded = false;
  }
  if (instances === null) {
    instances = 1;
  }

  switch (deployType) {
    case VERTICLE: {
      __jcontainer.deployVerticle(name, config, instances, doneHandler);
      break;
    }
    case WORKER: {
      __jcontainer.deployWorkerVerticle(name, config, instances, multiThreaded, doneHandler);
      break;
    }
    case MODULE: {
      __jcontainer.deployModule(name, config, instances, doneHandler);
      break;
    }
  }
}

/**
 * Deploy a verticle. The actual deploy happens asynchronously
 * @param {string} main the main of the verticle to deploy
 */
container.deployVerticle = function(main) {
  var args = Array.prototype.slice.call(arguments);
  args.shift();
  deploy(VERTICLE, main, args);
};

/**
 * Deploy a verticle. The actual deploy happens asynchronously
 * @param {string} main the main of the verticle to deploy
 */
container.deployWorkerVerticle = function(main) {
  var args = Array.prototype.slice.call(arguments);
  args.shift();
  deploy(WORKER, main, args);
};

/**
 * Deploy a module. The actual deploy happens asynchronously
 *
 * @param {string} moduleName The name of the module to deploy
 */
container.deployModule = function(moduleName) {
  var args = Array.prototype.slice.call(arguments);
  args.shift();
  deploy(MODULE, moduleName, args);
};

/**
 * Undeploy a verticle
 *
 * @param {DeploymentId} id The unique id of the deployment
 * @param {Handler} handler A handler that will be called when undeploy has completed
 */
container.undeployVerticle = function(name, doneHandler) {
  if (doneHandler) {
    doneHandler = helpers.adaptAsyncResultHandler(doneHandler);
  } else {
    doneHandler = null;
  }
  __jcontainer.undeployVerticle(name, doneHandler);
};

/**
 * Undeploy a module
 *
 * @param {DeploymentId} id The unique id of the module
 * @param {Handler} handler A handler that will be called when undeploy has completed
 */
container.undeployModule = function(name, doneHandler) {
  if (doneHandler) {
    doneHandler = helpers.adaptAsyncResultHandler(doneHandler);
  } else {
    doneHandler = null;
  }
  __jcontainer.undeployModule(name, doneHandler);
};

/**
 * Causes the container to exit. All running modules will be undeployed.
 */
container.exit = function() {
  __jcontainer.exit();
};
var j_conf = __jcontainer.config();
container.config =  j_conf === null ? null : JSON.parse(j_conf.encode());

/**
 * The container's environment variables
 */
container.env = {
  get: function(key) {
    return this[key];
  }
};

var envIter = __jcontainer.env().entrySet().iterator();
while(envIter.hasNext()) {
  var entry = envIter.next();
  container.env[entry.getKey()] = entry.getValue();
}

/**
 * The container's logger
 */
container.logger = __jcontainer.logger();

module.exports = container;


