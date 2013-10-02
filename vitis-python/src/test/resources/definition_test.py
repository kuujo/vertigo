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
from test import TestCase, run_test
from vitis import create_network

class DefinitionTestCase(TestCase):
  """
  A definition test case.
  """
  def test_create_network(self):
    """
    Tests creating a network.
    """
    network = create_network("test")
    self.assert_equals("test", network.address)
    network.address = "foo"
    self.assert_equals("foo", network.address)
    network.enable_acking()
    self.assert_true(network.acking_enabled())
    network.disable_acking()
    self.assert_false(network.acking_enabled())
    network.num_ackers = 10
    self.assert_equals(10, network.num_ackers)
    network.ack_expire = 50000
    self.assert_equals(50000, network.ack_expire)
    node = network.from_verticle('test_feeder_verticle', main='test_feeder_verticle.py')
    self.assert_equals('test_feeder_verticle', node.name)
    self.assert_equals('test_feeder_verticle.py', node.main)
    node.workers = 4
    self.assert_equals(4, node.workers)
    node2 = node.to_verticle('test_worker_verticle')
    node2.main = 'test_worker_verticle.py'
    self.assert_equals('test_worker_verticle.py', node2.main)
    self.complete()

run_test(DefinitionTestCase())
