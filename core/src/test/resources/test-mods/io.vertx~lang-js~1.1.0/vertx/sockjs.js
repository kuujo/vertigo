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
var Streams  = require('vertx/streams');
var MultiMap = require('vertx/multi_map').MultiMap;
var helpers  = require('vertx/helpers');
var asyncResultHandler = helpers.adaptAsyncResultHandler;

/**
 * <p>
 * SockJS enables browsers to communicate with the server using a simple
 * WebSocket-like api for sending and receiving messages. Under the hood
 * SockJS chooses to use one of several protocols depending on browser
 * capabilities and what apppears to be working across the network.
 * </p>
 *
 * <p>
 * Available protocols include:
 * </p>
 *
 * <ul>
 *   <li>WebSockets</li>
 *   <li>xhr-polling</li>
 *   <li>xhr-streaming</li>
 *   <li>json-polling</li>
 *   <li>event-source</li>
 *   <li>html-file</li>
 * </ul>
 *
 * <p>
 * This means, it should <i>just work</i> irrespective of what browser is being
 * used, and whether there are nasty things like proxies and load balancers
 * between the client and the server.
 * </p>
 *
 * <p>
 * For more detailed information on SockJS, see their website.
 * </p>
 *
 * @exports vertx/sockjs
 */
var sockJS = {};
var JsonObject   = org.vertx.java.core.json.JsonObject;
var JsonArray    = org.vertx.java.core.json.JsonArray;
var EventBusHook = org.vertx.java.core.sockjs.EventBusBridgeHook;

/**
 * Create a new SockJSServer
 * @param {module:vertx/http.HttpServer} httpServer the HTTP server to use
 * @return {module:vertx/sockjs.SockJSServer} the SockJS server instance
 */
sockJS.createSockJSServer = function(httpServer) {
  if (typeof httpServer._to_java_server !== 'function') {
    throw "Please construct a vertx.SockJSServer with an instance of vert.HttpServer";
  }
  return new sockJS.SockJSServer(httpServer);
};

/**
 * <p>
 * This is an implementation of the server side part of 
 * <a href="https://github.com/sockjs">SockJS</a>.
 * </p>
 * <p>
 * You can register multiple applications with the same SockJSServer, each
 * using different path prefixes, each application will have its own handler,
 * and configuration.
 * </p>
 * <p>
 *    Configuration options and their defaults are:
 *    <pre>
 *      - session_timeout: 5000ms
 *      - insert_JSESSIONID: true
 *      - heartbeat_period: 25000ms
 *      - max_bytes_streaming: 131072 (128*1024)
 *      - prefix: "/"
 *      - library_url: "http://cdn.sockjs.org/sockjs-0.3.4.min.js"
 *      - disabled_transports: []
 *    </pre>
 *  </p>
 *
 *
 * @constructor
 */
sockJS.SockJSServer = function(httpServer) {
  var jserver = __jvertx.createSockJSServer(httpServer._to_java_server());
  var hooks   = {};

  /**
   *  Specify a function to call on the given event. All functions take a
   *  SockJSSocket as the first (and perhaps only) parameter. The events are:
   *
   *  <ul>
   *    <li>socket-created - Called when a new socket is created. Use this to
   *        do things like check the origin header on the socket before accepting it.
   *        The function will be provided with a {module:vertx/sockjs.SockJSSocket}.
   *        Returning <code>true</code> will allow the socket to be accepted,
   *        <code>false</code> will cause it to be rejected.
   *    </li>
   *    <li>pre-register - Called when a socket registers a handler on the event bus.
   *        The function will be provided with a {module:vertx/sockjs.SockJSSocket}
   *        and the address as a {string}. Return true to let the registration occur,
   *        or false otherwise.
   *    </li>
   *    <li>post-register - Called after a socket registers a handler on the event bus.
   *        do things like check the origin header on the socket before accepting it.
   *        The function will be provided with a {module:vertx/sockjs.SockJSSocket}
   *        and the address as a {string}. 
   *    </li>
   *    <li>send-or-pub - Called when the client is sending or publishing on the socket.
   *        The function will be provided with a {module:vertx/sockjs.SockJSSocket};
   *        a {boolean} that, if true means the client is sending on the socket, or
   *        if false the client is publishing on the socket; a {JSON} object that is
   *        the body of the message; and finally the address as a {string}. Return
   *        <code>true</code> to allow the message to be published/sent, false otherwise.
   *    </li>
   *    <li>socket-closed - Called when the client socket has closed.
   *        The function will be provided with the {module:vertx/sockjs.SockJSSocket}.
   *    </li>
   *    <li>authorize - Called on client authorization.</li>
   *  </ul>
   * @param {string} evt The name of the event
   * @param {function} func The function to call when the event occurs
   */
  this.on = function(evt, func) {
    hooks[evt] = func;
  };

  /**
   * Install a SockJS application.
   * @param {JSON} config The application configuration
   * @param {SockJSHandler} handler The handler that will be called when SockJS sockets are created
   * @return {module:vertx/sockjs.SockJSServer} this
   */
  this.installApp = function(config, handler) {
    jserver.installApp(new JsonObject(JSON.stringify(config)), sockJSHandler(handler));
  };

  /**
   * Install an app which bridges the SockJS server to the event bus
   * @param {JSON} config The application configuration
   * @param {Array} inboundPermitted A list of JSON objects which define
   *                permitted matches for inbound (client->server) traffic 
   * @param {Array} outboundPermitted A list of JSON objects which define 
   *                permitted matches for outbound (server->client) traffic
   * @param {JSON} bridgeConfig A JSON object containing the configuration for
   *   the event bus bridge. Configuration options and their defaults are:
   *    <pre>
   *      auth_address: "vertx.basicauthmanager.authorise"
   *      auth_timeout: 300000ms
   *      ping_interval: 10000ms
   *      max_address_length: 200
   *      max_handlers_per_socket: 1000
   *    </pre>
   */
  this.bridge = function(config, inboundPermitted, outboundPermitted, bridgeConfig) {
    var jInboundPermitted = convertPermitted(inboundPermitted);
    var jOutboundPermitted = convertPermitted(outboundPermitted);

    jserver.setHook( new EventBusHook({
      handleSendOrPub    : lookup('send-or-pub',    true),
      handleSocketCreated: lookup('socket-created', true),
      handlePreRegister  : lookup('pre-register',   true),
      handleUnRegister   : lookup('unregister',     true),
      handleAuthorise    : lookup('authorize',      true),
      handlePostRegister : lookup('post-register'),
      handleSocketClosed : lookup('socket-closed')
    }));
    if (bridgeConfig) {
      jserver.bridge(new JsonObject(JSON.stringify(config)),
          jInboundPermitted, jOutboundPermitted, bridgeConfig);
    } else {
      jserver.bridge(new JsonObject(JSON.stringify(config)),
          jInboundPermitted, jOutboundPermitted);
    }
  };

  function convertPermitted(permitted) {
    var json_arr = new JsonArray();
    for (var i = 0; i < permitted.length; i++) {
      var match = permitted[i];
      var json_str = JSON.stringify(match);
      var jJson = new JsonObject(json_str);
      json_arr.add(jJson);
    }
    return json_arr;
  }

  function lookup(evt, defaultReturn) {
    return function( /* arguments */ ) {
      if (hooks[evt]) {
        var args = Array.prototype.slice.call(arguments);
        if (evt === 'authorize') {
          // authorize events have an async result handler as
          // their final argument. Convert it accordingly.
          args[args.length-1] = asyncResultHandler(args.slice(-1)[0]);
        } else {
          // all other events have a SockJSSocket as their first argument
          args[0] = new sockJS.SockJSSocket(args[0]);
        }
        return hooks[evt].call(hooks[evt], args);
      }
      return defaultReturn;
    };
  }

  function sockJSHandler(func) {
    return function(sock) {
      func.call(func, new sockJS.SockJSSocket(sock));
    };
  }
};

/**
 * A <code>SockJSHandler</code> is a {@linkcode Handler} that is called when
 * new SockJSSockets are created. It takes a {module:vertx/sockjs.SockJSSocket}
 * as its only parameter.
 *
 * @typedef {function} SockJSHandler
 * @param {module:vertx/sockjs.SockJSSocket} sockJSSocket The socket object
 */

/**
 * <p>Represents a SockJS socket.  You interact with SockJS clients through
 * instances of a SockJS socket.
 * The API is very similar to {@linkcode module:vertx/http.WebSocket}.</p>
 * <p>Instances of this class are created and provided to a {@linkcode SockJSHandler}.</p>
 * <p>It implements both {@linkcode ReadStream} and {@linkcode WriteStream} so
 * it can be used with {@linkcode module:vertx/Pump~Pump|Pump} to pump data
 * with flow control.</p>
 *
 * @constructor
 *
 * @param {org.vertx.java.core.sockjs.SockJSSocket} delegate The java SockJSSocket object
 * @see SockJSHandler
 * @augments module:vertx/streams~ReadStream
 * @augments module:vertx/streams~WriteStream
 */
sockJS.SockJSSocket = function(delegate) {
  /**
   * <p>
   * When a {@code SockJSSocket} is created it automatically registers an event
   * handler with the event bus, the ID of that handler is given by 
   * {@linkcode writeHandlerID}.
   * </p>
   * <p>
   * Given this ID, a different event loop can send a buffer to that event
   * handler using the event bus and that buffer will be received by this
   * instance in its own event loop and written to the underlying socket. This
   * allows you to write data to other sockets which are owned by different
   * event loops.
   * </p>
   * @return {string} the ID
   */
  this.writeHandlerID = function() {
    return delegate.writeHandlerID();
  };

  /**
   * Close the socket
   */
  this.close = function() {
    delegate.close();
  };

  /**
   * Get the local address for this socket
   * @return {} The address of the local socket
   */
  this.localAddress = function() {
    return helpers.convertInetSocketAddress(delegate.getLocalAddress());
  };

  /**
   * @return {module:vertx/multi_map~MultiMap} The headers map
   */
  this.headers = function() {
    if (_headers === null) {
      _headers = new MultiMap(delegate.headers());
    }
    return _headers;
  };

  /**
   * Return the URI corresponding to the last request for this socket or the
   * websocket handshake
   * @return {string} The URI string
   */
  this.uri = function() {
    return delegate.uri();
  };

  var _headers = null;
  Streams.WriteStream.call(this, delegate);
  Streams.ReadStream.call(this, delegate);
};


module.exports = sockJS;

