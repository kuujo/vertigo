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
from core.javautils import map_from_java, map_to_java

class Message(object):
  """
  A seed message.
  """
  def __init__(self, message):
    self._message = message
    self._body = map_from_java(message.body().toMap())

  @property
  def id(self):
    return self._message.id()

  @property
  def source(self):
    return self._message.source()

  @property
  def parent(self):
    return self._message.parent()

  @property
  def body(self):
    return self._body

  @property
  def tag(self):
    return self._message.tag()

  def create_child(self, body, id=None, tag=None):
    """
    Creates a child message.
    """
    if id is not None:
      if tag is not None:
        return Message(self._message.createChild(id, org.vertx.java.core.json.JsonObject(map_to_java(body)), tag))
      else:
        return Message(self._message.createChild(id, org.vertx.java.core.json.JsonObject(map_to_java(body))))
    else:
      if tag is not None:
        return Message(self._message.createChild(org.vertx.java.core.json.JsonObject(map_to_java(body)), tag))
      else:
        return Message(self._message.createChild(org.vertx.java.core.json.JsonObject(map_to_java(body))))
