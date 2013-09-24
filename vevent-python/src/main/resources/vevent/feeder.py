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
import net.kuujo.vine.feeder.UnreliableFeeder
import net.kuujo.vine.feeder.ReliableFeeder
import net.kuujo.vine.feeder.RemoteFeeder
import net.kuujo.vine.feeder.RemoteFeederServer
import net.kuujo.vine.feeder.ReliableExecutor
import net.kuujo.vine.feeder.RemoteExecutor
import net.kuujo.vine.feeder.RemoteExecutorServer
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.Handler
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
from core.javautils import map_from_java, map_to_java

class _AbstractFeeder(object):
  """
  An abstract vine feeder.
  """
  def __init__(self, context):
    if self._handlercls is not None:
      self._feeder = self._handlercls(context._context, org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def feed_queue_full(self):
    return self._feeder.feedQueueFull()

  def feed(self, data, handler=None):
    if handler is None:
      self._feeder.feed(map_to_java(data))
    else:
      self._feeder.feed(map_to_java(data), FeedHandler(handler))
    return self

  def drain_handler(self, handler):
    self._executor.drainHandler(DrainHandler(handler))

class UnreliableFeeder(object):
  """
  An unreliable vine feeder.
  """
  _handlercls = net.kuujo.vine.feeder.UnreliableFeeder

class ReliableFeeder(object):
  """
  A reliable vine feeder.
  """
  _handlercls = net.kuujo.vine.feeder.ReliableFeeder

class RemoteFeeder(_AbstractFeeder):
  """
  A remote vine feeder.
  """
  _handlercls = net.kuujo.vine.feeder.RemoteFeeder

class RemoteFeederServer(object):
  """
  A remote feeder server.
  """
  def __init__(self, context):
    self._server = net.kuujo.vine.feeder.RemoteFeederServer(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def listen(self, address):
    self._server.listen(address)

  @property
  def address(self):
    return self._server.address()

  def close(self):
    self._server.close()

class FeedHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A feed handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None)
    else:
      self.handler(result.cause())

class _AbstractExecutor(object):
  """
  An abstract vine executor.
  """
  def __init__(self, context):
    if self._handlercls is not None:
      self._executor = self._handlercls(context._context, org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def feed_queue_full(self):
    return self._executor.feedQueueFull()

  def execute(self, data, handler, timeout=None):
    if timeout is None:
      self._executor.execute(map_to_java(data), ExecutorHandler(handler))
    else:
      self._executor.execute(map_to_java(data), timeout, ExecuteHandler(handler))
    return self

  def drain_handler(self, handler):
    self._executor.drainHandler(DrainHandler(handler))

class ReliableExecutor(_AbstractExecutor):
  """
  A reliable vine executor.
  """
  _handlercls = net.kuujo.vine.feeder.ReliableExecutor

class RemoteExecutor(_AbstractExecutor):
  """
  A remote vine executor.
  """
  _handlercls = net.kuujo.vine.feeder.RemoteExecutor

class RemoteExecutorServer(object):
  """
  A remote executor server.
  """
  def __init__(self, context):
    self._server = net.kuujo.vine.feeder.RemoteExecutorServer(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def listen(self, address):
    self._server.listen(address)

  @property
  def address(self):
    return self._server.address()

  def close(self):
    self._server.close()

class ExecuteHandler(org.vertx.java.core.AsyncResultHandler):
  """
  An execute handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None, map_from_java(result.result()))
    else:
      self.handler(result.cause(), None)

class DrainHandler(org.vertx.java.core.Handler):
  """
  A drain handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    self.handler()
