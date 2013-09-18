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
import org.vertx.java.platform.impl.JythonVerticleFactory
import org.vertx.java.core.Handler
import com.blankstyle.vine.seed.BasicSeed
import com.blankstyle.vine.seed.ReliableSeed
import com.blankstyle.vine.messaging.JsonMessage
from core.javautils import map_from_java, map_to_java

class _AbstractSeed(object):
  """
  An abstract seed instance.
  """
  _handlercls = None

  def __init__(self):
    if self._handlercls is not None:
      self.__seed = self._handlercls()
      self.__seed.setVertx(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)
      self.__seed.setContainer(org.vertx.java.platform.impl.JythonVerticleFactory.container)
      self.__seed.setContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())

  def data_handler(self, handler):
    """
    Sets the seed data handler.
    """
    self.__seed.dataHandler(DataHandler(handler))
    return handler

  def start(self):
    """
    Starts the seed.
    """
    self.__seed.start()

  def emit(self, *data):
    """
    Emits data to all output streams.
    """
    if len(data) == 1:
      self.__seed.emit(map_to_java(data[0]))
    else:
      self.__seed.emit(*[map_to_java(data[i]) for i in range(len(data))])

  def emitto(self, seed, *data):
    """
    Emits data to a specific output stream.
    """
    if len(data) == 1:
      self.__seed.emitTo(seed, data[0])
    else:
      self.__seed.emitTo(seed, *[map_to_java(data[i]) for i in range(len(data))])

  def ack(self, *data):
    """
    Acknowledges a message.
    """
    if len(data) == 1:
      self.__seed.ack(data[0]._original)
    else:
      self.__seed.ack(*[data[i]._original for i in range(len(data))])

  def fail(self, *data):
    """
    Fails a message.
    """
    if len(data) == 1:
      self.__seed.fail(data[0].__message)
    else:
      self.__seed.fail(*[data[i].__message for i in range(len(data))])

class BasicSeed(_AbstractSeed):
  """
  A basic seed instance.
  """
  _handlercls = com.blankstyle.vine.seed.BasicSeed

class ReliableSeed(_AbstractSeed):
  """
  A reliable seed instance.
  """
  _handlercls = com.blakstyle.vine.seed.ReliableSeed

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
    self.__message = message
    dict.__init__(self, data)
