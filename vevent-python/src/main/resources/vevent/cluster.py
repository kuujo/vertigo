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
from context import NetworkContext
import net.kuujo.vevent.LocalCluster
import net.kuujo.vevent.ViaCluster
import org.vertx.java.core.AsyncResultHandler
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
from core.javautils import map_from_java, map_to_java

class _AbstractCluster(object):
  """
  An abstract cluster.
  """
  _handlercls = None

  def __init__(self):
    if self._handlercls is not None:
      self._cluster = self._handlercls(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)

  def deploy(self, definition, handler):
    """
    Deploys a network.
    """
    self._cluster.deploy(definition._def, DeployHandler(handler))

  def shutdown(self, context, handler=None):
    """
    Shuts down a network.
    """
    if handler is not None:
      self._cluster.shutdown(context._context, ShutdownHandler(handler))
    else:
      self._cluster.shutdown(context._context)

class DeployHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A deployment handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.failed():
      self.handler(result.cause(), None)
    else:
      self.handler(None, NetworkContext(result.result()))

class ShutdownHandler(org.vertx.java.core.AsyncResultHandler):
  """
  A shutdown handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.failed():
      self.handler(result.cause())
    else:
      self.handler(None)
