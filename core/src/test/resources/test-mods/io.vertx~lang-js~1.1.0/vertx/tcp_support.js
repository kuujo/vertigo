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
  throw "Use require() to load Vert.x API modules"
}

/**
 * This module provides mixin functions for Vert.x networking objects.
 * Classes defined in this module are not used directly, but rather,
 * exposed through interfaces in {@linkcode module:vertx/net} and 
 * {@linkcode module:vertx/http}.
 *
 * @module vertx/tcp_support
 */

var NetworkSupport = require('vertx/network_support');

/**
 * The {@linkcode module:vertx/tcp_support~TCPSupport} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/TCPSupport.java
 */


/**
 * Provides TCP support functions to network objects. Do not use this object directly.
 * @see module:vertx.net
 * @see module:vertx.http
 * @augments module:vertx/network_support
 * @constructor
 * @param {external:TCPSupport} delegate The Java delegate for most functions
 */
var TCPSupport = function(delegate) {

  // inherits common NetworkSupport functions
  NetworkSupport.call(this, delegate);

  /**
   * Set or get the TCP no delay value.
   * @param {boolean} [nodelay] If provided, set the value; if not, returns the current value.
   * @return {boolean|{{}}} the value or this
   */
  this.tcpNoDelay = function(nodelay) {
    if (nodelay === undefined) {
      return delegate.isTCPNoDelay();
    } else {
      delegate.setTCPNoDelay(nodelay);
      return this;
    }
  }

  /**
   * Set or get the TCP keep-alive value
   * @param {boolean} [keepAlive] If provided, set the value; if not, returns the current value.
   * @return {boolean|{{}}} the value or this
   */
  this.tcpKeepAlive = function(keepAlive) {
    if (keepAlive === undefined) {
      return delegate.isTCPKeepAlive();
    } else {
      delegate.setTCPKeepAlive(keepAlive);
      return this;
    }
  }

  /**
   * Set or get the TCP so linger value
   * @param {boolean} [linger] If provided, set the value; if not, returns the current value.
   * @return {boolean|{{}}} the value or this
   */
  this.soLinger = function(linger) {
    if (linger === undefined) {
      return delegate.isSoLinger();
    } else {
      delegate.setSoLinger(linger);
      return this;
    }
  }
  
  /**
   * Set or get if vertx should use pooled buffers for performance reasons.
   * Doing so will give the best throughput but may need a bit higher memory
   * footprint.
   * @param {boolean} [use] If provided, set the value; if not, returns the current value.
   * @return {boolean|{{}}} the value or this
   */
  this.usePooledBuffers = function(use) {
    if (use === undefined) {
      return delegate.isUsedPooledBuffers();
    } else {
      delegate.setUsePooledBuffers(use);
      return this;
    }
  }
}

/**
 * The {@linkcode module:vertx/tcp_support~ServerTCPSupport} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/ServerTCPSupport.java
 */

/**
 * Provides server-side-only TCP support functions. Do not use this object directly.
 *
 * @see module:vertx/net
 * @see module:vertx/http
 * @constructor
 * @param {external:TCPSupport} delegate The Java delegate for most functions
 */
var ServerTCPSupport = function(delegate) {
  /**
   * Set or get the server's accept backlog
   * @param {number} [backlog] If provided, set the value; if not, returns the current value.
   * @return {number|{{}}} the value or this
   */
  this.acceptBacklog = function(backlog) {
    if (backlog === undefined) {
      return delegate.getAcceptBacklog();
    } else {
      delegate.setAcceptBacklog(backlog);
      return this;
    }
  }
}

module.exports.TCPSupport = TCPSupport;
module.exports.ServerTCPSupport = ServerTCPSupport;
