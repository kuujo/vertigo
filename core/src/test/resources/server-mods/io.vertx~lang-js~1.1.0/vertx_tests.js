if (typeof __vertxload === 'string') {
  throw "Use require() to load Vert.x API modules";
}

var container = require('vertx/container');
var vassert   = require('vertx_assert');
var vertxTests = {};

vertxTests.vassert = vassert;

vertxTests.startTests = function (top) {
  var methodName = container.config.methodName;
  top[methodName]();
};

module.exports = vertxTests;