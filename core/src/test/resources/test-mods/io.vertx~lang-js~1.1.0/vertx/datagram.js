/**
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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


var console = require("vertx/console");

/**
 * This module provides classes for UDP networking.
 *
 * @module vertx/datagram
 */


var streams        = require('vertx/streams');
var NetworkSupport = require('vertx/network_support');
var helpers        = require("vertx/helpers.js");
var Buffer         = require('vertx/buffer');

/**
 * <p>
 * A socket which can be used to send and receive {@linkcode module:vertx/datagram~DatagramPacket}s 
 * </p>
 *
 * <p>
 * UDP is connectionless which means you are not connected to the remote peer
 * in a persistent way. Because of this you have to supply the address and port
 * of the remote peer when sending data.  You can send data to ipv4 or ipv6
 * addresses, which also include multicast addresses.
 * </p>
 * @constructor
 * @augments module:vertx/streams~ReadSupport
 * @augments module:vertx/streams~DrainSupport
 * @augments module:vertx/network_support
 * @param {boolean} ipv4 If true, use IPv4 addresses, if false use IPv6 addresses,
 * if undefined, use the operating system default.
 */
var DatagramSocket = function(ipv4) {
  var family = null;
    family = org.vertx.java.core.datagram.InternetProtocolFamily.IPv4;
  if(ipv4 === true) {
    family = org.vertx.java.core.datagram.InternetProtocolFamily.IPv4;
  } else if (ipv4 === false) {
    family = org.vertx.java.core.datagram.InternetProtocolFamily.IPv6;
  }

  var _localAddress;
  var _address  = {
    port: undefined,
    address: undefined,
    family: family.toString()
  };
  var _delegate     = __jvertx.createDatagramSocket(family);
  var _that         = this;

  streams.ReadSupport.call(this, _delegate);
  streams.DrainSupport.call(this, _delegate);
  NetworkSupport.call(this, _delegate);

  /**
   * Writes a packet to the host and port provided, calling the handler
   * when the write has completed. If an encoding is supplied, the packet's
   * toString function is called and the specified encoding is used.
   *
   * @param {string} host the network host address of the remote peer
   * @param {number} port the network port of the remote peer
   * @param {string|module:vertx/buffer} packet the packet to write
   * @param {ResultHandler} handler the handler to notify when the write completes
   * @param {string} [encoding] the encoding to use when writing a string packet
   */
  this.send = function(host, port, packet, handler, encoding) {
    if (encoding !== undefined) {
      _delegate.send(packet.toString(), encoding, host, port, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    } else {
      if (packet instanceof Buffer) {
        packet = packet._to_java_buffer();
      }
      _delegate.send(packet, host, port, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    }
    return this;
  };

  /**
   * Get or set the SO_BROADCAST option
   * @param {boolean} [value] turns on or off the SO_BROADCAST option
   * @return {boolean|module:vertx/datagram~DatagramSocket} the SO_BROADCAST option as currently set or this
   */
  this.broadcast = function(value) {
    if (value !== undefined) {
      _delegate.setBroadcast(value);
      return this;
    }
    return _delegate.isBroadcast();
  };

  /**
   * Get or set the IP_MULTICAST_LOOP option
   * @param {boolean} [value] turns on or off the IP_MULTICAST_LOOP option
   * @return {boolean|module:vertx/datagram~DatagramSocket} the IP_MULTICAST_LOOP option as currently set or this
   */
  this.multicastLoopbackMode = function(value) {
    if (value !== undefined) {
      _delegate.setMulticastLoopbackMode(value);
      return this;
    }
    return _delegate.isMulticastLoopbackMode();
  };

  /**
   * Get or set the IP_MULTICAST_TTL option
   * @param {number} [value] the IP_MULTICAST_TTL value
   * @return {number|module:vertx/datagram~DatagramSocket} the IP_MULTICAST_TTL option as currently set or this
   */
  this.multicastTimeToLive = function(value) {
    if (value !== undefined) {
      _delegate.setMulticastTimeToLive(value);
      return this;
    }
    return _delegate.getMulticastTimeToLive();
  };

  /**
   * Get or set the IP_MULTICAST_IF option
   * @param {string} [value] the IP_MULTICAST_IF value
   * @return {string|module:vertx/datagram~DatagramSocket} the IP_MULTICAST_IF option as currently set or this
   */
  this.multicastNetworkInterface = function(value) {
    if (value !== undefined) {
      _delegate.setMulticastNetworkInterface(value);
      return this;
    }
    var iface = _delegate.getMulticastNetworkInterface();
    return iface ? iface : null;
  };

  /**
   * Close the {@linkcode module:vertx/datagram~DatagramSocket} asynchronously
   * and notify the handler when complete.
   * @param {ResultHandler} handler the handler to notify when close() has completed.
   */
  this.close = function(handler) {
    _delegate.close(helpers.adaptAsyncResultHandler(handler));
  };

  /** 
   * Get the local address of this socket. 
   * @return {} An object with the socket's address, port and IP family as properties
   */
  this.localAddress = function() {
    if (_localAddress === undefined) {
      return  undefined;
    }
    return _address;
  };

  /**
   * Joins a multicast group and listens for packets sent to it. The
   * {@linkcode ResultHandler} is notified once the operation completes.
   *
   * @param {string} address The address of the multicast group to join
   * @param {ResultHandler} handler The handler to notify when the operation has completed
   * @param {string} [source] The address of the source to which we'll listen for packets
   * @param {string} [networkInterface] The network interface on which to listen for packets
   * @return {module:vertx/datagram~DatagramSocket} this
   */
  this.listenMulticastGroup = function(address, handler, source, networkInterface) {
    if (networkInterface) {
      _delegate.listenMulticastGroup(address, networkInterface, source, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    } else {
      _delegate.listenMulticastGroup(address, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    }
    return this;
  };

  /**
   * Leaves a multicast group and stops listening for packets sent to it on the
   * given network interface. The {@linkcode
   *
   * @param {string} address The address of the multicast group to stop listening to
   * @param {ResultHandler} handler The handler to notify when the operation completes
   * @param {string} [source] The source address to stop listening to
   * @param {string} [networkInterface] The network interface this socket is currently listening on
   * @return {module:vertx/datagram~DatagramSocket} this
   */
  this.unlistenMulticastGroup = function(address, handler, source, networkInterface) {
    if (networkInterface) {
      _delegate.unlistenMulticastGroup(address, networkInterface, source, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    } else {
      _delegate.unlistenMulticastGroup(address, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    }
    return this;
  };

  /**
   * Blocks the given source address on the given network interface notifies
   * the handler when this operation has completed.
   *
   * @param {string} address The address of the multicast group on which the source is broadcasting
   * @param {ResultHandler} handler The handler to notify when the operation completes
   * @param {string} source The source address to block
   * @param {string} [networkInterface] The network interface this socket is currently listening on
   * @return {module:vertx/datagram~DatagramSocket} this
   */
  this.blockMulticastGroup = function(address, handler, networkInterface, source) {
    if (networkInterface && source) {
      _delegate.blockMulticastGroup(address, networkInterface, source, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    } else {
      _delegate.blockMulticastGroup(address, helpers.adaptAsyncResultHandler(handler, function() { return _that; }));
    }
    return this;
  };

  /**
   * Listens to broadcast messages on the given port and optional host address. The
   * handler is notified when the listen operation has completed.
   *
   * @param {number} port The port to listen on for incoming packets
   * @param {string} [host] The host address to listen on. Defaults to '0.0.0.0'.
   * @param {ResultHandler} [handler] The handler to notify when the listen operation completes
   */
  this.listen = function(port, host, handler) {
    if (typeof host == 'function') {
      handler = host;
      host = '0.0.0.0';
    }
    _delegate.listen(host, port, helpers.adaptAsyncResultHandler(handler, function() {
      _localAddress = _delegate.localAddress();
      _address.address = _localAddress.getHostString();
      _address.port = _localAddress.getPort();
      _address.family = family.toString();
      return _that; 
    }));
    return this;
  };

  /**
   * A <code>PacketHandler</code> is a {@linkcode Handler} that accepts a
   * {@linkcode module:vertx/datagram.DatagramPacket} as it's parameter.
   * @typedef {function} PacketHandler
   * @param {module:vertx/datagram.DatagramPacket} datagramPacket The received packet
   */

  /**
   * Set a {@linkcode PacketHandler} to be notified of incoming data. 
   *
   * @param {PacketHandler} handler The handler to be notified of incoming packets.
   */
  this.dataHandler = function(handler) {
    _delegate.dataHandler(function(packet) {
      handler(new DatagramPacket(packet));
    });
  };
};

/**
 * A received UDP datagram packet, with the received data and sender information.
 * @param {external:DatagramPacket} packet the Java delegate
 * @class
 * @property {object} sender The sender of the packet
 * @property {string} sender.host The packet sender's host address
 * @property {number} sender.port The packet sender's port number
 * @property {module:vertx/buffer} data The packet data that was sent
 */
var DatagramPacket = function(_delegate) {

  var _sender = null;
  var _data   = null;

  this.sender = function() {
    if (_sender === null) {
      _sender = {
        host: _delegate.sender().getAddress().getHostAddress(),
        port: _delegate.sender().getPort()
      };
    }
    return _sender;
  };

  this.data = function() {
    if (_data === null) {
      _data = new Buffer(_delegate.data());
    }
    return _data;
  };
};


module.exports.DatagramSocket = DatagramSocket;
module.exports.DatagramPacket = DatagramPacket;
module.exports.InternetProtocolFamily = org.vertx.java.core.datagram.InternetProtocolFamily;
