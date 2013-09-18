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
import com.blankstyle.vine.seed.ReliableSeed
import com.blankstyle.vine.messaging.JsonMessage
from core.javautils import map_dict_to_java

class Seed(object):
  """
  A Vine seed instance.
  """
  seed = ReliableSeed()
  seed.setVertx(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)
  seed.setContainer(org.vertx.java.platform.impl.JythonVerticleFactory.container)
  seed.setContext(container.config())

  @classmethod
  def process(cls, handler):
    """
    Sets the seed processor callback.
    """
    def handle(message):
      cls.current_message = message
      handler(message.body())
    cls.seed.messageHandler(MessageHandler(handler))

  @classmethod
  def processor(cls, handler):
    """
    Decorator for setting the seed processor callback.
    """
    cls.process(handler)
    return handler

  @classmethod
  def emit(cls, data, parent=None, ack=None):
    """
    Emits data from the seed.
    """
    if parent is None:
      if ack is None:
        cls.seed.emit(map_dict_to_java(data))
      else:
        cls.seed.emit(map_dict_to_java(data), AckHandler(ack))
    else:
      if ack is None:
        cls.seed.emit(map_dict_to_java(data), parent)
      else:
        cls.seed.emit(map_dict_to_java(data), parent, AckHandler(ack))

  @classmethod
  def emitto(cls, seedname, data, parent=None, ack=None):
    """
    Emits data to a specific seed.
    """
    if parent is None:
      if ack is None:
        cls.seed.emitTo(seedname, map_dict_to_java(data))
      else:
        cls.seed.emitTo(seedname, map_dict_to_java(data), AckHandler(ack))
    else:
      if ack is None:
        cls.seed.emitTo(seedname, map_dict_to_java(data), parent)
      else:
        cls.seed.emitTo(seedname, map_dict_to_java(data), parent, AckHandler(ack))

class MessageHandler(org.vertx.java.core.Handler):
  """
  A message handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, message):
    self.handler(message)

class AckHandler(org.vertx.java.core.Handler):
  """
  An ack handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    self.handler(None if result.succeeded() else result.cause())
