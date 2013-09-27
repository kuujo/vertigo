# Copyright 2013 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
import net.kuujo.vevent.context.NetworkContext
import net.kuujo.vevent.node.BasicWorker
import net.kuujo.vevent.node.BasicFeeder
import net.kuujo.vevent.node.BasicPullFeeder
import net.kuujo.vevent.node.BasicStreamFeeder
import net.kuujo.vevent.messaging.JsonMessage
import org.vertx.java.platform.impl.JythonVerticleFactory
import org.vertx.java.core.Handler
from core.javautils import map_from_java, map_to_java

class Worker(object):
  """
  A worker instance.
  """
  _handlercls = None

  def __init__(self):
    self.__worker = net.kuujo.vevent.node.BasicWorker(
      org.vertx.java.platform.impl.JythonVerticleFactory.vertx,
      org.vertx.java.platform.impl.JythonVerticleFactory.container,
      net.kuujo.vevent.context.NetworkContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())
    )

  def data_handler(self, handler):
    """
    Sets the seed data handler.
    """
    self.__worker.dataHandler(DataHandler(handler))
    return handler

  def start(self):
    """
    Starts the seed.
    """
    self.__worker.start()

  def emit(self, data, parent=None, tag=None):
    """
    Emits data to all output streams.
    """
    if parent is not None:
      if tag is not None:
        self.__worker.emit(data, tag, parent)
      else:
        self.__worker.emit(data, parent)
    else:
      if tag is not None:
        self.__worker.emit(data, tag)
      else:
        self.__worker.emit(data)

  def ack(self, *data):
    """
    Acknowledges a message.
    """
    if len(data) == 1:
      self.__worker.ack(data[0]._message)
    else:
      self.__worker.ack(*[data[i]._message for i in range(len(data))])

  def fail(self, *data):
    """
    Fails a message.
    """
    if len(data) == 1:
      self.__worker.fail(data[0]._message)
    else:
      self.__worker.fail(*[data[i]._message for i in range(len(data))])

class DataHandler(org.vertx.java.core.Handler):
  """
  A data handler wrapper.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, message):
    self.handler(Message(map_from_java(message.toMap()), message))

class Message(dict):
  """
  A seed message.
  """
  def __init__(self, data, message):
    self._message = message
    dict.__init__(self, data)

class _AbstractFeeder(object):
  """
  An abstract feeder.
  """
  _handlercls = None

  def __init__(self):
    self._feeder = self._handlercls(
      org.vertx.java.platform.impl.JythonVerticleFactory.vertx,
      org.vertx.java.platform.impl.JythonVerticleFactory.container,
      net.kuujo.vevent.context.NetworkContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())
    )

  def feed(self, data, tag=None, handler=None, timeout=None):
    """
    Feeds data to the network.
    """
    self._feeder.feed(data, tag, timeout, FeedResultHandler(handler))
    return self

class BasicFeeder(_AbstractFeeder):
  """
  A basic feeder.
  """
  _handlercls = net.kuujo.vevent.node.BasicFeeder

class PullFeeder(_AbstractFeeder):
  """
  A pull feeder.
  """
  _handlercls = net.kuujo.vevent.node.BasicPullFeeder

  def feed_handler(self, handler):
    """
    Registers a feed handler.
    """
    self._feeder.feedHandler(FeedHandler(handler, self))
    return handler

class StreamFeeder(_AbstractFeeder):
  """
  A stream feeder.
  """
  _handlercls = net.kuujo.vevent.node.BasicStreamFeeder

  @property
  def queue_full(self):
    """
    Indicates whether the feed queue is full.
    """
    return self._feeder.feedQueueFull()

  def drain_handler(self, handler):
    """
    Registers a drain handler.
    """
    self._feeder.drainHandler(DrainHandler(handler))
    return handler

class FeedResultHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A feed result handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None)
    else:
      self.handler(result.cause())

class FeedHandler(org.vertx.java.core.Handler):
  """
  A feed handler.
  """
  def __init__(self, handler, feeder):
    self.handler = handler
    self.feeder = feeder

  def handle(self, feeder):
    self.handler(self.feeder)

class DrainHandler(org.vertx.java.core.Handler):
  """
  A queue drain handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, void):
    self.handler()
