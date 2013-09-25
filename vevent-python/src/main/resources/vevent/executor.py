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
import net.kuujo.vevent.executor.BasicExecutor
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.Handler
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
from core.javautils import map_from_java, map_to_java

class _AbstractExecutor(object):
  """
  An abstract executor.
  """
  _handlercls = None

  def __init__(self):
    if self._handlercls is not None:
      self._executor = self._handlercls(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def execute_handler(self, handler):
    """
    Registers an executor handler.
    """
    self._executor.executeHandler(ExecutorHandler(handler, self))
    return handler

  def full_handler(self, handler):
    """
    Registers a queue full handler.
    """
    self._executor.fullHandler(ExecutorHandler(handler, self))
    return handler

  def drain_handler(self, handler):
    """
    Registers a queue drain handler.
    """
    self._executor.drainHandler(ExecutorHandler(handler, self))
    return handler

  def execute(self, args, handler, tag=None, timeout=0):
    """
    Executes the network.
    """
    if tag is not None:
      self._executor.execute(map_to_java(args), tag, timeout, ResultHandler(handler))
    else:
      self._executor.execute(map_to_java(args), timeout, ResultHandler(handler))

class BasicExecutor(_AbstractExecutor):
  """
  A basic executor.
  """
  _handlercls = net.kuujo.vevent.executor.BasicExecutor

class ExecutorHandler(org.vertx.java.core.Handler):
  """
  An execute handler.
  """
  def __init__(self, handler, executor):
    self.handler = handler
    self.executor = executor

  def handle(self, executor):
    self.handler(executor)

class ResultHandler(org.vertx.java.core.AsyncResultHandler):
  """
  An asynchronous result handler handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None, map_from_java(result.result()))
    else:
      self.handler(result.cause(), None)
