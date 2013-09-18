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
import com.blankstyle.vine.local.LocalRoot
import com.blankstyle.vine.remote.RemoteRoot
import org.vertx.java.platform.impl.JythonVerticleFactory

class _AbstractRoot(object):
  """
  An abstract root implementation.
  """
  _handlercls = None

  def __init__(self):
    if self._handlercls is not None:
      self.__root = self._handlercls(org.vertx.java.platform.impl.JythonVerticleFactory.vertx, org.vertx.java.platform.impl.JythonVerticleFactory.container)

  def deploy(self, definition, handler, timeout=None):
    """
    Deploys a vine.
    """
    if timeout is not None:
      self.__root.deploy(definition.__def, timeout, DeployHandler(handler))
    else:
      self.__root.deploy(definition.__def, DeployHandler(handler))

class LocalRoot(_AbstractRoot):
  """
  A local root.
  """
  _handlercls = com.blankstyle.vine.local.LocalRoot

class RemoteRoot(_AbstractRoot):
  """
  A remote root.
  """
  _handlercls = com.blankstyle.vine.remote.RemoteRoot

class DeployHandler(org.vertx.java.core.Handler):
  """
  A vine deployment handler.
  """
  def __init__(self, handler):
    self.handler = handler

  def handle(self, result):
    if result.succeeded():
      self.handler(None, result.result())
    else:
      self.handler(result.cause(), None)
