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
from vertx.javautils import map_dict_to_java

class Seed(object):
  """
  A Vine seed instance.
  """
  seed = ReliableSeed()
  seed.setVertx(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)
  seed.setContainer(org.vertx.java.platform.impl.JythonVerticleFactory.container)
  seed.setContext(container.config())

  @classmethod
  def process(cls, processor):
    """
    Sets the seed processor callback.
    """
    cls.seed.messageHandler(MessageHandler(processor))

  @classmethod
  def emit(cls, data):
    """
    Emits data from the seed.
    """
    cls.seed.emit(map_dict_to_java(data))

  @classmethod
  def emit(cls, data, parent):
    """
    Emits data from the seed.
    """
    cls.seed.emit(map_dict_to_java(data), parent)

  @classmethod
  def ack(cls, message):
    """
    Acknowledges processing of a message.
    """
    cls.seed.ack(message)

  @classmethod
  def fail(cls, message):
    """
    Fails processing of a message.
    """
    cls.seed.fail(message)

class MessageHandler(org.vertx.java.core.Handler):
  """
  A message handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, message):
    self.handler(message)
