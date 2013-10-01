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
import net.kuujo.vevent.context.WorkerContext
import net.kuujo.vevent.node.worker.BasicWorker
import org.vertx.java.platform.impl.JythonVerticleFactory
import org.vertx.java.core.Handler
from messaging import Message
from core.javautils import map_from_java, map_to_java

class BasicWorker(object):
  """
  A basic worker instance.
  """
  def __init__(self):
    self._worker = net.kuujo.vevent.node.worker.BasicWorker(
      org.vertx.java.platform.impl.JythonVerticleFactory.vertx,
      org.vertx.java.platform.impl.JythonVerticleFactory.container,
      net.kuujo.vevent.context.WorkerContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())
    )

  def data_handler(self, handler):
    """
    Sets the seed data handler.
    """
    self._worker.dataHandler(DataHandler(handler))
    return handler

  def start(self):
    """
    Starts the seed.
    """
    self._worker.start()

  def emit(self, data, parent=None, tag=None):
    """
    Emits data to all output streams.
    """
    if parent is not None:
      if tag is not None:
        self._worker.emit(data, tag, parent)
      else:
        self._worker.emit(data, parent)
    else:
      if tag is not None:
        self._worker.emit(data, tag)
      else:
        self._worker.emit(data)

  def ack(self, *data):
    """
    Acknowledges a message.
    """
    if len(data) == 1:
      self._worker.ack(data[0]._message)
    else:
      self._worker.ack(*[data[i]._message for i in range(len(data))])

  def fail(self, *data):
    """
    Fails a message.
    """
    if len(data) == 1:
      self._worker.fail(data[0]._message)
    else:
      self._worker.fail(*[data[i]._message for i in range(len(data))])

class DataHandler(org.vertx.java.core.Handler):
  """
  A data handler wrapper.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, message):
    self.handler(Message(map_from_java(message.toMap()), message))
