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
import net.kuujo.vevent.definition.NetworkDefinition
import net.kuujo.vevent.definition.NodeDefinition
import net.kuujo.vevent.grouping.FieldsGrouping
import net.kuujo.vevent.grouping.RandomGrouping
import net.kuujo.vevent.grouping.RoundGrouping

class NetworkDefinition(object):
  """
  A network definition.
  """
  def __init__(self, address=None, **options):
    self.__def = net.kuujo.vevent.definition.NetworkDefinition()
    if address is not None:
      self.__def.setAddress(address)
    for key, value in options.iteritems():
      self.__def.setOption(key, value)

  def get_address(self):
    return self.__def.getAddress()

  def set_address(self, address):
    self.__def.setAddress(address)

  address = property(get_address, set_address)

  def get_queuesize(self):
    return self.__def.getMaxQueueSize()

  def set_queuesize(self, queuesize):
    self.__def.setMaxQueueSize(queuesize)

  queuesize = property(get_queuesize, set_queuesize)

  def get_message_timeout(self):
    return self.__def.getMessageTimeout()

  def set_message_timeout(self, timeout):
    self.__def.setMessageTimeout(timeout)

  message_timeout = property(get_message_timeout, set_message_timeout)

  def feed(self, name, main=None, workers=1, grouping=None, **options):
    seed = NodeDefinition(name, main, workers, grouping, **options)
    self.__def.feed(seed.__def)
    return seed

  @property
  def options(self):
    return Options(self)

  class Options(object):
    def __init__(self, definition):
      self.__def = definition

    def __setitem__(self, key, value):
      self.__def.__def.setOption(key, value)

    def __getitem__(self, key):
      self.__def.__def.getOption(key)

class NodeDefinition(object):
  """
  A network node definition.
  """
  def __init__(self, name, main=None, workers=1, grouping=None, **options):
    self.__def = net.kuujo.vevent.definition.NodeDefinition(name)
    if main is not None:
      self.__def.setMain(main)
    self.__def.setWorkers(workers)
    if grouping is not None:
      self.__def.setGrouping(grouping.__def)
    for key, value in options.iteritems():
      self.__def.setOption(key, value)

  def get_name(self):
    return self.__def.getName()

  def set_name(self, name):
    self.__def.setName(name)

  name = property(get_name, set_name)

  def get_main(self):
    return self.__def.getMain()

  def set_main(self, main):
    self.__def.setMain(main)

  main = property(get_main, set_main)

  def get_workers(self):
    return self.__def.getWorkers()

  def set_workers(self, workers):
    self.__def.setWorkers(workers)

  workers = property(get_workers, set_workers)

  def get_heartbeat_interval(self):
    return self.__def.getHeartbeatInterval()

  def set_heartbeat_interval(self, interval):
    self.__def.setHeartbeatInterval(interval)

  heartbeat_interval = property(get_heartbeat_interval, set_heartbeat_interval)

  def group_by(self, grouping):
    self.__def.groupBy(grouping.__def)

  def to(self, name, main=None, workers=1, **options):
    seed = NodeDefinition(name, main, workers, **options)
    self.__def.to(seed.__def)
    return seed

class Grouping(object):
  """
  A node grouping definition.
  """
  def __init__(self):
    self.__def = None

class FieldsGrouping(Grouping):
  """
  A fields based grouping.
  """
  def __init__(self, field):
    self.__def = net.kuujo.vevent.grouping.FieldsGrouping(field)

  def get_field(self):
    return self.__def.getField()

  def set_field(self, field):
    self.__def.setField(field)

  field = property(get_field, set_field)

class RandomGrouping(Grouping):
  """
  A random grouping.
  """
  def __init__(self):
    self.__def = net.kuujo.vevent.grouping.RandomGrouping()

class RoundGrouping(Grouping):
  """
  A round-robin grouping.
  """
  def __init__(self):
    self.__def = net.kuujo.vevent.grouping.RoundGrouping()
