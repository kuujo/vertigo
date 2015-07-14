/*
 *
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
 * The {@linkcode module:vertx/buffer~Buffer} interface delegates most
 * function calls to the Java class provided by Vert.x
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/buffer/Buffer.java
 * @external org.vertx.java.core.buffer.Buffer
 */

/**
 * Most data in vert.x is shuffled around using buffers.
 *
 * A Buffer represents a sequence of zero or more bytes that can be written to
 * or read from, and which expands automatically as necessary to accomodate any
 * bytes written to it. You can perhaps think of a buffer as smart byte array.
 *
 * The methods documented for the Java Buffer objects
 * are applicable to Javascript Buffer instances as well. 
 *
 * @see https://github.com/eclipse/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/buffer/Buffer.java
 *
 * @example
 * var Buffer = require('vertx/buffer');
 * var buff   = new Buffer('Hello!');
 *
 * @example <caption>Creating Buffers</caption>
 * var Buffer = require('vertx/buffer');
 *
 * // Create a buffer from a string with UTF-8 encoding (the default)
 * var buff = new Buffer('Now is the winter of our discontent made glorioius summer');
 *
 * // Create a buffer from a string and specify an encoding
 * buff = new Buffer('Too hot, too hot!', 'UTF-16');
 *
 * // etc etc
 * // TODO: Finish these examples
 *
 * @constructor
 * @param {string|number|undefined} [obj] The paramater from which the buffer
 * will be created. If a string is given it create a new Buffer which contains
 * the string. If a number is given it will create a new Buffer which has the
 * given initial size. if no parameter is defined a new empty Buffer will be
 * created.
 */
var Buffer = function(obj) {
  var __jbuf;
  if (typeof obj ==='undefined') {
    __jbuf = new org.vertx.java.core.buffer.Buffer();
  } else if (typeof obj === 'string') {
    __jbuf = new org.vertx.java.core.buffer.Buffer(obj);
  } else if (typeof obj === 'number') {
    __jbuf = new org.vertx.java.core.buffer.Buffer(obj);
  } else {
    __jbuf = obj;
  }

  /**
   * Get an unsigned 8 bit integer from the buffer.
   *
   * @param {number} pos the position
   * @returns {number}
   */
  this.getByte = function(pos) {
    return __jbuf.getByte(pos);
  };

  /**
   * Get a signed 32 bit integer from the buffer.
   *
   * @param {number} pos the position
   * @returns {number}
   */
  this.getInt = function(pos) {
    return __jbuf.getInt(pos);
  };

  /**
   * Get a signed 64 bit double from the buffer.
   *
   * @param {number} pos the position
   * @returns {number}
   */
  this.getDouble = function(pos) {
    return __jbuf.getDouble(pos);
  };

  /**
   * Get a signed 32 bit float from the buffer.
   *
   * @param {number} pos the position
   * @returns {number}
   */
  this.getFloat = function(pos) {
    return __jbuf.getFloat(pos);
  };

  /**
   * Get a signed 16 bit short from the buffer.
   *
   * @param {number} pos the position
   * @returns {number}
   */
  this.getShort = function(pos) {
    return __jbuf.getShort(pos);
  };

  /**
   * Get a {module:vertx/buffer~Buffer} for the given range from the buffer.
   *
   * @param {number} start of the range
   * @param {number} end of the range, exclusive
   * @returns {module:vertx/buffer~Buffer}
   */
  this.getBuffer = function(start, end) {
    return new Buffer(__jbuf.getBuffer(start, end));
  };

  /**
   * Get a string for the given range from the buffer.
   *
   * @param {number} start of the range
   * @param {number} end of the range, exclusive
   * @param {string} [enc] An optional encoding for the returned string
   * @returns {string}
   */
  this.getString = function(start, end, enc) {
    if (typeof enc === 'undefined') {
      return __jbuf.getString(start, end);
    } else  {
      return __jbuf.getString(start, end, enc);
    }
  };

  /**
   * Append the contents of the provided buffer to this buffer.
   *
   * @param {module:vertx/buffer~Buffer} buf a buffer
   * @param {number} [offset] the offset in the provided buffer from which
   * point bytes should be read. Defaults to 0.
   * @param {number} [length] the number of bytes to read from the provided
   * buffer. Defaults to buf.length() - offset.
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendBuffer = function(buf, offset, length) {
    var _offset = offset ? offset : 0;
    var _length = length ? length : buf.length() - _offset;
    __jbuf.appendBuffer(buf._to_java_buffer(), _offset, _length);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {number} b a valid signed 8 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendByte = function(b) {
    __jbuf.appendByte(b);
    return this;
  };

  /**
   * Append a byte array to this Buffer.
   * @param {array} b an array of bytes
   * @param {number} [o] offset in the byte array from which to start reading
   * @param {number} [l] the number of bytes to read from the byte array
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendBytes = function(b, o, l) {
    var _offset = o ? o : 0;
    var _length = l ? l : (b.length - _offset);
    __jbuf.appendBytes(b, _offset, _length);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {number} i a valid signed 32 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendInt = function(i) {
    __jbuf.appendInt(i);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {number} s a valid signed 16 bit short
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendShort = function(s) {
    __jbuf.appendShort(s);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {number} f a valid signed 32 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendFloat = function(f) {
    __jbuf.appendFloat(f);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {number} d a valid signed 64 bit double
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendDouble = function(d) {
    __jbuf.appendDouble(d);
    return this;
  };

  /**
   * Append to the buffer.
   *
   * @param {string} str the string
   * @param {string} [enc] the encoding to use or {undefined} if the default
   * should be used.  
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.appendString = function(str, enc) {
    if (typeof enc === 'undefined') {
      __jbuf.appendString(str);

    } else  {
      __jbuf.appendString(str, enc);
    }
    return this;
  };

  /**
   * Set on the buffer at the given position.
   *
   * @param {number} pos the position on which to set b
   * @param {number} b a valid signed 8 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setByte = function(pos, b) {
    __jbuf.setByte(pos, b);
    return this;
  };

  /**
   * Sets a byte array on this Buffer at the given position.
   * @param {number} p the position to begin writing in this buffer
   * @param {array} b an array of bytes
   * @param {number} o offset in the byte array from which to start reading
   * @param {number} l the number of bytes to read from the byte array
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setBytes = function(pos, b, o, l) {
    var _offset = o ? o : 0;
    var _length = l ? l : (b.length - _offset);
    __jbuf.setBytes(pos, b, _offset, _length);
    return this;
  };

  /**
   * Set on the buffer at the given position.
   *
   * @param {number} pos the position on which to set i
   * @param {number} i a valid signed 32 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setInt = function(pos, i) {
    __jbuf.setInt(pos, i);
    return this;
  };

  /**
   * Set on the buffer at the given position.
   *
   * @param {number} pos the position on which to set d
   * @param {number} d a valid signed 64 bit double
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setDouble = function(pos, d) {
    __jbuf.setDouble(pos, d);
    return this;
  };

  /**
   * Set on the buffer at the given position.
   *
   * @param {number} pos the position on which to set f
   * @param {number} f a valid signed 32 bit integer
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setFloat = function(pos, f) {
    __jbuf.setFloat(pos, f);
    return this;
  };

  /**
   * Set on the buffer at the given position.
   *
   * @param {number} pos the position on which to set s
   * @param {number} s a valid signed 16 bit short
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setShort = function(pos, s) {
    __jbuf.setShort(pos, s);
    return this;
  };

  /**
   * Sets the bytes from the provided buffer onto this buffer.
   *
   * @param {number} position the position on which to set the buffer
   * @param {module:vertx/buffer~Buffer} buffer the buffer to read from
   * @param {number} [offset] the point at which bytes should be read from 
   * the provided buffer. Defaults to 0.
   * @param {number} [length] the number of bytes to read from the provided
   * buffer. Defaults to buffer.length() - offset.
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setBuffer = function(pos, b, o, l) {
    var _offset = o ? o : 0;
    var _length = l ? l : b.length() - _offset;
    __jbuf.setBuffer(pos, b._to_java_buffer(), _offset, _length);
    return this;
  };

  /**
   * Set a string in the buffer at the given position.
   *
   * @param {number} pos the position on which to set str
   * @param {string} str the string
   * @param {string} [enc] an optional encoding to use
   * @returns {module:vertx/buffer~Buffer} this
   */
  this.setString = function(pos, str, enc) {
    if (typeof enc === 'undefined') {
      __jbuf.setString(pos, str);
    } else {
      __jbuf.setString(pos, str, enc);
    }
    return this;
  };

  /**
   * Get the length of the buffer
   * @returns {number} The buffer length. Note this is the actual length
   * of the data in the buffer, not an allocated length. For example, 
   *
   * @example
   *
   * var b = new Buffer(1024);
   * b.length(); // => 0
   * b = new Buffer("Hello world!");
   * b.length(); // => 12
   */
  this.length = function() {
    return __jbuf.length();
  };

  /**
   * Create a copy of this buffer and its content.
   *
   * @returns {module:vertx/buffer~Buffer}
   */
  this.copy = function() {
    return new Buffer(__jbuf.copy());
  };

  /**
   * Determines if this buffer is equal to the other
   * @param {module:vertx/buffer~Buffer} other the buffer to compare to this one
   * @returns {boolean} true if the buffers are equal
   */
  this.equals = function(o) {
    if (o instanceof Buffer) {
      return __jbuf.equals(o._to_java_buffer());
    }
    return false;
  };

  /**
   * Returns this buffer as a string. The default encoding is UTF-8.
   * @param {string} [encoding] An optional encoding
   * @returns {string} This buffer as a string
   */
  this.toString = function(enc) {
    if (typeof enc === 'undefined')  {
      return __jbuf.toString();
    } else {
      return __jbuf.toString(enc);
    }
  };

  /**
   * @private
   */
  this._to_java_buffer = function() {
    return __jbuf;
  };
};


/**
 * @see https://github.com/vert-x/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/buffer/Buffer.java
 * @module vertx/buffer
 */
module.exports = Buffer;
