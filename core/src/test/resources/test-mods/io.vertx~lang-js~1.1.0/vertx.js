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
 * The 'vertx' module provides all of the vertx API namespaced 
 * under 'vertx'. For example:
 *
 * @example
 * var vertx  = require('vertx');
 * var buff   = new vertx.Buffer('some string');
 * var bus    = vertx.eventBus;
 * var client = vertx.http.createHttpClient();
 *
 * // Each of the modules imported by vertx may also be required as 
 * //individual modules. For example:
 *
 * var http   = require('vertx/http');
 * var server = http.createHttpServer();
 * var client = http.createHttpClient();
 *
 * var Buffer = require('vertx/buffer');
 * var buff   = new Buffer('another string');
 *
 * @exports vertx
 */
var vertx = {};

/** 
 * The vert.x Buffer class. 
 * @see module:vertx/buffer~Buffer
 * */
vertx.Buffer = require('vertx/buffer');

/**
 * The vert.x distributed event bus.
 * @see module:vertx/event_bus
 */
vertx.eventBus = require('vertx/event_bus');

/**
 * The vert.x net module
 * @see module:vertx/net
 */
vertx.net = require('vertx/net');

/**
 * The vert.x http module
 * @see module:vertx/http
 */
vertx.http = require('vertx/http');

/**
 * The vert.x stream pump.
 * @see module:vertx/pump~Pump
 */
vertx.Pump = require('vertx/pump');

/**
 * The vert.x timer module
 * @see module:vertx/timer
 */
vertx.timer = require('vertx/timer');

// TODO: Document and test this
vertx.sockJS = require('vertx/sockjs');

/**
 * The vert.x parseTools module
 * @see module:vertx/parse_tools
 */
vertx.parseTools = require('vertx/parse_tools');

/**
 * The vert.x sharedData module
 * @see module:vertx/shared_data
 */
vertx.sharedData = require('vertx/shared_data');

/**
 * The vert.x fileSystem module
 * @see module:vertx/file_system
 */
vertx.fileSystem = require('vertx/file_system');

/**
 * Put the task on the event queue for this loop so it will be run asynchronously
 * immediately after this event is processed.
 *
 * @param {Handler} handler The handler to be called
 */
vertx.runOnContext = function(task) {
  __jvertx.runOnContext(task);
};

vertx.currentContext = function() {
  return __jvertx.currentContext();
};

// For compatability with functions that aren't namespaced to the module.
// For example: `vertx.createHttpServer()` instead of the namespaced
// version `vertx.http.createHttpServer()`.
// These are not documented any longer, and may be deprecated some day.
function addProps(obj) {
  for (var key in obj) {
    if (obj.hasOwnProperty(key)) {
      vertx[key] = obj[key];
    }
  }
}
addProps(vertx.net);
addProps(vertx.http);
addProps(vertx.timer);
addProps(vertx.sockJS);
addProps(vertx.parseTools);
addProps(vertx.sharedData);
addProps(vertx.fileSystem);

module.exports = vertx;

// JSDoc @typedef declarations go here. This is primarily a documentation
// convenience that allows us to document named types for things that don't
// really have names in this API, e.g. a RequestHandler

/**
 * Vert.x makes heavy use of callback handlers in the API. A basic handler is
 * simply a function that is called when vert.x events are fired, and expects
 * no parameters.
 *
 * @see module:vertx.runOnContext
 * @typedef {function} Handler
 */


