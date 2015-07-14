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
 * A TimerId is just a number that identifies a given timer.
 * @see module:vertx/timer
 * @typedef {number} TimerId 
 * */


/**
 * @exports vertx/timer
 */
var timers = {

  /**
   * Sets a one-shot timer that will fire after a certain delay.
   *
   * @param {number} delay the delay, in milliseconds
   * @param {function} handler an handler that will be called when the timer fires
   * @returns {TimerId} the unique id of the timer
   */
  setTimer : function(delay, handler) {
    return __jvertx.setTimer(delay, handler);
  },

  /**
   * Set a periodic timer.
   *
   * @param {number} interval the period of the timer, in milliseconds
   * @param {function} handler an handler that will be called each time the timer fires
   * @returns {TimerId} the unique id of the timer
   */
  setPeriodic : function(interval, handler) {
    return __jvertx.setPeriodic(interval, handler);
  },

  /**
   * Cancels a timer.
   *
   * @param {TimerId} id the id of the timer, as returned from set_timer or set_periodic
   * @returns {boolean} true if the timer was cancelled, false if it wasn't found.
   */
  cancelTimer : function(id) {
    return __jvertx.cancelTimer(id);
  }
};

module.exports = timers;

