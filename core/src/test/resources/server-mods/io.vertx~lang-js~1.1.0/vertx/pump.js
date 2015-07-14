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

var console = require("vertx/console");

/** 
 * <p>
 * Pumps data from a {@linkcode ReadStream} to a 
 * {@linkcode WriteStream} and performs flow control
 * where necessary to prevent the write stream from getting overloaded.
 * </p>
 * <p>
 * Instances of this class read bytes from a {@linkcode ReadStream}
 * and write them to a {@linkcode WriteStream}. If data
 * can be read faster than it can be written this could result in the write
 * queue of the WriteStream growing without bound, eventually
 * causing it to exhaust all available RAM.
 * </p>
 * <p>
 * To prevent this, after each write, instances of this class check whether the
 * write queue of the WriteStream is full, and if so, the ReadStream is paused,
 * and a drainHandler is set on the WriteStream. When the WriteStream has
 * processed half of its backlog, the drainHandler will be called, which
 * results in the pump resuming the ReadStream.
 * </p>
 *
 * <p>
 * This class can be used to pump from any ReadStream to any WriteStream, e.g.
 * from an HttpServerRequest to an AsyncFile, or from NetSocket to a WebSocket.
 * </p>
 *
 * @constructor 
 * @param {module:vertx/streams~ReadStream} readStream a ReadStream
 * @param {module:vertx/streams~WriteStream} writeStream a WriteStream
 * */


 var Pump = function(rs, ws) {

  /** @private */
  var _delegate = org.vertx.java.core.streams.Pump.createPump(rs._delegate(), ws._delegate());

  /** @private */
  var _self = this;

  /**
   * Start the Pump. The Pump can be started and stopped multiple times.
   * @returns {module:vertx/pump~Pump}
   */
  this.start = function() {
    _delegate.start();
    return _self;
  };

  /**
   * Stop the Pump. The Pump can be started and stopped multiple times.
   * @returns {module:vertx/pump~Pump}
   */
  this.stop = function() {
    _delegate.stop();
    return _self;
  };

  /**
   * Return the total number of bytes pumped by this pump.
   * @returns {number} the number of bytes pumped
   */
  this.bytesPumped = function() {
    return _delegate.bytesPumped();
  };

  /**
   * Set the write queue max size to maxSize
   * @param {number} maxSize the maximum size of the write queue
   * @returns {module:vertx/pump~Pump}
   */
  this.setWriteQueueMaxSize = function(maxSize) {
    _delegate.setWriteQueueMaxSize(maxSize);
    return _self;
  };


};

/** @module vertx/pump */
module.exports = Pump;
