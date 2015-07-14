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

var Buffer = require('vertx/buffer');
var parseTools = require('vertx/parse_tools');

/**
 * <p>
 * There are several objects in vert.x that allow data to be read from and
 * written to in the form of Buffers.  In Vert.x, calls to write data return
 * immediately and writes are internally queued.
 * </p>
 *
 * <p>
 * It's not hard to see that if you write to an object faster than it can
 * actually write the data to its underlying resource then the write queue
 * could grow without bound - eventually resulting in exhausting available
 * memory.
 * </p>
 *
 * <p>
 * To solve this problem a simple flow control capability is provided by some
 * objects in the vert.x API.
 * </p>
 *
 * <p>
 * Any flow control aware object that can be written to is said to implement
 * {@linkcode module:vertx/streams~WriteStream}, and any flow control object
 * that can be read from is said to implement {@linkcode module:vertx/streams~ReadStream}.  
 * </p>
 *
 * <p>
 * The object types defined in this module are typically not instantiated
 * by directly, but are provided to callback functions in modules such as
 * {@linkcode module:vertx/file_system}, {@linkcode module:vertx/net} and
 * {@linkcode module:vertx/http}.
 * </p>
 *
 * @module vertx/streams
 */

/**
 * The {@linkcode module:vertx/streams~ReadStream} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/streams/ReadStream.java
 */

/**
 * The {@linkcode module:vertx/streams~WriteStream} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/streams/WriteStream.java
 *
 */

/**
 * Provides methods for querying and draining data streams.
 * This is used internally and exposed through {@linkcode module:vertx/streams~WriteStream}
 * and other mixins and is not typically used directly.
 * @param {external:org.vertx.java.core.streams.WriteStream} delegate The Java delegate
 * @class
 */
var DrainSupport = function(delegate) {
  /**
   * Set the maximum size of the write queue to <code>maxSize</code>. You
   * will still be able to write to the stream even if there is more than
   * <code>maxSize</code> bytes in the write queue. This is used as an
   * indicator by classes such as <code>Pump</code> to provide flow control.
   * @param {number} size the size of the write queue
   */
  this.writeQueueMaxSize = function(size) {
    delegate.setWriteQueueMaxSize(size);
    return this;
  };
  /**
   * This will return <code>true</code> if there are more bytes in the write
   * queue than the value set using <code>writeQueueMaxSize</code>
   */
  this.writeQueueFull = function() {
    return delegate.writeQueueFull();
  };
  /**
   * Set a drain handler on the stream. If the write queue is full, then the
   * handler will be called when the write queue has been reduced to maxSize/2.
   * See {@linkcode module:vertx/pump~Pump} for an example of this being used.
   * @param {Handler} handler the handler to call when the stream has been drained
   */
  this.drainHandler = function(handler) {
    delegate.drainHandler(handler);
    return this;
  };
  /**
   * Set an exception handler on the stream
   * @param {Handler} handler the handler to call when an exception occurs
   */
  this.exceptionHandler = function(handler) {
    delegate.exceptionHandler(handler);
    return this;
  };
};

/**
 * Provides methods to read from a data stream. It's used by things
 * like {@linkcode module:vertx/file_system.AsyncFile} and {@linkcode
 * module:vertx/http.HttpServerResponse} and is not typically instantiated
 * directly.
 *
 * @param {external:WriteStream} delegate The Java delegate
 * @augments module:vertx/streams~DrainSupport
 * @class
 */
var WriteStream = function(delegate) {
  /**
   * Write some data to the stream. The data is put on an internal write
   * queue, and the write actually happens asynchronously. To avoid running
   * out of memory by putting too much on the write queue, check the 
   * {@link module:vertx/streams~WriteStream#writeQueueFull} method before
   * writing. This is done automatically if using a {@linkcode module:vertx/pump~Pump}.
   *
   * @param {module:vertx/buffer~Buffer} data the data to write
   */
  this.write = function(data) {
    if (data instanceof Buffer) {
      delegate.write(data._to_java_buffer());
    } else {
      delegate.write(data);
    }
    return this;
  };

  DrainSupport.call(this, delegate);

  /**
   * @private
   */
  this._delegate = function() { return delegate; };
};

/**
 * Provides methods to read from a data stream.  This is used internally and
 * exposed through {@linkcode module:vertx/streams~ReadStream} and other mixins
 * and is not typically used directly.
 *
 * @param {external:ReadStream} delegate The Java delegate
 * @class
 */
var ReadSupport = function(delegate) {
  /**
   * Set a data handler. As data is read, the handler will be called with 
   * a Buffer containing the data read.
   * @param {BodyHandler} handler the handler to call
   */
  this.dataHandler = function(handler) {
    delegate.dataHandler(function(buf) {
      handler.call(handler, new Buffer(buf));
    });
    return this;
  };
  /**
   * Pause the <code>ReadStream</code>. While the stream is paused, no data
   * will be sent to the data Handler
   */
  this.pause = function() {
    delegate.pause();
    return this;
  };
  /**
   * Resume reading. If the <code>ReadStream</code> has been paused, reading
   * will recommence on it.
   */
  this.resume = function() {
    delegate.resume();
    return this;
  };
  /**
   * Set an exception handler.
   * @param {Handler} handler the handler to call
   */
  this.exceptionHandler = function(handler) {
    delegate.exceptionHandler(handler);
    return this;
  };
};

/**
 * Provides methods to read from a data stream. It's used by things
 * like {@linkcode module:vertx/file_system.AsyncFile} and {@linkcode
 * module:vertx/http.HttpServerResponse} and is not typically instantiated
 * directly.
 *
 * @param {external:ReadStream} delegate The Java delegate
 * @augments module:vertx/streams~ReadSupport
 * @class
 */
var ReadStream = function(delegate) {
  /**
   * Set an end handler. Once the stream has ended, and there is no more data
   * to be read, the handler will be called.
   * @param {Handler} handler the handler to call
   */
  this.endHandler = function(handler) {
    delegate.endHandler(handler);
    return this;
  };

  ReadSupport.call(this, delegate);

  /**
   * @private
   */
  this._delegate = function() { return delegate; };
};

module.exports.ReadSupport  = ReadSupport;
module.exports.ReadStream   = ReadStream;

module.exports.DrainSupport = DrainSupport;
module.exports.WriteStream  = WriteStream;

