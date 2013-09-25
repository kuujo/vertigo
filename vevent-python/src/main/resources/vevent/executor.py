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
import net.kuujo.vevent.executor.UnreliableFeeder
import net.kuujo.vevent.executor.ReliableFeeder
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.core.Handler
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
from core.javautils import map_from_java, map_to_java

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
