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
from worker import BasicWorker
from feeder import BasicFeeder, PollingFeeder, StreamFeeder

def create_basic_feeder():
  """
  Creates a basic feeder.
  """
  return BasicFeeder()

def create_polling_feeder():
  """
  Creates a polling feeder.
  """
  return PollingFeeder()

def create_stream_feeder():
  """
  Creates a stream feeder.
  """
  return StreamFeeder()

def create_worker():
  """
  Creates a basic worker.
  """
  return BasicWorker()
