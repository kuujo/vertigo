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

if (typeof module === 'undefined') {
  throw "Use require() to load Vert.x API modules"
}

/**
 * <p>
 * Sometimes it makes sense to allow different verticles instances to share
 * data in a safe way. Vert.x allows simple Map and Set data structures to be
 * shared between verticles.
 * </p><p>
 * There is a caveat: To prevent issues due to mutable data, vert.x only allows
 * simple immutable types such as number, boolean and string or Buffer to be
 * used in shared data. With a Buffer, it is automatically copied when
 * retrieved from the shared data, so different verticle instances never see
 * the same object instance.
 * </p>
 * @see https://github.com/vert-x/vert.x/blob/master/vertx-core/src/main/java/org/vertx/java/core/shareddata/SharedData.java
 *
 * @exports vertx/shared_data
 */
var sharedData = {};

/**
 * Return a <code>Map</code> with the specific <code>name</code>. All invocations of this
 * method with the same value of <code>name</code> are guaranteed to return the same
 * <code>Map</code> instance.
 * @param {string} name The name of the map
 * @return {{}} The shared data map
 */
sharedData.getMap = function(name) {
  return __jvertx.sharedData().getMap(name);
}

/**
 * Return a <code>Set</code> with the specific <code>name</code>. All invocations of this
 * method with the same value of <code>name</code> are guaranteed to return the same
 * <code>Set</code> instance.
 * @param {string} name The name of the set
 * @return {Array} The shared data set
 */
sharedData.getSet = function(name) {
  return __jvertx.sharedData().getSet(name);
}

sharedData.removeMap = function(name) {
  return __jvertx.sharedData().removeMap(name);
}

sharedData.removeSet = function(name) {
  return __jvertx.sharedData().removeSet(name);
}

module.exports = sharedData;

