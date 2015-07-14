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
 * The <code>vertx/net</code> module provides network client and server
 * functions and classes.
 * @exports vertx/net
 */
var net = {};
var streams = require('vertx/streams');
var tcp_support = require('vertx/tcp_support');
var ssl_support = require('vertx/ssl_support');
var helpers = require("vertx/helpers.js");
var Buffer = require('vertx/buffer');


/**
 * Creates a {@linkcode module:vertx/net.NetServer|NetServer}.
 * @returns {module:vertx/net.NetServer} A new server instance
 */
net.createNetServer = function() {
  return new net.NetServer();
};

/**
 * Creates a {@linkcode module:vertx/net.NetClient|NetClient}.
 * @returns {module:vertx/net.NetClient} A new client instance
 */
net.createNetClient = function() {
  return new net.NetClient();
};

/**
 * A <code>ListenHandler</code> is a {@linkcode Handler} that is
 * notified when a call to {@linkcode module:vertx/net.NetServer#listen}
 * has been initiated and the underlying socket has been bound.
 * @typedef {function} ListenHandler
 */

/**
 * A <code>ConnectHandler</code> is a {@linkcode Handler} that accepts a
 * {@linkcode module:vertx/net.NetSocket} as it's parameter.
 * @typedef {function} ConnectHandler
 * @param {module:vertx/net.NetSocket} netSocket The raw socket
 */

/**
 * Represents a TCP or SSL server.  
 * @constructor
 * @augments module:vertx/ssl_support~SSLSupport
 * @augments module:vertx/ssl_support~ServerSSLSupport
 * @augments module:vertx/tcp_support~TCPSupport
 * @augments module:vertx/tcp_support~ServerTCPSupport
 */
net.NetServer = function() {
  var that = this;
  var jserver = __jvertx.createNetServer();

  tcp_support.TCPSupport.call(this, jserver);
  tcp_support.ServerTCPSupport.call(this, jserver);

  ssl_support.SSLSupport.call(this, jserver);
  ssl_support.ServerSSLSupport.call(this, jserver);

  /**
   * Supply a connect handler for this server. The server can only have at most
   * one connect handler at any one time.  As the server accepts TCP or SSL
   * connections it creates an instance of {@linkcode module:vertx/net.NetSocket}
   * and passes it to the connect handler.
   *
   * @param {ConnectHandler} handler the connection handler
   * @returns {module:vertx/net.NetServer} this
   */
  this.connectHandler = function(handler) {
    jserver.connectHandler(function(result) {
      handler(new net.NetSocket(result));
    });
    return that;
  };

  /**
   * Tell the server to start listening on all available interfaces and
   * <code>port</code>. Be aware this is an async operation and the server may
   * not bound on return of the method.
   *
   * @param {number} port The network port to listen on
   * @param {string} [host] The hostname or IP address to listen on
   * @param {ListenHandler} [handler] The listen handler
   * @returns {module:vertx/net.NetServer} this
   */
  this.listen = function() {
    var args = Array.prototype.slice.call(arguments);
    var handler = helpers.getArgValue('function', args);
    var host = helpers.getArgValue('string', args);
    var port = helpers.getArgValue('number', args);
    if (handler !== null) {
      handler = helpers.adaptAsyncResultHandler(handler);
    }
    if (host === null) {
      host = "0.0.0.0";
    }
    jserver.listen(port, host, handler);
    return that;
  };

  /**
   * Close the server. This will close any open connections.
   * @param {Handler} [handler] the handler to call when the close operation has completed
   */
  this.close = function(handler) {
    if (handler === undefined) {
      jserver.close();
    } else {
      jserver.close(helpers.adaptAsyncResultHandler(handler));
    }
  };

  /**
   * The actual port the server is listening on. This is useful if you bound
   * the server specifying 0 as port number signifying an ephemeral port.
   * @returns {number} The port number the server is listening on
   */
  this.port = function() {
    return jserver.port();
  };

  /**
   * The host name
   * @returns {string} the host
   */
  this.host = function() {
    return jserver.host();
  };
};

/**
 * <p>
 * A TCP/SSL client.
 * </p>
 * <p>
 * Multiple connections to different servers can be made using the same
 * instance.  This client supports a configurable number of connection attempts
 * and a configurable delay between attempts.
 * </p>
 *
 * @constructor
 * @augments module:vertx/ssl_support~SSLSupport
 * @augments module:vertx/ssl_support~ClientSSLSupport
 * @augments module:vertx/tcp_support~TCPSupport
 */
net.NetClient = function() {
  var jclient = __jvertx.createNetClient();
  var that = this;
  tcp_support.TCPSupport.call(this, jclient);

  ssl_support.SSLSupport.call(this, jclient);
  ssl_support.ClientSSLSupport.call(this, jclient);

  /**
   * Attempt to open a connection to a server at the specific port and host.
   * The connect is done asynchronously and on success, a
   * {@linkcode module:vertx/net.NetSocket} instance is supplied via the
   * {@linkcode ConnectHandler} instance 
   *
   * @param {number} port The port to connect on
   * @param {string} [host] The hostname or IP address to connect to (default: localhost)
   * @param {ConnectHandler} [connectHandler] The handler receiving the connection
   * @return {module:vertx/net.NetClient} this
   */
  this.connect = function(arg0, arg1, arg2) {
    var port = arg0;
    var host;
    var handler;
    if (arg2 === undefined) {
      host = 'localhost';
      handler = arg1;
    } else {
      host = arg1;
      handler = arg2;
    }
    jclient.connect(port, host, helpers.adaptAsyncResultHandler(handler, function(result) {
      return new net.NetSocket(result);
    }));
    return that;
  };

  /**
   * Get or set the number of reconnection attempts. Default value is zero.
   * @param {number} [attempts] the number of reconnection attempts to make before failing
   * @returns {number|module:vertx/net.NetClient}
   */
  this.reconnectAttempts = function(attempts) {
    if (attempts === undefined) {
      return jclient.getReconnectAttempts();
    } else {
      jclient.setReconnectAttempts(attempts);
      return that;
    }
  };

  /**
   * Get or set the reconnection interval, in milliseconds.
   * @param {number} [interval] the number of milliseconds to wait before attempting a reconnection
   * @returns {number|module:vertx/net.NetClient}
   */
  this.reconnectInterval = function(interval) {
    if (interval === undefined) {
      return jclient.getReconnectInterval();
    } else {
      jclient.setReconnectInterval(interval);
      return that;
    }
  };
  
  /**
   * Get or set the connect timeout, in milliseconds.
   * @param {number} [timeout] the number of milliseconds to wait before timing out a connection
   * @returns {number|module:vertx/net.NetClient}
   */
  this.connectTimeout = function(timeout) {
    if (timeout === undefined) {
      return jclient.getConnectTimeout();
    } else {
      jclient.setConnectTimeout(timeout);
      return that;
    }
  };
  
  /**
   * Close the client.
   */
  this.close = function() {
    jclient.close();
  };
};

/**
 * <p>
 * A socket-like interface to a TCP/SSL connection on either the client or the
 * server side.
 * </p>
 * <p>
 * Instances of this class are created on the client side by a 
 * {@linkcode module:vertx/net.NetClient} when a connection to a server is
 * made, or on the server side by a {@linkcode vertx/net.NetServer}
 * when a server accepts a connection.
 * </p>
 * <p>
 * It implements both {@linkcode ReadStream} and 
 * {@linkcode WriteStream} so it can be used with 
 * {@linkcode module:vertx/pump~Pump} to pump data with flow control.
 * </p>
 * @constructor
 * @augments module:vertx/streams~ReadStream
 * @augments module:vertx/streams~WriteStream
 */
net.NetSocket = function(jNetSocket) {
  var that = this;
  streams.ReadStream.call(this, jNetSocket);
  streams.WriteStream.call(this, jNetSocket);

  /**
   * <p>
   * When a NetSocket is created it automatically registers an event handler
   * with the event bus, the ID of that handler is given by
   * writeHandlerID.
   * </p>
   * <p>
   * Given this ID, a different event loop can send a buffer to that event
   * handler using the event bus and that buffer will be received by this
   * instance in its own event loop and written to the underlying connection.
   * This allows you to write data to other connections which are owned by
   * different event loops.
   * </p>
   */
  this.writeHandlerID = function() {
    return jNetSocket.writeHandlerID();
  };

  /**
   * Write a string or {@linkcode module:vertx/buffer~Buffer} to the socket.
   * @param {string|module:vertx/buffer~Buffer} chunk the data to write
   * @param {string} [encoding] the charset for the data (default: UTF-8)
   * @returns {module:vertx/net.NetSocket} this
   */
  this.write = function(arg0, arg1) {
    if (arg1 === undefined) {
      if (arg0 instanceof Buffer) {
        jNetSocket.write(arg0._to_java_buffer());
      } else {
        jNetSocket.write(arg0);
      }
    } else {
      jNetSocket.write(arg0, arg1);
    }
    return that;
  };

  /**
   * Tell the kernel to stream a file as specified by <code>filename</code>
   * directly from disk to the outgoing connection, bypassing userspace
   * altogether (where supported by the underlying operating system. This is a
   * very efficient way to stream files
   * @param {string} filename The path to the file
   * @param {function} [callback] A function to be called when the send
   * has completed or a failure has occurred.
   * @returns {module:vertx/net.NetSocket} this
   */
  this.sendFile = function(filename, callback) {
    jNetSocket.sendFile(filename, helpers.adaptAsyncResultHandler(callback));
    return that;
  };
  
  /**
   * Returns the remote address for this socket
   * @returns {Address} The remote address
   */
  this.remoteAddress = function() {
    return {
      'ipaddress': jNetSocket.remoteAddress().getAddress().getHostAddress(),
      'port': jNetSocket.remoteAddress().getPort()
    };
  };

  /**
   * Returns the local address for this socket
   * @returns {Address} The local address
   */
  this.localAddress = function() {
    return {
      'ipaddress': jNetSocket.localAddress().getAddress().getHostAddress(),
      'port': jNetSocket.localAddress().getPort()
    };
  };

  /**
   * Close this socket.
   */
  this.close = function() {
    jNetSocket.close();
  };

  /**
   * Set a {@linkcode Handler} to be called when this socket is closed
   * @param {Handler} handler The close handler to use
   */
  this.closeHandler = function(handler) {
    jNetSocket.closeHandler(handler);
    return that;
  };

  /**
   * Upgrade channel to use SSL/TLS. Be aware that for this to work SSL must
   * already be configured.
   *
   * @example
   * <pre><code>
   *     var server = vertx.createNetServer().
   *                        .keyStorePath('/path/to/your/keystore/server-keystore.jks')
   *                        .keyStorePassword('password');
   *     server.connectHandler(function(sock) {
   *       if (authCheck()) {
   *         sock.ssl();
   *       }
   *     }
   * </code></pre>
   *
   * @param {Handler} [handler] Called when the upgrade has been completed
   * @returns {module:vertx/net.NetSocket} this
   */
  this.ssl = function(handler) {
    if (!handler) {
      handler = function() {};
    }
    jNetSocket.ssl(handler);
    return that;
  };

  /**
   * Returns true if this socket is SSL/TLS encrypted.
   */
  this.isSSL = function() {
    return jNetSocket.isSsl();
  };
};

/**
 * @typedef {{}} Address
 * @property {string} ipaddress The IP address
 * @property {number} port The port for this address
 */

module.exports = net;
