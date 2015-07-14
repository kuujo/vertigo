if (typeof __vertxload === 'string') {
    throw "Use require() to load the Vert.x API";
}

var vertxAssert = org.vertx.testtools.VertxAssert;
vertxAssert.initialize(__jvertx);

module.exports = vertxAssert;
