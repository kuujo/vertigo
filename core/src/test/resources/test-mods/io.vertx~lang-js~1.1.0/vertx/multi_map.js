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
 * A <code>MultiMap</code> is a type of Map object that can store more
 * than one value for a given key. This is useful for storing and passing
 * around things like HTTP headers where a single header can have multiple
 * values.
 *
 * @constructor
 * @param {org.vertx.java.core.MultiMap} multiMap the underlying Java MultiMap instance
 */
var MultiMap = function(j_map) {

  /**
   * Return the value for the given name
   *
   * @param {string} name The name to lookup in the map
   * @returns {string} value The value for the given name. If more than one value maps to 
   *          <code>name</code>, the first value is returned.
   */
  this.get = function(name) {
    if (!j_map) { return undefined; }
    var value = j_map.get(name);
    // Handles discrepancy between how rhino and dynjs deal with null return values 
    // from Java objects. It seems as though Rhino changes the null to undefined, or
    // perhaps has some distinction where undefined is returned iff the key isn't there.
    if (value === null) { 
      return undefined;
    }
    return value;
  };

  /**
   * Execute the given function for every name, value pair stored
   *
   * @param {function} func The function to execute
   */
  this.forEach = function(func) {
    if (!j_map) { return; }
    var names = j_map.names().iterator();
    while (names.hasNext()) {
      var name = names.next();
      var values = j_map.getAll(name).iterator();
      while (values.hasNext()) {
        func(name, values.next());
      }
    }
  };

  /**
   * Return all values stored for the given name.
   *
   * @param {string} name The name to lookup values for
   * @returns {Array} The values for the given name
   */
  this.getAll = function(name) {
    if (!j_map) { return []; }
    var n =  j_map.getAll(name);
    return _convertToArray(n);
  };

  /**
   * Returns if a value for the given name is stored
   *
   * @param {string} name The name to check for
   * @returns {boolean} <code>true</code> if <code>name</code> is stored in this map
   */
  this.contains = function(name) {
    if (!j_map) { return false; }
    return j_map.contains(name);
  };

  /**
   * Returns if this map is empty
   *
   * @returns {boolean} <code>true</code> if empty
   */
  this.isEmpty = function() {
    if (!j_map) { return true; }
    return j_map.isEmpty();
  };

  /**
   * Return all names for which values are stored
   *
   * @returns {Array} The names for which values are stored
   */
  this.names = function() {
    if (!j_map) { return []; }
    var n =  j_map.names();
    return _convertToArray(n);
  };

  /**
   * Add a value for the given name
   *
   * @param {string} name The name under which the value should be stored
   * @param {string} value The value to store
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.add = function(name, value) {
    if (j_map) { 
      j_map.add(name, value);
    }
    return this;
  };

  /**
   * Set a value for the given name. All previous stored values under the name will get deleted.
   *
   * @param {string} name The name under which the value should be stored
   * @param {string} value The value to store
   * @returns {module:vertx/multi_map~MultiMap}
   */
  this.set = function(name, value) {
    if (j_map) {
      j_map.set(name, value);
    }
    return this;
  };

  /**
   * Set the content of the given map.
   *
   * @param {module:vertx/multi_map~MultiMap} The map to set
   * @returns {module:vertx/multi_map~MultiMap} self
   */
  this.setMap = function(map) {
    if (j_map) {
      j_map.set(map._jmap);
    }
    return this;
  };

  /**
   * Remove all values stored under the name
   *
   * @param {string} name The name for which all values should be removed
   * @returns {module:vertx/multi_map~MultiMap} self
   */
  this.remove = function(name) {
    if (j_map) {
      j_map.remove(name);
    }
    return this;
  };

  /**
   * Clears the map
   *
   * @returns {module:vertx/multi_map~MultiMap} self
   */
  this.clear = function() {
    if (j_map) {
      j_map.clear();
    }
    return this;
  };

  /**
   * Return the number of names stored.
   * @returns {number} the number of names stored
   */
  this.size = function() {
    if (!j_map) { return 0; }
    return j_map.size();
  };

  /**
   * @private
   */
  this._jmap = j_map;
};

function _convertToArray(j_col) {
  var n = j_col.iterator();
  var array = [];
  var i = 0;

  while (n.hasNext()) {
    array[i++] = n.next();
  }
  return array;
}


/**
 * @module vertx/multi_map
 */

module.exports.MultiMap = MultiMap;
