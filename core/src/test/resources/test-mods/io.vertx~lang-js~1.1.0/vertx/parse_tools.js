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

/** 
 * <p>
 * A helper class which allows you to easily parse protocols which are
 * delimited by a sequence of bytes, or fixed size records.
 * Instances of this class take as input {@linkcode module:vertx/buffer~Buffer|Buffer}
 * instances containing raw bytes, and output records. For example, if I had a
 * simple ASCII text protocol delimited by '\n' and the input was the
 * following:
 * </p>
 * <pre>
 * buffer1:HELLO\nHOW ARE Y
 * buffer2:OU?\nI AM
 * buffer3: DOING OK
 * buffer4:\n
 * </pre>
 * <p>Then the output would be:</p>
 * <pre>
 * buffer1:HELLO
 * buffer2:HOW ARE YOU?
 * buffer3:I AM DOING OK
 * </pre>
 * <p>
 * Instances of this class can be changed between delimited mode and fixed size
 * record mode on the fly as individual records are read, this allows you to
 * parse protocols where, for example, the first 5 records might all be fixed
 * size (of potentially different sizes), followed by some delimited records,
 * followed by more fixed size records.
 * </p>
 * <p>
 * Instances of this class can't currently be used for protocols where the text
 * is encoded with something other than a 1-1 byte-char mapping.
 * </p>
 *
 * @see https://github.com/vert-x/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/parsetools/RecordParser.java
 *
 * @exports vertx/parse_tools 
 * */
var parseTools = {};

/**
 * Create a new <code>RecordParser</code> instance, initially in delimited
 * mode, and where the delimiter can be represented by <code>delim</code>.
 * <code>output</code> will receive whole records which have been parsed.
 * @param {string} delim The record delimiter
 * @param {Function} output The function to call once more data is ready
 * @returns {module:vertx/parse_tools.RecordParser} A delimited record parser
 */
parseTools.createDelimitedParser = function(delim, output) {
  return new parseTools.RecordParser(org.vertx.java.core.parsetools.RecordParser.newDelimited(delim, function(buf) {
    output(new Buffer(buf));
  }));
};

/**
 * Create a new <code>RecordParser</code> instance, initially in fixed size
 * mode, and where the record size is specified by the <code>size</code>
 * parameter.  <code>output</code> will receive whole records which have been
 * parsed.
 * @param {number} size The record size
 * @param {Function} The function to call once more data is ready
 * @returns {module:vertx/parse_tools.RecordParser} A fixed size record parser
 */
parseTools.createFixedParser = function(size, output) {
  return new parseTools.RecordParser(org.vertx.java.core.parsetools.RecordParser.newFixed(size, function(buf) {
    output(new Buffer(buf));
  }));
};

/**
 *
 * @param parser
 * @constructor
 */
parseTools.RecordParser = function(parser) {
  var _jparser = parser;

  /**
   * Flip the parser into delimited mode, and where the delimiter can be represented
   * by the String delim encoded in latin-1 . Don't use this if your String contains other than latin-1 characters.<p>
   *
   * This method can be called multiple times with different values of delim while data is being parsed.
   * @param delim
   */
  this.delimitedMode = function(delim) {
    _jparser.delimitedMode(delim);
  };

  /**
   /**
   * Flip the parser into fixed size mode, where the record size is specified by size in bytes.<p>
   * This method can be called multiple times with different values of size while data is being parsed.
   *
   * @param {number} size
   */
  this.fixedSizeMode = function(size) {
    _jparser.fixedSizeMode(size);
  };

  /**
   * Allows a Parser to be provided to a {vertx/streams~ReadStream}'s
   * dataHandler function. 
   * @param {} the value for 'this'
   * @param {vertx/buffer.Buffer} buffer 
   */
  this.call = function(that, buf) {
    _jparser.handle(buf._to_java_buffer());
    return that;
  };

  this.handle = function(buf) {
    _jparser.handle(buf._to_java_buffer());
    return this;
  };
};

module.exports = parseTools;

