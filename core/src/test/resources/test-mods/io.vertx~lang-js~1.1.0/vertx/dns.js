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

// handler wrapper
var helpers = require("vertx/helpers.js");

/**
 * @exports vertx/dns
 */
var dns = {
  /**
   * Creates and returns a DNS client object
   *
   * @param {Array|string} servers The DNS server address(es). 
   * @returns {module:vertx/dns.DnsClient} A DnsClient object 
   */
  createDnsClient: function(servers) {
    if (typeof servers == 'undefined') {
      servers = '127.0.0.1';
    }
    return new dns.DnsClient(servers);
  }
};

/**
* @class 
* @param {Array|string} servers The DNS server address(es). 
*/
dns.DnsClient = function(servers) {
  var that = this;

  var wrapIpAddresses = function(addresses) {
    if (typeof addresses === 'string') {
      return [new java.net.InetSocketAddress(java.net.InetAddress.getByName(addresses), 53)];
    } else {
      // TODO: Be smarter about what's passed in
      return [addresses];
    }
  };

  var mappedHostAddressConverter = function(addresses) {
    var addrs = [];
    for (var i = 0; i < addresses.size(); i++) {
      addrs[i] = addresses.get(i).getHostAddress();
    }
    return addrs;
  };

  /** 
   * Represents a DNS MX record.
   *
   * @typedef {{}} MxRecord 
   * @property {number} priority The record priority
   * @property {string} name The record name
   * */
  var mappedMxConverter = function(records) {
    var recs = [];
    for (var i = 0; i < records.size(); i++) {
      recs[i] = {
        priority: records.get(i).priority(),
        name: records.get(i).name()
      };
    }
    return recs;
  };

  /** 
   * Represents a DNS SRV record
   * @typedef {{}} SrvRecord 
   * @property {number} priority The record priority
   * @property {number} weight The record weight
   * @property {number} port The record port
   * @property {string} name The record name
   * @property {string} protocol The record protocol
   * @property {string} service The record service
   * @property {string} target The record target
   * */
  var mappedSrvConverter = function(records) {
    var recs = [];
    for (var i = 0; i < records.size(); i++) {
      var record = records.get(i);
      recs[i] = {
        priority: record.priority(),
        weight: record.weight(),
        port: record.port(),
        name: record.name(),
        protocol: record.protocol(),
        service: record.service(),
        target: record.target()
      };
    }
    return recs;
  };

  /**
   * Lookup a server address
   * @param {string} name The server name to be looked up
   * @param {ResultHandler} handler The handler to be called when the lookup has completed.
   * The result parameter provided to the handler is a string address.
   * @returns {module:vertx/dns.DnsClient}
   *
   * @example
   * // use google dns
   * var client = dns.createClient(['8.8.8.8', '8.8.4.4']); 
   *
   * client.lookup('vertx.io', function(err, address) {
   *   if (err) {
   *     console.log("Can't find the server");
   *   } else {
   *     console.log("Address: " + address);
   *   }
   * }
   */
  this.lookup = function(name, handler) {
    __jClient.lookup(name, helpers.adaptAsyncResultHandler(handler, function(address) { return address.getHostAddress(); }));
    return that;
  };

  /**
   * Look up the IPv4 address for name
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with a string address
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.lookup4 = function(name, handler) {
    __jClient.lookup4(name, helpers.adaptAsyncResultHandler(handler, function(address) { return address.getHostAddress(); }));
    return that;
  };

  /**
   * Look up the IPv6 address for name
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with a string address
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.lookup6 = function(name, handler) {
    __jClient.lookup6(name, helpers.adaptAsyncResultHandler(handler, function(address) { return address.getHostAddress(); }));
    return that;
  };

  /**
   * Try to resolve all NS records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of string addresses
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveNS = function(name, handler) {
    __jClient.resolveNS(name, helpers.adaptAsyncResultHandler(handler, function(list) { return list.toArray(); }));
    return that;
  };

  /**
   * Try to resolve all TXT records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of string TXT records
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveTXT = function(name, handler) {
    __jClient.resolveTXT(name, helpers.adaptAsyncResultHandler(handler, function(list) { return list.toArray(); }));
    return that;
  };

  /**
   * Try to resolve all MX records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of MxRecord objects
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveMX = function(name, handler) {
    __jClient.resolveMX(name, helpers.adaptAsyncResultHandler(handler, mappedMxConverter));
    return that;
  };

  /**
   * Try to resolve all A records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of Strings
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveA = function(name, handler) {
    __jClient.resolveA(name, helpers.adaptAsyncResultHandler(handler, mappedHostAddressConverter));
    return that;
  };

  /**
   * Try to resolve all AAAA records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of Strings
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveAAAA = function(name, handler) {
    __jClient.resolveAAAA(name, helpers.adaptAsyncResultHandler(handler, mappedHostAddressConverter));
    return that;
  };

  /**
   * Try to resolve all AAAA records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of Strings
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveCNAME = function(name, handler) {
    __jClient.resolveCNAME(name, helpers.adaptAsyncResultHandler(handler, function(list) { return list.toArray(); }));
    return that;
  };

  /**
   * Try to resolve the PTR record for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with a string
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolvePTR = function(name, handler) {
    __jClient.resolvePTR(name, helpers.adaptAsyncResultHandler(handler));
    return that;
  };

  /**
   * Try to resolve all SRV records for the given name.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with an array of SRV records
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.resolveSRV = function(name, handler) {
    __jClient.resolveSRV(name, helpers.adaptAsyncResultHandler(handler, mappedSrvConverter));
    return that;
  };

  /**
   * Try to do a reverse lookup of an ipaddress. This is basically the same as
   * doing trying to resolve a PTR record but allows you to just pass in the
   * ipaddress and not a valid ptr query string.
   * @param {string} name
   * @param {ResultHandler} handler The handler is called with a string
   * when the lookup completes.
   * @returns {module:vertx/dns.DnsClient}
   */
  this.reverseLookup = function(name, handler) {
    __jClient.reverseLookup(name, helpers.adaptAsyncResultHandler(handler, function(address) { return address.getHostName(); }));
    return that;
  };

  var __jClient = __jvertx.createDnsClient(wrapIpAddresses(servers));
};

module.exports = dns;

