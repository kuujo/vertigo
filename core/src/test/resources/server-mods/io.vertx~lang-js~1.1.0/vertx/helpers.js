var helpers = {

  getArgValue: function(type, args) {
    if (args.length === 0) {
      return null;
    }
    var arg = args[args.length - 1];
    if (typeof(arg) === type) {
      args.pop();
      return arg;
    } else {
      return null;
    }
  },

  adaptAsyncResultHandler: function(handler, resultConverter) {
    return function(fr) {
      if (handler) {
        if (fr.failed()) {
          handler(fr.cause(), null);
        } else {
          var result = fr.result();
          if (resultConverter) {
            result = resultConverter(fr.result());
          }
          handler(null, result);
        }
      }
    };
  },

  convertInetSocketAddress: function(javaAddr) {
    return {
      address: javaAddr.getHostString(),
      port: javaAddr.getPort(),
      getPort: function() { return javaAddr.getPort(); },
      getHostString: function() { return javaAddr.getHostString(); }
    };
  }
};

module.exports = helpers;

