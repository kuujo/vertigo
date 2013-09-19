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
import net.kuujo.vine.local.LocalRoot
import net.kuujo.vine.remote.RemoteRoot
import org.vertx.java.core.Handler
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.platform.impl.JythonVerticleFactory
from core.javautils import map_to_java, map_from_java

class _AbstractRoot(object):
  """
  An abstract root implementation.
  """
  _handlercls = None

  def __init__(self):
    if self._handlercls is not None:
      self.__root = self._handlercls(org.vertx.java.platform.impl.JythonVerticleFactory.vertx, org.vertx.java.platform.impl.JythonVerticleFactory.container)

  def deploy(self, definition, handler, timeout=None):
    """
    Deploys a vine.
    """
    if timeout is not None:
      self.__root.deploy(definition.__def, timeout, DeployHandler(handler))
    else:
      self.__root.deploy(definition.__def, DeployHandler(handler))

class LocalRoot(_AbstractRoot):
  """
  A local root.
  """
  _handlercls = net.kuujo.vine.local.LocalRoot

class RemoteRoot(_AbstractRoot):
  """
  A remote root.
  """
  _handlercls = net.kuujo.vine.remote.RemoteRoot

class DeployHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A vine deployment handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None, result.result())
    else:
      self.handler(result.cause(), None)

class Feeder(object):
  """
  A vine feeder.
  """
  def __init__(self, feeder):
    if isinstance(feeder, basestring):
      self.__feeder = net.kuujo.vine.BasicFeeder(feeder, org.vertx.java.platform.impl.JythonVerticleFactory.vertx)
    else:
      self.__feeder = feeder

  def feed(self, data, handler=None, timeout=None):
    if handler is None:
      self.__feeder.feed(map_to_java(data))
    else:
      if timeout is None:
        self.__feeder.feed(map_to_java(data), FeedHandler(handler))
      else:
        self.__feeder.feed(map_to_java(data), timeout, FeedHandler(handler))

  def execute(self, data, handler, timeout=None):
    if timeout is None:
      self.__feeder.execute(map_to_java(data), ExecuteHandler(handler))
    else:
      self.__feeder.execute(map_to_java(data), timeout, ExecuteHandler(handler))

  def feed_queue_full(self):
    return self.__feeder.feedQueueFull()

  queue_full = property(feed_queue_full)

  def drain_handler(self, handler):
    self.__feeder.drainHandler(DrainHandler(handler))

class FeedHandler(org.vertx.java.core.AsyncResultHandler):
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None)
    else:
      self.handler(result.cause())

class ExecuteHandler(org.vertx.java.core.AsyncResultHandler):
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None, map_from_java(result.result().toMap()))
    else:
      self.handler(result.cause(), None)

class DrainHandler(org.vertx.java.core.Handler):
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    self.handler()
