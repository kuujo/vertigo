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
import net.kuujo.vertigo.context.WorkerContext
import net.kuujo.vertigo.component.worker.BasicWorker
import org.vertx.java.platform.impl.JythonVerticleFactory
import org.vertx.java.core.Handler
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.json.JsonObject
from messaging import Message
from core.javautils import map_from_java, map_to_java

class BasicWorker(object):
  """
  A basic worker instance.
  """
  def __init__(self, context=None):
    if context is not None:
      context = context._context
    else:
      context = net.kuujo.vertigo.context.WorkerContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())
    self._worker = net.kuujo.vertigo.component.worker.BasicWorker(
      org.vertx.java.platform.impl.JythonVerticleFactory.vertx,
      org.vertx.java.platform.impl.JythonVerticleFactory.container,
      context
    )

  def message_handler(self, handler):
    """
    Sets the worker message handler.
    """
    self._worker.messageHandler(MessageHandler(handler, self))
    return handler

  def start(self, handler=None):
    """
    Starts the worker.
    """
    if handler is not None:
      self._worker.start(StartHandler(handler, self))
    else:
      self._worker.start()

  def emit(self, data, parent=None, tag=None):
    """
    Emits data to all output streams.
    """
    if parent is not None:
      if tag is not None:
        self._worker.emit(self._convert_data(data), tag, parent._message)
      else:
        self._worker.emit(self._convert_data(data), parent._message)
    else:
      if tag is not None:
        self._worker.emit(self._convert_data(data), tag)
      else:
        self._worker.emit(self._convert_data(data))

  def ack(self, *messages):
    """
    Acknowledges a message.
    """
    if len(messages) == 1:
      self._worker.ack(messages[0]._message)
    else:
      self._worker.ack(*[messages[i]._message for i in range(len(messages))])

  def fail(self, *messages):
    """
    Fails a message.
    """
    if len(messages) == 1:
      self._worker.fail(messages[0]._message)
    else:
      self._worker.fail(*[messages[i]._message for i in range(len(messages))])

  def _convert_data(self, data):
    return org.vertx.java.core.json.JsonObject(map_to_java(data))

class StartHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A worker start handler.
  """
  def __init__(self, handler, worker):
    self.handler = handler
    self.worker = worker

  def handle(self, result):
    if result.failed():
      self.handler(result.cause(), self.worker)
    else:
      self.handler(None, self.worker)

class MessageHandler(org.vertx.java.core.Handler):
  """
  A message handler wrapper.
  """
  def __init__(self, handler, worker):
    self.handler = handler
    self.worker = worker

  def handle(self, message):
    self.handler(Message(message), self.worker)
