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
 * Functions defined in this module are exposed through objects in
 * {@linkcode module:vertx/net} and {@linkcode module:vertx/http}.
 *
 * @module vertx/network_support
 */

/**
 * The {@linkcode module:vertx/network_support} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/NetworkSupport.java
 */


/**
 * Provides common parameters that can be set on network objects. Do not use
 * this directly.  
 *
 * @see module:vertx/net
 * @see module:vertx/http
 * @alias module:vertx/network_support
 * @constructor
 * @param {external:NetworkSupport} delegate The Java delegate for most functions
 */
var NetworkSupport = function(delegate) {

  /**
   * Set or get the send buffer size
   * @param {number} [size] If provided, set the value; if not, returns the current value.
   * @return {number|{{}}} the value or this
   */
  this.sendBufferSize = function(size) {
    if (size === undefined) {
      return delegate.getSendBufferSize();
    } else {
      delegate.setSendBufferSize(size);
      return this;
    }
  }

  /**
   * Set or get the receive buffer size
   * @param {number} [size] If provided, set the value; if not, returns the current value.
   * @return {number|{{}}} the value or this
   */
  this.receiveBufferSize = function(size) {
    if (size === undefined) {
      return delegate.getReceiveBufferSize();
    } else {
      delegate.setReceiveBufferSize(size);
      return this;
    }
  }

  /**
   * Set or get the TCP reuse address value
   * @param {boolean} [reuse] If provided, set the value; if not, returns the current value.
   * @return {boolean|{{}}} the value or this
   */
  this.reuseAddress = function(reuse) {
    if (reuse === undefined) {
      return delegate.isReuseAddress();
    } else {
      delegate.setReuseAddress(reuse);
      return this;
    }
  }

  /**
   * Set or get the TCP traffic class
   * @param {number} [class] If provided, set the value; if not, returns the current value.
   * @return {number|{{}}} the value or this
   */
  this.trafficClass = function(cls) {
    if (cls === undefined) {
      return delegate.getTrafficClass();
    } else {
      delegate.setTrafficClass(cls);
      return this;
    }
  }
}

module.exports = NetworkSupport;
