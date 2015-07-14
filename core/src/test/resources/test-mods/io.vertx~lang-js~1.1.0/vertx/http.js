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
 * The http module provides HTTP functions. 
 *
 * @exports vertx/http
 */
var http = {};
var net = require('vertx/net');
var MultiMap = require('vertx/multi_map').MultiMap;
var streams = require('vertx/streams');
var tcp_support = require('vertx/tcp_support');
var ssl_support = require('vertx/ssl_support');
var helpers = require("vertx/helpers.js");
var Buffer = require('vertx/buffer');

/**
 * A <code>BodyHandler</code> is a {@linkcode Handler} that accepts a
 * {@linkcode module:vertx/buffer~Buffer|Buffer} as it's parameter.
 * @typedef {function} BodyHandler
 * @param {module:vertx/buffer~Buffer} buffer A Buffer object containing the body of the message
 */

/**
 * Create and return a {@linkcode module:vertx/http.HttpServer}
 *
 * @example
 * var http = require('vertx/http');
 * var server = http.createHttpServer();
 *
 * // setup request handlers and such...
 * server.listen(8080, 'localhost');
 *
 * @return {module:vertx/http.HttpServer} the newly created server
 */
http.createHttpServer = function() {
  return new http.HttpServer();
};


/**
 * Create and return a {@linkcode module:vertx/http.HttpClient}
 *
 * @returns {module:vertx/http.HttpClient}
 */
http.createHttpClient = function() {
  return new http.HttpClient();
};

/**
 * Represents a server-side HttpServerRequest object. This object is created internally
 * by vert.x and passed as a parameter to a request listener. It should not be directly
 * created.
 *
 * @example
 * var http    = require('vertx/http');
 * var console = require('vertx/console');
 *
 * var server = http.createHttpServer();
 * server.requestHandler(function(request) {
 *
 *   // Get headers from the HttpServerRequest object
 *   // and write them to the console
 *   for (var k in request.headers()) {
 *     console.log(k + ": " + headers[k]);
 *   }
 * 
 *   request.response.end(str);
 * 
 * }).listen(8080, 'localhost');
 *
 * @class 
 * @param {org.vertx.java.core.http.HttpServerRequest} request the underlying
 * Java HttpServerRequest object
 * @augments module:vertx/streams~ReadStream
 */
http.HttpServerRequest = function(jreq) {
  var reqHeaders   = null;
  var reqParams    = null;
  var version      = null;
  var reqFormAttrs = null;
  var netSocket    = null;
  var that         = this;

  /**
   * The HTTP version - either HTTP_1_0 or HTTP_1_1
   *
   * @returns {string} version
   */
  this.version = function() {
    if (version === null) {
      version = jreq.version().toString();
    }
    return version;
  };

  /**
   *
   * Get the NetSocket. Primarily for internal use, but if you really must
   * roll your own websockets or some such, this will let you do that.
   * @returns {module:vertx/net.NetSocket} The raw <code>NetSocket</code>.
   */
  this.netSocket = function() {
    if (netSocket === null) {
      netSocket = new net.NetSocket(jreq.netSocket());
    }
    return netSocket;
  };

  /**
   * The HTTP method, one of HEAD, OPTIONS, GET, POST, PUT, DELETE, CONNECT, TRACE
   *
   * @returns {string} The HTTP method
   */
  this.method = function() {
    return jreq.method();
  };

  /**
   * The uri of the request. For example
   * http://www.somedomain.com/path/morepath/resource.foo?param=32&otherparam=x
   *
   * @returns {string} uri
   */
  this.uri = function() {
    return jreq.uri();
  };

  /**
   * The path part of the uri. For example /path/morepath/resource.foo
   *
   * @returns {string} path
   */
  this.path = function() {
    return jreq.path();
  };

  /**
   * The query part of the uri. For example param=32&otherparam=x
   *
   * @returns {string} query
   */
  this.query = function() {
    return jreq.query();
  };

  /**
   * The headers of the request.
   *
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.headers = function() {
    if (!reqHeaders) {
      reqHeaders = new MultiMap(jreq.headers());
    }
    return reqHeaders;
  };

  /**
   * Return the remote (client side) address of the request
   *
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.params = function() {
    if (!reqParams) {
      reqParams = new MultiMap(jreq.params());
    }
    return reqParams;
  };

  /**
   * Get the address of the remote peer. An address object contains
   * port and address properties.
   *
   * @return {} the remote address
   */
  this.remoteAddress = function() {
    return helpers.convertInetSocketAddress(jreq.remoteAddress());
  };


  /**
   * Get the address of the server. An address object contains port
   * and address properties.
   *
   * @return {} the local address.
   */
  this.localAddress = function() {
    return helpers.convertInetSocketAddress(jreq.localAddress());
  };

  /**
   * Get an array of Java X509Certificate objects
   *
   * @return {Array} Array of Java X509Certificate objects
   */
  this.peerCertificateChain = function() {
    return jreq.peerCertificateChain();
  };

  /**
   * Return the absolute URI corresponding to the the HTTP request
   *
   * @returns {string} absoluteURI
   */
  this.absoluteURI = function() {
    return jreq.absoluteURI();
  };

  /**
   * Inform the server that we are expecting a multi-part form.
   * You _must_ call this function _before_ receiving the request
   * body if you expect it to contain a multi-part form.
   *
   * @param {boolean} expect Whether to expect a multi-part form
   * @returns {module:vertx/http.HttpServerRequest} this
   */
  this.expectMultiPart = function(expect) {
    jreq.expectMultiPart(expect);
    return that;
  };

  /**
   * Return a form attributes object
   *
   * @returns {module:vertx/multi_map~MultiMap} The form attributes
   */
  this.formAttributes = function() {
    if (!reqFormAttrs) {
      reqFormAttrs =  new MultiMap(jreq.formAttributes());
    }
    return reqFormAttrs;
  };

  /**
   * Set the upload handler. The handler will get notified once a new file
   * upload was received and so allow to get notified by the upload in
   * progress.
   *
   * @param {UploadHandler} handler The handler to call
   * @returns {module:vertx/http.HttpServerRequest} this
   */
  this.uploadHandler = function(handler) {
    if (handler) {
      jreq.uploadHandler(wrapUploadHandler(handler));
    }
    return that;
  };

  /**
   *  Set the body handler for this request, the handler receives a single
   *  Buffer object as a parameter.  This can be used as a decorator.
   *
   * @param {BodyHandler} handler The handler to call once the body was received
   * @returns {module:vertx/http.HttpServerRequest} this
   */
  this.bodyHandler = function(handler) {
    jreq.bodyHandler(handler);
    return that;
  };

  var jresp = jreq.response();

  /**
   * @property {module:vertx/http.HttpServerResponse} response A response object
   * that can be used to send a response to this request.
   */
  this.response = new http.HttpServerResponse(jresp);

  /**
   * @private
   */
  this._to_java_request = function() {
    return jreq;
  };

  streams.ReadStream.call(this, jreq);

};

/**
 * <p>
 * A server-side HTTP response.
 * An instance of this class is created and associated to every instance of
 * {@linkcode module:vertx/http.HttpServerRequest} that is created.
 * </p>
 *
 * <p>
 * It allows the developer to control the HTTP response that is sent back to
 * the client for a partcular HTTP request. It contains methods that allow HTTP
 * headers and trailers to be set, and for a body to be written out to the
 * response.  
 * </p>
 *
 * @class
 * @param {org.vertx.java.core.http.HttpServerResponse} jresp the underlying java proxy
 * @augments module:vertx/streams~WriteStream
 */
http.HttpServerResponse = function(jresp) {
  var that = this;
  var respHeaders = null;
  var respTrailers = null;

  /**
   * Get or set HTTP status code of the response.
   * @param {number} [code] The HTTP status code, e.g. 200
   * @returns {number|module:vertx/http.HttpServerResponse} If a status code is supplied, this method
   * sets it and returns itself. If a status code is not provided, return the current
   * status code for this response.
   */
  this.statusCode = function(code) {
    if (code) {
      jresp.setStatusCode(code);
      return that;
    } else {
      return jresp.getStatusCode();
    }
  };

  /**
   * Get or set HTTP status message of the response.
   * @param {string} [message] The HTTP status message.
   * @returns {string|module:vertx/http.HttpServerResponse} 
   */
  this.statusMessage = function(msg) {
    if (msg) {
      jresp.setStatusMessage(msg);
      return that;
    } else {
      return jresp.getStatusMessage();
    }
  };

  /**
   * Get or set if the response is chunked
   * @param {boolean} [chunked] Whether or not the response will be chunked encoding
   * @returns {boolean|module:vertx/http.HttpServerResponse}
   */
  this.chunked = function(ch) {
    if (ch) {
      jresp.setChunked(ch);
      return that;
    } else {
      return jresp.isChunked();
    }
  };

  /**
   * Return the http headers of the response
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.headers = function() {
    if (!respHeaders) {
      respHeaders = new MultiMap(jresp.headers());
    }
    return respHeaders;
  };

  /**
   * Put a header on the response.
   *
   * @param {string} headerName The name under which the header should be stored
   * @param {string} headerValue T the value of the header
   * @returns {module:vertx/http.HttpServerResponse}
   */
  this.putHeader = function(k, v) {
    jresp.putHeader(k, v);
    return that;
  };

  /**
   * Return the trailing headers of the response
   *
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.trailers = function() {
    if (!respTrailers) {
      respTrailers = new MultiMap(jresp.trailers());
    }
    return respTrailers;
  };

  /**
   * Put a trailing header
   *
   * @param {string} trailerName The name under which the header should be stored
   * @param {string} trailerValue The value of the trailer
   * @returns {module:vertx/http.HttpServerResponse}
   */
  this.putTrailer = function(k, v) {
    jresp.putTrailer(k, v);
    return that;
  };

  /**
   * Write content to the response
   *
   * @param {string} body The body of the response.
   * @param {string} [encoding] The character encoding, defaults to UTF-8
   * @returns {module:vertx/http.HttpServerResponse} Returns self
   */
  this.write = function(body, encoding) {
    if (encoding === undefined) {
      jresp.write(body);
    } else {
      jresp.write(body, encoding);
    }
    return that;
  };

  /**
   * Forces the head of the request to be written before end is called on the
   * request. This is normally used to implement HTTP 100-continue handling,
   * see continue_handler for more information.
   *
   * @returns {module:vertx/http.HttpServerResponse}
   */
  this.sendHead = function() {
    jresp.sendHead();
    return that;
  };

  /**
   * <p>
   * Ends the response. If no data has been written to the response body,
   * the actual response won't get written until this method gets called.
   * </p>
   * <p>
   * Once the response has ended, it cannot be used any more.
   * </p>
   *
   * @param {string} [chunk] a string to write to the data stream before closing
   * @param {string} [encoding] the encoding to use for the write (default is UTF-8)
   */
  this.end = function(arg0, arg1) {
    if (arg0) {
      if (arg1) {
        jresp.end(arg0, arg1);
      } else {
        jresp.end(arg0);
      }
    } else {
      jresp.end();
    }
  };

  /**
   * Tell the kernel to stream a file directly from disk to the outgoing
   * connection, bypassing userspace altogether (where supported by the
   * underlying operating system. This is a very efficient way to serve
   * files.
   *
   * @param {string} fileName Path to file to send.
   * @param {string} [notFoundFile] Path to a file to send if <code>fileName</code> can't be found.
   * @param {ResultHandler} [handler] Function to be called when send has completed (or failed).
   * @returns {module:vertx/http.HttpServerResponse}
   */
  this.sendFile = function(fileName /* arguments */) {
    var args = Array.prototype.slice.call(arguments, 1);
    var notFound = args[0];
    var handler  = args[1];
    if (typeof notFound === 'undefined') {
      notFound = null;
    } else if (typeof notFound === 'function') { 
      handler = notFound;
      notFound = null;
    }
    handler = helpers.adaptAsyncResultHandler(handler);
    jresp.sendFile(fileName, notFound, handler);
    return that;
  };

  streams.WriteStream.call(that, jresp);
};

/**
 * Represents an upload from an HTML form. Created internally and provided to upload
 * handlers. Instances of this class should not be created externally.
 *
 * @constructor
 * @param {org.vertx.java.core.http.HttpServerFileUpload} jupload the underlying java proxy object
 * @see UploadHandler
 * @augments module:vertx/streams~ReadStream
 */
http.HttpServerFileUpload = function(jupload) {
  /**
   * Stream the upload to the given file
   *
   * @param {string} filename The file to which it wil be streamed
   * @returns {http.HttpServerFileUpload}
   */
  this.streamToFileSystem = function(filename) {
    jupload.streamToFileSystem(filename);
    return this;
  };

  /**
   * The filename of the upload
   *
   * @returns {string} filenmae
   */
  this.filename = function() {
    return jupload.filename();
  };

  /**
   * The name of the upload
   *
   * @returns {string} name
   */
  this.name = function() {
    return jupload.name();
  };

  /**
   * The content type
   *
   * @returns {string} contentType
   */
  this.contentType = function() {
    return jupload.contentType();
  };

  /**
   * The content transfer encoding
   *
   * @returns {string} contentTransferEncoding
   */
  this.contentTransferEncoding = function() {
    return jupload.contentTransferEncoding();
  };

  /**
   * The charset
   *
   * @returns {string} charset
   */
  this.charset = function() {
    return jupload.charset().toString();
  };

  /**
   * The size
   *
   * @returns {number} size
   */
  this.size = function() {
    return jupload.size();
  };

  streams.ReadStream.call(this, jupload);
};

/**
 * An <code>UploadHandler</code> is a {@linkcode Handler} that accepts a
 * {@linkcode module:vertx/http.HttpServerFileUpload|HttpServerFileUpload} 
 * object as it's parameter. This allows server code to handle and process
 * uploaded files from HTTP clients.
 * @typedef {function} UploadHandler
 * @param {module:vertx/http.HttpServerFileUpload} upload The file being uploaded
 */
var wrapUploadHandler = function(handler) {
  return function(jupload) {
    handler(new http.HttpServerFileUpload(jupload));
  };
};

/**
 * <p>Represents an HTML 5 Websocket</p>
 * <p>Instances of this class are created and provided to the handler of an
 * {@linkcode HttpClient} when a successful websocket connect attempt occurs.</p>
 * <p>It implements both {@linkcode ReadStream} and {@linkcode WriteStream} so
 * it can be used with {@linkcode module:vertx/Pump~Pump|Pump} to pump data
 * with flow control.</p>
 *
 * @constructor
 *
 * @param {org.vertx.java.core.http.WebSocketBase} jWebSocket The java WebSocketBase object
 * @param {boolean} [server] whether this is a server-side websocket (default: false)
 * @see WebSocketHandler
 * @augments module:vertx/streams~ReadStream
 * @augments module:vertx/streams~WriteStream
 */
http.WebSocket = function(jwebsocket, server) {
  var headers = null;

  /**
   * When a WebSocket is created it automatically registers an event handler
   * with the eventbus, the ID of that handler is returned.
   *
   * Given this ID, a different event loop can send a binary frame to that
   * event handler using the event bus and that buffer will be received by
   * this instance in its own event loop and written to the underlying
   * connection. This allows you to write data to other websockets which are
   * owned by different event loops.
   *
   * @returns {string} id
   */
  this.binaryHandlerID = function() {
    return jwebsocket.binaryHandlerID();
  };

  /**
   * When a WebSocket is created it automatically registers an event handler
   * with the eventbus, the ID of that handler is returned.
   *
   * Given this ID, a different event loop can send a text frame to that
   * event handler using the event bus and that buffer will be received by
   * this instance in its own event loop and written to the underlying
   * connection. This allows you to write data to other websockets which are
   * owned by different event loops.
   *
   * @returns {string} id
   */
  this.textHandlerID = function() {
    return jwebsocket.textHandlerID();
  };

  /**
   *  Write data to the websocket as a binary frame
   *
   * @param {module:vertx/buffer~Buffer} data
   */
  this.writeBinaryFrame = function(data) {
    jwebsocket.writeBinaryFrame(data._to_java_buffer());
  };

  /**
   *  Write data to the websocket as a text frame
   *
   * @param {module:vertx/buffer~Buffer} data
   */
  this.writeTextFrame = function(data) {
    jwebsocket.writeTextFrame(data);
  };

  /**
   * Set a closed {@linkcode Handler} on the connection, the handler receives
   * no parameters.
   *
   * @param {Handler} handler The handler to call when the underlying connection has been closed
   * @returns {WebSocket} this
   */
  this.closeHandler = function(handler) {
    jwebsocket.closeHandler(handler);
    return this;
  };

  /**
   * Close the websocket connection
   */
  this.close = function() {
    jwebsocket.close();
  };

  if (server) {
    /**
     * The path the websocket connect was attempted at.
     * Only available if this is a server websocket.
     *
     * @returns {string} path
     */
    this.path = function() {
      return jwebsocket.path();
    };

    /**
     * Reject the WebSocket. Sends 404 to client
     * Only available if this is a server websocket.
     *
     * @returns {module:vertx/http.WebSocket}
     */
    this.reject = function() {
      jwebsocket.reject();
      return ws;
    };
    /**
     * The headers of the handshake request
     * Only available if this is a server websocket.
     *
     * @returns {module:vertx/multi_map~MultiMap}
     */
    this.headers = function() {
      if (!headers) {
        headers = new MultiMap(jwebsocket.headers());
      }
      return headers;
    };

    /**
     * The URI the websocket handshake occured at
     * @returns {string}
     */
    this.uri = function() {
      return jwebsocket.uri();
    };
  }
  streams.WriteStream.call(this, jwebsocket);
  streams.ReadStream.call(this, jwebsocket);
};

/**
 * An HTTP and websockets server. Created by calling 
 * {@linkcode module:vertx/http.createHttpServer}.
 *
 * @example
 * var http = require('vertx/http');
 * var server = http.createHttpServer();
 *
 * server.requestHandler( function(request) {
 *    // handle the incoming request
 * }
 *
 * server.listen(8000, 'localhost');
 *
 * @class
 * @augments module:vertx/tcp_support~TCPSupport
 * @augments module:vertx/tcp_support~ServerTCPSupport
 * @augments module:vertx/ssl_support~SSLSupport
 * @augments module:vertx/ssl_support~ServerSSLSupport
 */
http.HttpServer = function() {
  var that = this;
  var jserver = __jvertx.createHttpServer();

  tcp_support.TCPSupport.call(this, jserver);
  tcp_support.ServerTCPSupport.call(this, jserver);

  ssl_support.SSLSupport.call(this, jserver);
  ssl_support.ServerSSLSupport.call(this, jserver);

  /**
   * Set the request handler for the server. As HTTP requests are received by
   * the server, instances of 
   * {@linkcode module:vertx/http.HttpServerRequest|HttpServerRequest} will be created
   * and passed to this handler.
   *
   * @param {RequestHandler} handler the function used to handle the request.
   * @return {module:vertx/http.HttpServer}
   */
  this.requestHandler = function(handler) {
    if (handler) {
      if (typeof handler === 'function') {
        handler = wrappedRequestHandler(handler);
      } else {
        // It's a route matcher
        handler = handler._to_java_handler();
      }
      jserver.requestHandler(handler);
    }
    return that;
  };

  /**
   * Set the websocket handler for the server. If a websocket connect
   * handshake is successful a new 
   * {@linkcode module:vertx/http.WebSocket|WebSocket} instance will be created
   * and passed to the handler.
   * 
   * @param {WebSocketHandler} handler the function used to handle the request.
   * @return {module:vertx/http.HttpServer}
   */
  this.websocketHandler = function(handler) {
    if (handler) {
      jserver.websocketHandler(wrappedWebsocketHandler(true, handler));
    }
    return that;
  };

  /**
   * Set or get whether the server should compress the http response if the
   * connected client supports it.
   * @param {boolean} [supported] whether compression should be supported when possible
   * @return {boolean|module:vertx/http.HttpServer} the current server configuration or self
   */
  this.compressionSupported = function(bool) {
    if (bool) {
      jserver.setCompressionSupported(bool);
      return that;
    }
    return jserver.isCompressionSupported();
  };

  /**
   * Set or get the maximum frame size for websocket connections exposed
   * over the SockJS bridge with this HTTPServer
   * @param {number} [size] The frame size in bytes
   */
  this.maxWebSocketFrameSize = function(size) {
    if (size) {
      jserver.setMaxWebSocketFrameSize(size);
      return that;
    }
    return jserver.getMaxWebSocketFrameSize();
  };

  /**
   * Close the server. If a handler is supplied, it will be called
   * when the underlying connection has completed closing.
   *
   * @param {Handler} [handler] The handler to notify when close() completes
   */
  this.close = function(handler) {
    if (handler) {
      jserver.close(handler);
    } else {
      jserver.close();
    }
  };

  /**
   * Start to listen for incoming HTTP requests
   *
   * @param {number} port The port to listen on
   * @param {string} [host] The host name or IP address
   * @param {Handler} [listenHandler] A handler to be notified when the underlying
   *        system level listen() call has completed.
   * @returns {module:vertx/http.HttpServer}
   */
  this.listen = function() {
    var args = Array.prototype.slice.call(arguments);
    var handler = helpers.getArgValue('function', args);
    var host = helpers.getArgValue('string', args);
    var port = helpers.getArgValue('number', args);
    if (handler) {
      handler = helpers.adaptAsyncResultHandler(handler);
    }
    if (host === null) {
      host = "0.0.0.0";
    }
    jserver.listen(port, host, handler);
    return that;
  };

  /**
   * @private
   */
  this._to_java_server = function() {
    return jserver;
  };
};


/**
 * <p>An HTTP client that maintains a pool of connections to a specific host, at a
 * specific port. The client supports pipelining of requests.</p>
 * <p>As well as HTTP requests, the client can act as a factory for 
 * HTML5 {@linkcode module:vertx/http.WebSocket|websockets}.</p>
 *
 * @class
 * @augments module:vertx/tcp_support~TCPSupport
 * @augments module:vertx/ssl_support~SSLSupport
 * @augments module:vertx/ssl_support~ClientSSLSupport
 */
http.HttpClient = function() {
  var that = this;
  var jclient = __jvertx.createHttpClient();

  tcp_support.TCPSupport.call(this, jclient);

  ssl_support.SSLSupport.call(this, jclient);
  ssl_support.ClientSSLSupport.call(this, jclient);
  /**
   * Set the exception handler.
   *
   * @param {Handler} handler The handler which is called on an exception
   * @returns {module:vertx/http.HttpClient}
   */
  this.exceptionHandler = function(handler) {
    jclient.exceptionHandler(handler);
    return that;
  };

  /**
   * Get or set the maxium number of connections this client will pool
   *
   * @param {number} [size] the maximum number of connection
   * @returns {number|module:vertx/http.HttpClient}
   */
  this.maxPoolSize = function(size) {
    if (size === undefined) {
      return jclient.getMaxPoolSize();
    } else {
      jclient.setMaxPoolSize(size);
      return that;
    }
  };

  /**
   * Set or get the maximum frame size for websocket connections exposed
   * over the SockJS bridge with this HTTPClient
   * @param {number} [size] The frame size in bytes
   */
  this.maxWebSocketFrameSize = function(size) {
    if (size) {
      jclient.setMaxWebSocketFrameSize(size);
      return that;
    }
    return jclient.getMaxWebSocketFrameSize();
  };

  /**
   * Set or get whether the client should try to use compression
   * @param {boolean} [shouldTry] Whether the client should try to use compression
   * @return {boolean|module:vertx/http.HttpClient} Whether compression is tried or self
   */
  this.tryUseCompression = function(shouldTry) {
    if (shouldTry) {
      jclient.setTryUseCompression(shouldTry);
      return that;
    }
    return jclient.getTryUseCompression();
  };

  /**
   * <p>
   * Get or set if the client use keep alive. If <code>keepAlive</code> is
   * <code>true</code> then, after the request has ended the connection will be
   * returned to the pool where it can be used by another request. In this
   * manner, many HTTP requests can be pipe-lined over an HTTP connection.
   * Keep alive connections will not be closed until the <code>close()</code>
   * method is invoked.</p>
   * <p>
   * If <code>keepAlive</code> is <code>false</code> then a new connection will
   * be created for each request and it won't ever go in the pool, and the
   * connection will closed after the response has been received. Even with no
   * keep alive, the client will not allow more than <code>getMaxPoolSize()</code>
   * connections to be created at any one time.</p> 
   * <p>
   * If <code>keepAlive</code> is <code>undefined</code> returns the current
   * keep alive status of this client.
   * </p>
   *
   * @param {boolean} [keepAlive]
   * @returns {boolean|module:vertx/http.HttpClient}
   */
  this.keepAlive = function(ka) {
    if (ka === undefined) {
      return jclient.isKeepAlive();
    } else {
      jclient.setKeepAlive(ka);
      return that;
    }
  };

  /**
   * Get or set the port that the client will attempt to connect to on the
   * server on. The default value is 80 
   *
   * @param {number} [port] The port to connect on.
   * @returns {number|module:vertx/http.HttpClient}
   */
  this.port = function(p) {
    if (p === undefined) {
      return jclient.getPort();
    } else {
      jclient.setPort(p);
      return that;
    }
  };

  /**
   *  Get or set the host name or ip address that the client will attempt to
   *  connect to on the server on
   *
   * @param {string} [host] The host name or IP address.
   * @returns {string|module:vertx/http.HttpClient}
   */
  this.host = function(h) {
    if (h === undefined) {
      return jclient.getHost();
    } else {
      jclient.setHost(h);
      return that;
    }
  };

  /**
   * Get or set if the host should be verified.  If set then the client will
   * try to validate the remote server's certificate hostname against the
   * requested host. Should default to 'true'.
   * This method should only be used in SSL mode
   *
   * @param {boolean} verify whether or not to verify hosts 
   * @returns {boolean|module:vertx/http.HttpClient}
   */
  this.verifyHost = function(h) {
    if (h === undefined) {
      return jclient.isVerifyHost();
    } else {
      jclient.setVerifyHost(h);
      return that;
    }
  };

  /**
   * Attempt to connect an HTML5 websocket to the specified URI.
   * The connect is done asynchronously and the handler is called with a WebSocket on success.
   *
   * @param {string} uri A relative URI where to connect the websocket on the host, e.g. /some/path
   * @param {WebSocketHandler} handler The handler to be called with the WebSocket
   */
  this.connectWebsocket = function(uri, handler) {
    jclient.connectWebsocket(uri, wrappedWebsocketHandler(false, handler));
  };

  /**
   * This is a quick version of the get method where you do not want to do anything with the request
   * before sing.
   * With this method the request is immediately sent.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the GET on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.getNow = function(uri, handler) {
    return new http.HttpClientRequest(jclient.getNow(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP OPTIONS request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the OPTIONS on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.options = function(uri, handler) {
    return new http.HttpClientRequest(jclient.options(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP GET request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the GET on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.get =function(uri, handler) {
    return new http.HttpClientRequest(jclient.get(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP HEAD request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the HEAD on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.head =function(uri, handler) {
    return new http.HttpClientRequest(jclient.head(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP POST request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the POST on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.post = function(uri, handler) {
    return new http.HttpClientRequest(jclient.post(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP PUT request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the PUT on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.put = function(uri, handler) {
    return new http.HttpClientRequest(jclient.put(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP DELETE request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the DELETE on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.delete = function(uri, handler) {
    return new http.HttpClientRequest(jclient.delete(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP TRACE request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the TRACE on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.trace = function(uri, handler) {
    return new http.HttpClientRequest(jclient.trace(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP CONNECT request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the CONNECT on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.connect = function(uri, handler) {
    return new http.HttpClientRequest(jclient.connect(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP PATCH request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} uri A relative URI where to perform the PATCH on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.patch = function(uri, handler) {
    return new http.HttpClientRequest(jclient.patch(uri, wrapResponseHandler(handler)));
  };

  /**
   * This method returns an request which represents an HTTP request with the specified uri.
   * When an HTTP response is received from the server the handler is called passing in the response.
   *
   * @param {string} method The HTTP method which is used for the request
   * @param {string} uri A relative URI where to perform the PUT on the server.
   * @param {ResponseHandler} handler The handler to be called
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.request = function(method, uri, handler) {
    return new http.HttpClientRequest(jclient.request(method, uri, wrapResponseHandler(handler)));
  };

  /**
   * Close the client
   */
  this.close = function() {
    jclient.close();
  };

  /**
   * A <code>ResponseHandler</code> is a {@linkcode Handler} that accepts a
   * {@linkcode module:vertx/http.HttpClientResponse|ClientResponse} 
   * object as it's parameter. 
   * @typedef {function} ResponseHandler
   * @param {module:vertx/http.HttpClientResponse} response The HTTP client response
   */
  var wrapResponseHandler = function(handler) {
    return function(jresp) {
      handler(new http.HttpClientResponse(jresp));
    };
  };

};

/**
 * <p>
 * Represents a client-side HTTP request.  Instances are created by an
 * {@linkcode module:vertx/http.HttpClient} instance, via one of the methods
 * corresponding to the specific HTTP methods, or the generic {@linkcode
 * module:vertx/http.HttpClient#request} method.
 * </p>
 * <p>
 * Once a request has been obtained, headers can be set on it, and data can be
 * written to its body if required. Once you are ready to send the request, the
 * {@linkcode module:vertx/http.HttpClientRequest#end} method should be called.
 * </p>
 * <p>
 * The headers of the request are actually sent either when the 
 * {@linkcode module:vertx/http.HttpClientRequest#end} method is called, or,
 * when the first part of the body is written, whichever
 * occurs first.
 * </p>
 * <p>
 * This class supports both chunked and non-chunked HTTP.
 * It mixes in {@linkcode module:vertx/streams~WriteStream} so it can be used with
 * {@linkcode module:vertx/pump~Pump} to pump data with flow control.
 * </p>
 * <p>
 * An example of using this class is as follows:
 * <p>
 *
 * @example
 *
 * var console = require('vertx/console');
 *
 * var req = httpClient.post("/some-url", function(response) {
 *     console.log("Got response: " + response.statusCode);
 *   }
 * });
 *
 * req.headers().add("Content-Length", 5);
 * req.end(new Buffer('hello');
 *
 * @constructor
 * @param org.vertx.java.core.http.HttpClientRequest the underlying Java proxy
 * @augments module:vertx/streams~WriteStream
 */
http.HttpClientRequest = function(jreq) {
  var that = this;
  var reqHeaders = null;

  /**
   * Sets or gets whether the request should used HTTP chunked encoding or not.
   *
   * @param {boolean} [chunked] If val is true, this request will use HTTP
   * chunked encoding, and each call to write to the body will correspond to a
   * new HTTP chunk sent on the wire. If chunked encoding is used the HTTP
   * header 'Transfer-Encoding' with a value of 'Chunked' will be automatically
   * inserted in the request. If <code>chunked</code> is not provided, returns
   * the current value.
   * @returns {boolean|module:vertx/http.HttpClientRequest}
   */
  this.chunked = function(ch) {
    if (ch === undefined) {
      return jreq.isChunked();
    } else {
      jreq.setChunked(ch);
      return that;
    }
  };

  /**
   * Returns the headers for the requests
   *
   * @returns {module:vertx/multi_map~MultiMap} The headers
   */
  this.headers = function() {
    if (!reqHeaders) {
      reqHeaders = new MultiMap(jreq.headers());
    }
    return reqHeaders;
  };

  /**
   * Put a header on the request
   *
   * @param {string} name The header name
   * @param {string} value The header value
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.putHeader = function(k, v) {
    jreq.putHeader(k, v);
    return that;
  };

  /**
   * Write to the request body
   * @param {string} chunk the data to write
   * @param {string} [encoding] the data encoding (default is UTF-8)
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.write = function(arg0, arg1) {
    if (arg1 === undefined) {
      jreq.write(arg0);
    } else {
      jreq.write(arg0, arg1);
    }
    return that;
  };

  /**
   * If you send an HTTP request with the header 'Expect' set to the value
   * '100-continue' and the server responds with an interim HTTP response with
   * a status code of '100' and a continue handler has been set using this
   * method, then the handler will be called.  You can then continue to write
   * data to the request body and later end it. This is normally used in
   * conjunction with the send_head method to force the request header to be
   * written before the request has ended.
   *
   * @param {Handler} handler The handler
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.continueHandler = function(handler) {
    jreq.continueHandler(handler);
    return that;
  };

  /**
   * Forces the head of the request to be written before end is called on the
   * request. This is normally used to implement HTTP 100-continue handling.
   *
   * @see module:vertx/http.HttpClientRequest#continue_handler
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.sendHead = function() {
    jreq.sendHead();
    return that;
  };

  /**
   * Ends the request. If no data has been written to the request body, and
   * send_head has not been called then the actual request won't get written
   * until this method gets called.  Once the request has ended, it cannot be
   * used any more, and if keep alive is true the underlying connection will
   * be returned to the HttpClient pool so it can be assigned to another request.
   *
   * @param {string} [chunk] The data to write
   * @param {string} [encoding] The charset to use if data is written
   */
  this.end = function(arg0, arg1) {
    if (arg0) {
      if (arg1) {
        jreq.end(arg0, arg1);
      } else {
        jreq.end(arg0);
      }
    } else {
      jreq.end();
    }
  };

  /**
   * Set's the amount of time after which if a response is not received an exception
   * will be sent to the exception handler of this request. Calling this method more than once
   * has the effect of canceling any existing timeout and starting the timeout from scratch.
   *
   * @param {number} timeout The amount of time in milliseconds to wait before timing out
   * @returns {module:vertx/http.HttpClientRequest}
   */
  this.timeout = function(t) {
    jreq.setTimeout(t);
    return that;
  };
  streams.WriteStream.call(this, jreq);
};

/**
 * <p>Represents a client-side HTTP response.
 * An instance is provided to the user via a {@linkcode ResponseHandler}
 * instance that was specified when one of the HTTP method operations, or the
 * generic {@linkcode module:vertx.http.HttpClient#request|request}
 * method was called on an instance of {@linkcode module:vertx/http.HttpClient}.
 * </p>
 * <p>
 * It mixes in {@linkcode module:vertx/streams~ReadStream} so it can be used with
 * {@linkcode module:vertx/pump~Pump|Pump} to pump data with flow control.
 * </p>
 * @constructor
 * @param {org.vertx.java.core.http.HttpClientResponse} jresp the underlying Java proxy
 * @see ResponseHandler
 * @augments module:vertx/streams~ReadStream
 */
http.HttpClientResponse = function(jresp) {
  var that = this;
  var respHeaders = null;
  var respTrailers = null;
  var netSocket    = null;

  /**
   *
   * Get the NetSocket. Primarily for internal use, but if you really must
   * roll your own websockets or some such, this will let you do that.
   * @returns {module:vertx/net.NetSocket} The raw <code>NetSocket</code>.
   */
  this.netSocket = function() {
    if (netSocket === null) {
      netSocket = new net.NetSocket(jresp.netSocket());
    }
    return netSocket;
  };

  /**
   * The HTTP status code of the response.
   *
   * @returns {number} The HTTP Status code
   */
  this.statusCode = function() {
    return jresp.statusCode();
  };

  /**
   * The HTTP Status message of the response
   *
   * @returns {string} The HTTP Status message
   */
  this.statusMessage = function() {
    return jresp.statusMessage();
  };

  /**
   * Get all the headers of the response.
   *
   * @returns {module:vertx/multi_map~MultiMap} The response headers
   */
  this.headers = function() {
    if (!respHeaders) {
      respHeaders = new MultiMap(jresp.headers());
    }
    return respHeaders;
  };

  /**
   * Get all the trailing headers of the response.
   *
   * @returns {module:vertx/multi_map~MultiMap} The response trailers
   */
  this.trailers = function() {
    if (!respTrailers) {
      respTrailers = new MultiMap(jresp.trailers());
    }
    return respTrailers;
  };

  /**
    * The Set-Cookie headers (including trailers)
   *
   * @returns {Array} The cookies as an array of strings
   */
  this.cookies = function() {
    return jresp.cookies();
  };

  /**
   * Set a handler to receive the entire body in one go - do not use this for large bodies
   *
   * @param {BodyHandler} handler The handler to use
   * @returns {module:vertx/http.HttpClientResponse}
   */
  this.bodyHandler = function(handler) {
    jresp.bodyHandler(function(buffer) {
      handler(new Buffer(buffer));
    });
    return that;
  };
  streams.ReadStream.call(this, jresp);
};


/**
 * <p>
 * This class allows you to do route requests based on the HTTP verb and the
 * request URI, in a manner similar to <a * href="http://www.sinatrarb.com/">Sinatra</a> 
 * or <a * href="http://expressjs.com/">Express</a>.
 * RouteMatcher also lets you extract paramaters from the request URI either a
 * simple pattern or using regular expressions for more complex matches. Any
 * parameters extracted will be added to the requests parameters
 * which will be available to you in your request handler.
 * </p>
 * <p>
 * It's particularly useful when writing REST-ful web applications.
 * </p>
 *
 * <p>
 * To use a simple pattern to extract parameters simply prefix the parameter
 * name in the pattern with a ':' (colon).
 * </p>
 *
 * <p>
 * Different handlers can be specified for each of the HTTP verbs, GET, POST, PUT, DELETE etc.
 * </p>
 *
 * <p>
 * For more complex matches regular expressions can be used in the pattern.
 * When regular expressions are used, the extracted parameters do not have a
 * name, so they are put into the HTTP request with names of param0, param1,
 * param2 etc.
 * </p>
 *
 * Multiple matches can be specified for each HTTP verb. In the case there are
 * more than one matching patterns for a particular request, the first matching
 * one will be used.
 *
 * @example
 * var http = require('vertx/http');
 * var server = http.createHttpServer();
 * 
 * var routeMatcher = new http.RouteMatcher();
 * 
 * routeMatcher.get('/animals/dogs', function(req) {
 *     req.response.end('You requested dogs');
 * });
 * 
 * routeMatcher.get('/animals/cats', function(req) {
 *     req.response.end('You requested cats');    
 * });
 * 
 * server.requestHandler(routeMatcher).listen(8080, 'localhost');
 *
 * @constructor
 */
http.RouteMatcher = function() {

  var j_rm = new org.vertx.java.core.http.RouteMatcher();

  // req_map keeps track of all JavaScript requests while the corresponding
  // Java request traverses j_rm. We do this in order to pass any JavaScript
  // request passed to this.call() unmodified to the route handlers in case
  // the user has augmented the request.
  var req_map = new java.util.HashMap();

  function req_map_wrappedRequestHandler(handler) {
    var wrappedHandler = wrappedRequestHandler(handler);
    return function(jreq) {
      var req = req_map.remove(jreq);
      if (req) {
        return handler(req);
      } else {
        return wrappedHandler(jreq);
      }
    };
  }

  this.call = function(req) {
    var jreq = req._to_java_request();
    req_map.put(jreq, req);
    j_rm.handle(jreq);
  };

  /**
   * Specify a handler that will be called for a matching HTTP GET
   *
   * @pattern {string} pattern to match
   * @param {RequestHandler} handler handler for match
   * @return {module:vertx/http.RouteMatcher}
   */
  this.get = function(pattern, handler) {
    j_rm.get(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP PUT
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.put = function(pattern, handler) {
    j_rm.put(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP POST
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.post = function(pattern, handler) {
    j_rm.post(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP DELETE
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.delete = function(pattern, handler) {
    j_rm.delete(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP OPTIONS
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.options = function(pattern, handler) {
    j_rm.options(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP HEAD
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.head = function(pattern, handler) {
    j_rm.head(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP TRACE
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.trace = function(pattern, handler) {
    j_rm.trace(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP CONNECT
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.connect = function(pattern, handler) {
    j_rm.connect(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP PATCH
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.patch = function(pattern, handler) {
    j_rm.patch(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP ALL
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.all = function(pattern, handler) {
    j_rm.all(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP GET
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}

   */
  this.getWithRegEx = function(pattern, handler) {
    j_rm.getWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP PUT
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.putWithRegEx = function(pattern, handler) {
    j_rm.putWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP POST
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.postWithRegEx = function(pattern, handler) {
    j_rm.postWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP DELETE
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.deleteWithRegEx = function(pattern, handler) {
    j_rm.deleteWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP PUT
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.optionsWithRegEx = function(pattern, handler) {
    j_rm.optionsWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP HEAD
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.headWithRegEx = function(pattern, handler) {
    j_rm.headWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP TRACE
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.traceWithRegEx = function(pattern, handler) {
    j_rm.traceWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP CONNECT
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.connectWithRegEx = function(pattern, handler) {
    j_rm.connectWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP PATCH
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.patchWithRegEx = function(pattern, handler) {
    j_rm.patchWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for a matching HTTP request
   *
   * @param {string} pattern pattern to match
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.allWithRegEx = function(pattern, handler) {
    j_rm.allWithRegEx(pattern, req_map_wrappedRequestHandler(handler));
    return this;
  };

  /**
   * Specify a handler that will be called for HTTP request that not match any pattern.
   *
   * @param {RequestHandler} handler http server request handler
   * @return {module:vertx/http.RouteMatcher}
   */
  this.noMatch = function(handler) {
    j_rm.noMatch(req_map_wrappedRequestHandler(handler));
    return this;
  };
  /**
   *
   * @returns {org.vertx.java.core.http.RouteMatcher}
   * @private
   */
  this._to_java_handler = function() {
    return j_rm;
  };

  /**
   * For testing.
   * @returns the map of JavaScript requests where the corresponding Java
   * request was passed to the Java RouteMatcher and has yet to return.
   * @private
   */
  this._requests_in_limbo_map = function() {
    return req_map;
  };

  // Regster default noMatchHandler to remove references from req_map.
  this.noMatch(function(req) {
    req.response.statusCode(404).end();
  });
};


/**
 * A <code>WebSocketHandler</code> is a {@linkcode Handler} that accepts a
 * {@linkcode module:vertx/http.WebSocket|WebSocket} as it's parameter.
 *
 * @typedef {function} WebSocketHandler
 * @param {module:vertx/http.WebSocket} websocket the active {@linkcode module:vertx/http.WebSocket|WebSocket}
 */
function wrappedWebsocketHandler(server, handler) {
  return function(jwebsocket) {
    handler(new http.WebSocket(jwebsocket, server));
  };
}

/**
 * A <code>RequestHandler</code> is a {@linkcode Handler} that responds to
 * notifications from objects in the <code>vertx/http</code> module and expects
 * an {@linkcode module:vertx/http.HttpServerRequest|HttpServerRequest} object
 * as its parameter.
 *
 * @example
 * var http = require('vertx/http');
 * var server = http.createHttpServer();
 *
 * server.requestHandler( function( request ) {
 *   // This function is executed for each
 *   // request event on our server
 * } );
 *
 * @see module:vertx/http.HttpServer#requestHandler
 * @typedef {function} RequestHandler
 * @param {message} request The incoming message
 */
function wrappedRequestHandler(handler) {
  return function(jreq) {
    handler(new http.HttpServerRequest(jreq));
  };
}

module.exports = http;

