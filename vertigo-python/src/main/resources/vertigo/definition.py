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
import net.kuujo.vertigo.definition.NetworkDefinition
import net.kuujo.vertigo.definition.ComponentDefinition
import net.kuujo.vertigo.grouping.FieldsGrouping
import net.kuujo.vertigo.grouping.RandomGrouping
import net.kuujo.vertigo.grouping.RoundGrouping
import net.kuujo.vertigo.grouping.AllGrouping
import net.kuujo.vertigo.filter.TagFilter
import net.kuujo.vertigo.filter.SourceFilter
import org.vertx.java.core.json.JsonObject
from core.javautils import map_from_java, map_to_java

class NetworkDefinition(object):
  """
  A network definition.
  """
  def __init__(self, address=None, **options):
    self._def = net.kuujo.vertigo.definition.NetworkDefinition()
    if address is not None:
      self._def.setAddress(address)
    for key, value in options.iteritems():
      self._def.setOption(key, value)

  def get_address(self):
    return self._def.address()

  def set_address(self, address):
    self._def.setAddress(address)

  address = property(get_address, set_address)

  def acking_enabled(self):
    return self._def.ackingEnabled()

  def enable_acking(self):
    self._def.enableAcking()
    return self

  def disable_acking(self):
    self._def.disableAcking()
    return self

  def set_num_ackers(self, num):
    self._def.setNumAckers(num)

  def get_num_ackers(self):
    return self._def.numAckers()

  num_ackers = property(get_num_ackers, set_num_ackers)

  def set_ack_expire(self, expire):
    self._def.setAckExpire(expire)

  def get_ack_expire(self):
    return self._def.ackExpire()

  ack_expire = property(get_ack_expire, set_ack_expire)

  def from_verticle(self, name, main=None, workers=1, config=None, grouping=None):
    if config is None:
      config = {}
    definition = self._def.fromVerticle(name, main, org.vertx.java.core.json.JsonObject(map_to_java(config)), workers)
    if grouping is not None:
      definition.groupBy(grouping._def)
    return ComponentDefinition(definition)

  def from_module(self, name, module=None, workers=1, config=None, grouping=None):
    if config is None:
      config = {}
    definition = self._def.fromModule(name, module, org.vertx.java.core.json.JsonObject(map_to_java(config)), workers)
    if grouping is not None:
      definition.groupBy(grouping._def)
    return ComponentDefinition(definition)

  @property
  def options(self):
    return Options(self)

class ComponentDefinition(object):
  """
  A component definition.
  """
  VERTICLE = "verticle"
  MODULE = "module"

  def __init__(self, definition):
    self._def = definition

  def get_type(self):
    return self._def.type()

  def set_type(self, type):
    self._def.setType(type)

  type = property(get_type, set_type)

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

  def get_module(self):
    return self._def.module()

  def set_module(self, module):
    self._def.setModule(module)

  module = property(get_module, set_module)

  def get_config(self):
    return map_from_java(self._def.config().toMap())

  def set_config(self, config):
    self._def.setConfig(org.vertx.java.core.json.JsonObject(map_to_java(config)))

  config = property(get_config, set_config)

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
    self._def.groupBy(grouping._def)
    return self

  def filter_by(self, filter):
    self._def.filterBy(filter._def)
    return self

  def to_verticle(self, name, main=None, workers=1, config=None, grouping=None):
    if config is None:
      config = {}
    definition = self._def.toVerticle(name, main, org.vertx.java.core.json.JsonObject(map_to_java(config)), workers)
    if grouping is not None:
      definition.groupBy(grouping._def)
    return ComponentDefinition(definition)

  def to_module(self, name, module=None, workers=1, config=None, grouping=None):
    if config is None:
      config = {}
    definition = self._def.toModule(name, module, org.vertx.java.core.json.JsonObject(map_to_java(config)), workers)
    if grouping is not None:
      definition.groupBy(grouping._def)
    return ComponentDefinition(definition)


class Options(object):
  """
  Definition options dictionary.
  """
  def __init__(self, definition):
    self._def = definition

  def __setitem__(self, key, value):
    self._def._def.setOption(key, value)

  def __getitem__(self, key):
    return self._def._def.option(key)

class Grouping(object):
  """
  A component grouping definition.
  """
  def __init__(self):
    self._def = None

class FieldsGrouping(Grouping):
  """
  A fields based grouping.
  """
  def __init__(self, field):
    self._def = net.kuujo.vertigo.grouping.FieldsGrouping(field)

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
    self._def = net.kuujo.vertigo.grouping.RandomGrouping()

class RoundGrouping(Grouping):
  """
  A round-robin grouping.
  """
  def __init__(self):
    self._def = net.kuujo.vertigo.grouping.RoundGrouping()

class AllGrouping(Grouping):
  """
  An all grouping.
  """
  def __init__(self):
    self._def = net.kuujo.vertigo.grouping.AllGrouping()

class Filter(object):
  """
  A component filter definition.
  """
  def __init__(self):
    self._def = None

class TagFilter(Filter):
  """
  A tag filter.
  """
  def __init__(self, *tags):
    self._def = net.kuujo.vertigo.filter.TagFilter(*tags)

class SourceFilter(Filter):
  """
  A source filter.
  """
  def __init__(self, source):
    self._def = net.kuujo.vertigo.filter.SourceFilter(source)
