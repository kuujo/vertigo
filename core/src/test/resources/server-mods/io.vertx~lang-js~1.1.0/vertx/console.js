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


var stdout = java.lang.System.out;
var stderr = java.lang.System.err;
/**
 * A simple console object that can be used to print log messages
 * errors, and warnings. 
 * @example
 * var console = require('vertx/console');
 *
 * console.log('Hello standard out');
 * console.warn('Warning standard error');
 * console.error('Alert! Alert!');
 *
 * @exports vertx/console
 */
var console = {

  // TODO this should take varargs and allow formatting a la sprintf
  /**
   * Log the msg to STDOUT.
   *
   * @param {string} msg The message to log to standard out.
   */
  log: function(msg) {
    stdout.println(msg);
  },

  /**
   * Log the msg to STDERR
   *
   * @param {string} msg The message to log with a warning to standard error.
   */
  warn: function(msg) {
    stderr.println(msg);
  },

  /**
   * Log the msg to STDERR
   *
   * @param {string} msg The message to log with a warning alert to standard error.
   */
  error: function(msg) {
    stderr.println(msg);
  }
};

module.exports = console;
