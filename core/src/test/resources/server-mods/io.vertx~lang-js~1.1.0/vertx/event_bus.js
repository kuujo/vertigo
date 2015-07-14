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

var helpers = require('vertx/helpers');
var Buffer  = require('vertx/buffer');

/**
 * While JSON is the preferred messaging on the event bus,
 * you can send any basic type as a message, for example,
 * <code>string</code>, <code>boolean</code>, etc. can all be passed
 * directly into {@linkcode module:vertx/event_bus.send}. You may also
 * send {@linkcode module:vertx/buffer~Buffer}s and plain old 
 * Javascript objects. Objects will be converted to <code>JSON</code>
 * before being placed on the bus.
 * @see module:vertx/event_bus
 * @typedef {string|boolean|number|{}|JSON|module:vertx/buffer~Buffer} Message
 */

/**
 * A <code>MessageHandler</code> is a {@linkcode Handler} that responds to
 * messages on the {@linkcode module:vertx/event_bus} module. <code>MessageHandler</code>s
 * are called with a {@linkcode Message} object as the parameter.
 *
 * @see module:vertx/event_bus.registerHandler
 * @typedef {function} MessageHandler
 * @param {Message} message The message
 */


/**
 * <p>Represents a distributed lightweight event bus which can encompass
 * multiple vert.x instances.  It is very useful for otherwise isolated vert.x
 * application instances to communicate with each other. Messages sent over the
 * event bus are JSON objects.</p>
 *
 * <p>The event bus implements a distributed publish / subscribe network.
 * Messages are sent to an address.  There can be multiple handlers registered
 * against that address.  Any handlers with a matching name will receive the
 * message irrespective of what vert.x application instance and what vert.x
 * instance they are located in.</p>
 *
 * <p>All messages sent over the bus are transient. On event of failure of all
 * or part of the event bus messages may be lost. Applications should be coded
 * to cope with lost messages, e.g. by resending them, and making application
 * services idempotent.</p>
 *
 * <p>The order of messages received by any specific handler from a specific
 * sender will match the order of messages sent from that sender.</p>
 *
 * <p>When sending a message, a reply handler can be provided. If so, it will
 * be called when the reply from the receiver has been received.</p>
 *
 * <p>This module can be used individually, or through the top-level
 * {@linkcode module:vertx|vertx} module.
 * 
 * @example <caption>Accessing the event bus</caption>
 *
 * var vertx = require('vertx');
 *
 * var eb1 = require('vertx/event_bus');
 * var eb2 = vertx.eventBus;
 *
 * eb1.registerHandler('some-address', function(message) {
 *   print("Got a message! " + message);
 * }
 * eb2.publish('some-address', 'Hello world');
 *
 * @exports vertx/event_bus
 */
var eventBus = {};

var handlerMap = {};

var jEventBus = __jvertx.eventBus();

/**
 * Register a handler which won't be propageted acress the cluster.
 *
 * @param {string} address the address to register for. Any messages sent to
 * that address will be received by the handler. A single handler can be
 * registered against many addresses.
 * @param {MessageHandler} handler The handler
 * @returns {module:vertx/event_bus} The event bus
 */
eventBus.registerLocalHandler = function(address, handler) {
  registerHandler(address, handler, true);
  return eventBus;
};

/**
 * Register a handler.
 *
 * @param {string} address the address to register for. Any messages sent to
 * that address will be received by the handler. A single handler can be
 * registered against many addresses.
 * @param {MessageHandler} handler The handler
 * @param {Handler} [registrationHandler] If supplied, this handler is called
 * when all nodes have registered this address
 *
 * @returns {module:vertx/event_bus} the event bus
 */
eventBus.registerHandler = function(address, handler, registrationHandler) {
  registerHandler(address, handler, false, registrationHandler);
  return eventBus;
};

/**
 * Unregisters a handler.
 *
 * @param {string} address The address the handler is registered to
 * @param {MessageHandler} handler The handler to unregister
 * @param {Handler} [registrationHandler] If supplied, this handler is called
 * when all nodes have unregistered this address
 * @returns {module:vertx/event_bus} the event bus
 */
eventBus.unregisterHandler = function(address, handler, registrationHandler) {
  checkHandlerParams(address, handler);
  var wrapped = handlerMap[handler];
  if (wrapped) {
    if (typeof registrationHandler == 'function') {
      jEventBus.unregisterHandler(address, wrapped, registrationHandler);
    } else {
      jEventBus.unregisterHandler(address, wrapped);
    }
    delete handlerMap[handler];
  }
  return eventBus;
};

/**
 * Sends a message on the event bus.
 *
 * @param {string} address The address to send the message to
 * @param {Message} message The message to send
 * @param {MessageHandler} [replyHandler] called when the message receives a reply
 * @returns {module:vertx/event_bus}
 */
eventBus.send = function(address, message, replyHandler) {
  sendOrPub(true, address, message, replyHandler);
  return eventBus;
};

/**
 * Sends a message on the event bus. If a reply is not received within
 * `timeout` milliseconds, the `replyHandler` will be called with an
 * error and empty message body, then discarded.
 *
 * @param {string} address The address to send the message to
 * @param {Message} message The message to send
 * @param {number} timeout The timeout period in milliseconds
 * @param {ResultHandler} resultHandler called when the message receives a reply, or when the timeout triggers
 * 
 */
eventBus.sendWithTimeout = function(address, message, timeout, replyHandler) {
  sendWithTimeout(address, message, replyHandler, timeout);
  return eventBus;
};

/**
 * Set the default reply time in milliseconds.
 * Unless changed, the default will be -1 which means messages sent on
 * the event bus will never timeout unless `sendWithTimeout()` is
 * used explicitly. Any other value will cause the event bus to
 * timeout reply hanlders in the given number of milliseconds.
 * @param {number} timeout the number of milliseconds to wait before timing out a handler
 * @returns {module:vertx/event_bus}
 */
eventBus.setDefaultReplyTimeout = function(timeout) {
  jEventBus.setDefaultReplyTimeout(timeout);
  return eventBus;
};

/**
 * Get the default reply timeout in milliseconds.
 * Unless changed, the default will be -1 which means messages sent on
 * the event bus will never timeout unless `sendWithTimeout()` is
 * used explicitly. Any other value will cause the event bus to
 * timeout reply hanlders in the given number of milliseconds.
 * @returns {number}
 */
eventBus.getDefaultReplyTimeout = function() {
  return jEventBus.getDefaultReplyTimeout();
};

/**
 * Publish a message on the event bus.
 * Message should be a JSON object It should have a property "address".
 *
 * @param {string} address The address to send the message to
 * @param {Message} message The message to send
 * @returns {module:vertx/event_bus}
 */
eventBus.publish = function(address, message) {
  sendOrPub(false, address, message);
  return eventBus;
};

function checkHandlerParams(address, handler) {
  if (!address) {
    throw "address must be specified";
  }
  if (!handler) {
    throw "handler must be specified";
  }
  if (typeof address != "string") {
    throw "address must be a string";
  }
  if (typeof handler != "function") {
    throw "handler must be a function";
  }
}

var jsonObjectClass = new org.vertx.java.core.json.JsonObject().getClass();
var jsonArrayClass  = new org.vertx.java.core.json.JsonArray().getClass();
var bufferClass     = new org.vertx.java.core.buffer.Buffer().getClass();

// Converts a received message from the java event bus into something
// a little more palatable for javascript
var resultConverter = function(jMsg) {
  var body = jMsg.body();

  // Strings, booleans, numbers, and undefined all
  // get passed across the bus without conversion
  switch(typeof body) {
    case 'string':
    case 'boolean':
    case 'number':
    case 'undefined':
      return body;
    case 'object':
      if (body === null) return undefined;
      break;
  }
    
  // must be some other kind of object - deal with it
  var clazz = body.getClass();
  if (clazz === jsonObjectClass || clazz === jsonArrayClass) {
    // Convert to JS JSON
    body = JSON.parse(body.encode());
  } else if (clazz === bufferClass) {
    // Convert to JS Buffer
    body = new Buffer(body);
  }
  return body;
};

function wrappedHandler(handler) {
  return new org.vertx.java.core.Handler({
    handle: function(jMsg) {
      var body = resultConverter(jMsg);
      var meta = {
        address: jMsg.address(),
        fail: function(code, msg) {
          jMsg.fail(code, msg);
        }
      };
      handler(body, function(reply, replyHandler, timeout) {
        if (typeof reply === 'undefined') {
          throw "Reply message must be specified";
        }
        reply = convertMessage(reply);
        if (replyHandler) {
          if (timeout) {
            jMsg.replyWithTimeout(reply, timeout, helpers.adaptAsyncResultHandler(replyHandler, resultConverter));
          } else {
            jMsg.reply(reply, wrappedHandler(replyHandler));
          }
        } else {
          jMsg.reply(reply);
        }
      }, meta);
    }
  });
}

function registerHandler(address, handler, localOnly, registrationHandler) {
  checkHandlerParams(address, handler);

  var wrapped = wrappedHandler(handler);
  if (typeof localOnly == 'function') {
    registrationHandler = localOnly;
    localOnly = false;
  }

  // This is a bit more complex than it should be because we have to wrap the
  // handler - therefore we have to keep track of it :(
  handlerMap[handler] = wrapped;

  if (localOnly) {
    jEventBus.registerLocalHandler(address, wrapped);
  } else {
    if (typeof registrationHandler == 'undefined') {
      jEventBus.registerHandler(address, wrapped);
    } else {
      jEventBus.registerHandler(address, wrapped, registrationHandler);
    }
  }
  return eventBus;
}

function convertMessage(message) {
  if (message === null || message === undefined) return '';
  var msgType = typeof message;
  switch (msgType) {
    case 'string':
    case 'boolean':
    case 'org.vertx.java.core.json.JsonArray':
    case 'org.vertx.java.core.buffer.Buffer':
      break;
    case 'number':
      message = new java.lang.Double(message);
      break;
    case 'object':
      if (message instanceof Array) {
        message = new org.vertx.java.core.json.JsonArray(message);
      } else if (message instanceof Buffer) {
        message = message._to_java_buffer();
      } else if (typeof message.getClass === "undefined") {
        message = new org.vertx.java.core.json.JsonObject(JSON.stringify(message));
      }
      break;
    default:
      throw 'Invalid type for message: ' + msgType;
  }
  return message;
}

function sendOrPub(send, address, message, replyHandler) {
  if (!address) {
    throw "address must be specified";
  }
  if (typeof address !== "string") {
    throw "address must be a string";
  }
  if (replyHandler && typeof replyHandler !== "function") {
    throw "replyHandler must be a function";
  }
  message = convertMessage(message);
  if (send) {
    if (replyHandler) {
      var wrapped = wrappedHandler(replyHandler);
      jEventBus.send(address, message, wrapped);
    } else {
      jEventBus.send(address, message);
    }
  } else {
    jEventBus.publish(address, message);
  }
  return eventBus;
}

function sendWithTimeout(address, message, replyHandler, timeout) {
  if (!address) {
    throw "address must be specified";
  }
  if (typeof address !== "string") {
    throw "address must be a string";
  }
  if (replyHandler && typeof replyHandler !== "function") {
    throw "replyHandler must be a function";
  }
  jEventBus.sendWithTimeout(address, convertMessage(message), timeout, helpers.adaptAsyncResultHandler(replyHandler, resultConverter));
  return eventBus;
}

module.exports = eventBus;

