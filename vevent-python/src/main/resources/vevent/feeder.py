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
"""
Usage:

from vevent.feeder import BasicFeeder
feeder = BasicFeeder()

@feeder.feed_handler
def do_feed(feeder):
  feeder.feed({'id': 1, 'body': 'Hello world!'})

@feeder.ack_handler
def do_ack(data):
  if data['id'] == 1:
    print "Message 1 succeeded!"

@feeder.fail_handler
def do_fail(data):
  if data['id'] == 1:
    print "Message 1 failed!"
"""
import net.kuujo.vevent.feeder.UnreliableFeeder
import net.kuujo.vevent.feeder.ReliableFeeder
import net.kuujo.vevent.context.FeederContext
import org.vertx.java.core.Handler
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
from core.javautils import map_from_java, map_to_java

class _AbstractFeeder(object):
  """
  An abstract vine feeder.
  """
  def __init__(self):
    if self._handlercls is not None:
      self._feeder = self._handlercls(net.kuujo.vevent.context.FeederContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config()),
                                      org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def feed_handler(self, handler):
    """
    Registers a feeder feed handler.
    """
    self._feeder.feedHandler(FeedHandler(handler, self))
    return handler

  def ack_handler(self, handler):
    """
    Registers an ack handler.
    """
    self._feeder.ackHandler(JsonHandler(handler))
    return handler

  def fail_handler(self, handler):
    """
    Registers a fail handler.
    """
    self._feeder.failHandler(JsonHandler(handler))
    return handler

  def feed(self, data, handler=None, tag=None):
    if handler is None:
      if tag is not None:
        self._feeder.feed(map_to_java(data), tag)
      else:
        self._feeder.feed(map_to_java(data))
    else:
      if tag is not None:
        self._feeder.feed(map_to_java(data), tag, AckHandler(handler, self))
      else:
        self._feeder.feed(map_to_java(data), AckHandler(handler))
    return self

class UnreliableFeeder(object):
  """
  An unreliable network feeder.
  """
  _handlercls = net.kuujo.vevent.feeder.UnreliableFeeder

class ReliableFeeder(object):
  """
  A reliable network feeder.
  """
  _handlercls = net.kuujo.vevent.feeder.ReliableFeeder

class FeedHandler(org.vertx.java.core.Handler):
  """
  A feed handler.
  """
  def __init__(self, handler, feeder):
    self.handler = handler
    self.feeder = feeder

  def handle(self, feeder):
    self.handler(self.feeder)

class AckHandler(org.vertx.java.core.AsyncResultHandler):
  """
  An async result handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.failed():
      self.handler(result.cause())
    else:
      self.handler(None)

class JsonHandler(org.vertx.java.core.Handler):
  """
  A JSON ack or fail handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, json):
    self.handler(map_from_java(json))
