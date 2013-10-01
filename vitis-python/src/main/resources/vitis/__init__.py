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
from definition import NetworkDefinition
from worker import BasicWorker
from feeder import BasicFeeder, PollingFeeder, StreamFeeder
from context import WorkerContext
import org.vertx.java.platform.impl.JythonVerticleFactory.container

def create_network(address=None, **options):
  """
  Creates a new network definition.
  """
  return NetworkDefinition(address, **options)

def create_basic_feeder(context=None):
  """
  Creates a basic feeder.
  """
  return BasicFeeder(context)

def create_polling_feeder(context=None):
  """
  Creates a polling feeder.
  """
  return PollingFeeder(context)

def create_stream_feeder(context=None):
  """
  Creates a stream feeder.
  """
  return StreamFeeder(context)

def create_worker(context=None):
  """
  Creates a basic worker.
  """
  return BasicWorker(context)

context = WorkerContext(org.vertx.java.platform.impl.JythonVerticleFactory.container.config())
