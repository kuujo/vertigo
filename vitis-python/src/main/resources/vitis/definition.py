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
import net.kuujo.vitis.definition.NetworkDefinition
import net.kuujo.vitis.definition.NodeDefinition
import net.kuujo.vitis.grouping.FieldsGrouping
import net.kuujo.vitis.grouping.RandomGrouping
import net.kuujo.vitis.grouping.RoundGrouping

class NetworkDefinition(object):
  """
  A network definition.
  """
  def __init__(self, address=None, **options):
    self._def = net.kuujo.vitis.definition.NetworkDefinition()
    if address is not None:
      self._def.setAddress(address)
    for key, value in options.iteritems():
      self._def.setOption(key, value)

  def get_address(self):
    return self._def.address()

  def set_address(self, address):
    self._def.setAddress(address)

  address = property(get_address, set_address)

  def from_root(self, name, main=None, workers=1, grouping=None, **options):
    node = NodeDefinition(name, main, workers, grouping, **options)
    self._def.fromRoot(component._def)
    return node

  @property
  def options(self):
    return Options(self)

  class Options(object):
    def __init__(self, definition):
      self._def = definition

    def __setitem__(self, key, value):
      self._def._def.setOption(key, value)

    def __getitem__(self, key, default=None):
      if default is not None:
        return self._def._def.option(key, default)
      else:
        return self._def._def.option(key)

class NodeDefinition(object):
  """
  A node definition.
  """
  def __init__(self, name, main=None, workers=1, grouping=None, **options):
    self._def = net.kuujo.vitis.definition.NodeDefinition(name)
    if main is not None:
      self._def.setMain(main)
    self._def.setWorkers(workers)
    if grouping is not None:
      self._def.groupBy(grouping.__def)
    for key, value in options.iteritems():
      self._def.setOption(key, value)

  def get_name(self):
    return self._def.name()

  def set_name(self, name):
    self._def.setName(name)

  name = property(get_name, set_name)

  def get_main(self):
    return self._def.main()

  def set_main(self, main):
    self._def.setMain(main)

  main = property(get_main, set_main)

  def get_workers(self):
    return self._def.workers()

  def set_workers(self, workers):
    self._def.setWorkers(workers)

  workers = property(get_workers, set_workers)

  def get_heartbeat_interval(self):
    return self._def.heartbeatInterval()

  def set_heartbeat_interval(self, interval):
    self._def.setHeartbeatInterval(interval)

  heartbeat_interval = property(get_heartbeat_interval, set_heartbeat_interval)

  def group_by(self, grouping):
    self._def.groupBy(grouping.__def)

  def to_node(self, name, main=None, workers=1, **options):
    node = NodeDefinition(name, main, workers, **options)
    self._def.toNode(node.__def)
    return node

class Grouping(object):
  """
  A node grouping definition.
  """
  def __init__(self):
    self._def = None

class FieldsGrouping(Grouping):
  """
  A fields based grouping.
  """
  def __init__(self, field):
    self._def = net.kuujo.vitis.grouping.FieldsGrouping(field)

  def get_field(self):
    return self._def.field()

  def set_field(self, field):
    self._def.setField(field)

  field = property(get_field, set_field)

class RandomGrouping(Grouping):
  """
  A random grouping.
  """
  def __init__(self):
    self._def = net.kuujo.vitis.grouping.RandomGrouping()

class RoundGrouping(Grouping):
  """
  A round-robin grouping.
  """
  def __init__(self):
    self._def = net.kuujo.vitis.grouping.RoundGrouping()
