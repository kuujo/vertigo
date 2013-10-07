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
from definition import NetworkDefinition, ComponentDefinition

class _AbstractContext(object):
  """
  An abstract context.
  """
  def __init__(self, context):
    self._context = context

class NetworkContext(_AbstractContext):
  """
  A network context.
  """
  @property
  def address(self):
    return self._context.address()

  @property
  def audit_address(self):
    return self._context.auditAddress()

  @property
  def broadcast_address(self):
    return self._context.broadcastAddress()

  @property
  def definition(self):
    return NetworkDefinition(self._context.definition())

  @property
  def contexts(self):
    collection = self._context.contexts()
    iterator = collection.iterator()
    contexts = {}
    while iterator.hasNext():
      context = iterator.next()
      contexts[context.name()] = ComponentContext(context)
    return contexts

class ComponentContext(_AbstractContext):
  """
  A component context.
  """
  @property
  def address(self):
    return self._context.address()

  @property
  def context(self):
    return NetworkContext(self._context.context())

  @property
  def definition(self):
    return ComponentDefinition(self._context.definition())

  @property
  def workers(self):
    collection = self._context.workerContexts()
    iterator = collection.iterator()
    contexts = {}
    while iterator.hasNext():
      context = iterator.next()
      contexts[context.address()] = WorkerContext(context)
    return contexts

class WorkerContext(_AbstractContext):
  """
  A worker context.
  """
  @property
  def address(self):
    return self._context.address()

  @property
  def context(self):
    return ComponentContext(self._context.context())
