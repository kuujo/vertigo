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
 * @module vertx/ssl_support
 */

/**
 * The {@linkcode module:vertx/ssl_support~SSLSupport} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/SSLSupport.java
 */


/**
 * The {@linkcode module:vertx/ssl_support~ServerSSLSupport} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/ServerSSLSupport.java
 */


/**
 * The {@linkcode module:vertx/ssl_support~ClientSSLSupport} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/ClientSSLSupport.java
 */


/**
 * SSL support functions for network objects. Do not use this object directly.
 * These functions are mixed into the network objects for you.
 *
 * @see module:vertx/net.NetServer
 * @see module:vertx/net.NetClient
 * @see module:vertx/http.HttpServer
 * @see module:vertx/http.HttpClient
 * @constructor
 * @param {external:SSLSupport} delegate The Java delegate for most functions
 */
var SSLSupport = function(delegate) {
  /**
   * Get or set the current SSL support for this object.
   * @param {boolean} [ssl] If provided, sets whether this object supports SSL
   * @return {boolean|{}} the current status, or <code>this</code>.
   */
  this.ssl = function(ssl) {
    if (ssl === undefined) {
      return delegate.isSSL();
    } else {
      delegate.setSSL(ssl);
      return this;
    }
  }

  /**
   * Get or set the current keystore path for this object.
   * @param {string} [path] If provided, sets the keystore path
   * @return {boolean|{}} the current path, or <code>this</code>.
   */
  this.keyStorePath = function(path) {
    if (path === undefined) {
      return delegate.getKeyStorePath();
    } else {
      delegate.setKeyStorePath(path);
      return this;
    }
  }

  /**
   * Get or set the current keystore password for this object.
   * @param {string} [password] If provided, sets the keystore password
   * @return {boolean|{}} the current password, or <code>this</code>.
   */
  this.keyStorePassword = function(password) {
    if (password === undefined) {
      return delegate.getKeyStorePassword();
    } else {
      delegate.setKeyStorePassword(password);
      return this;
    }
  }

  /**
   * Get or set the current trust store path for this object.
   * @param {string} [path] If provided, sets the trust store path
   * @return {boolean|{}} the current path, or <code>this</code>.
   */
  this.trustStorePath = function(path) {
    if (path === undefined) {
      return delegate.getTrustStorePath();
    } else {
      delegate.setTrustStorePath(path);
      return this;
    }
  }

  /**
   * Get or set the current trust store password for this object.
   * @param {string} [password] If provided, sets the trust store password
   * @return {boolean|{}} the current password, or <code>this</code>.
   */
  this.trustStorePassword = function(password) {
    if (password === undefined) {
      return delegate.getTrustStorePassword();
    } else {
      delegate.setTrustStorePassword(password);
      return this;
    }
  }
}

/**
 * Provides functions for server-side SSL support. Do not use this object directly.
 * @see module:vertx/net.NetServer
 * @see module:vertx/http.HttpServer
 * @constructor
 * @param {external:ServerSSLSupport} delegate The Java delegate for most functions
 */
var ServerSSLSupport = function(delegate) {
  /**
   * Get or set whether client authorization is required
   * @param {boolean} [required] If provided, sets whether client authorization is required
   * @return {boolean|{{}}} the current status, or <code>this</code>
   */
  this.clientAuthRequired = function(required) {
    if (required === undefined) {
      return delegate.isClientAuthRequired();
    } else {
      delegate.setClientAuthRequired(required);
      return this;
    }
  }
}

/**
 * Provides functions for server-side SSL support. Do not use this object directly.
 * @see module:vertx/net.NetClient
 * @see module:vertx/http.HttpClient
 * @constructor
 * @param {external:ServerSSLSupport} delegate The Java delegate for most functions
 */
var ClientSSLSupport = function(delegate) {
  /**
   * Get or set the trustAll SSL attribute
   * @param {boolean} [all] If provided, sets the trustAll attribute
   * @return {boolean|{{}}} the current trustAll status, or <code>this</code>
   */
  this.trustAll = function(all) {
    if (all === undefined) {
      return delegate.isTrustAll();
    } else {
      delegate.setTrustAll(all);
      return this;
    }
  }
}

module.exports.SSLSupport = SSLSupport;
module.exports.ServerSSLSupport = ServerSSLSupport;
module.exports.ClientSSLSupport = ClientSSLSupport;
