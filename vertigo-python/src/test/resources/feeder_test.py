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
from vertigo.cluster import LocalCluster, ViaCluster
import vertigo

class FeederTestCase(TestCase):
  """
  A feeder test case.
  """
  def _create_ack_network(self, feeder):
    network = vertigo.create_network()
    network.address = "test"
    network.enable_acking()
    network.from_verticle('test_feeder', feeder).to_verticle('test_worker', 'test_acking_worker.py')
    return network

  def _create_fail_network(self, feeder):
    network = create_network()
    network.address = "test"
    network.enable_acking()
    network.from_verticle('test_feeder', feeder).to_verticle('test_worker', 'test_failing_worker.py')
    return network

  def test_basic_feeder_ack(self):
    """
    Tests the basic feeder acking support.
    """
    network = self._create_ack_network('test_acking_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

  def test_basic_feeder_fail(self):
    """
    Tests the basic feeder fail support.
    """
    network = self._create_fail_network('test_failing_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

  def test_polling_feeder_ack(self):
    """
    Tests the polling feeder acking support.
    """
    network = self._create_ack_network('test_acking_polling_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

  def test_polling_feeder_fail(self):
    """
    Tests the polling feeder fail support.
    """
    network = self._create_fail_network('test_failing_polling_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

  def test_stream_feeder_ack(self):
    """
    Tests the stream feeder acking support.
    """
    network = self._create_ack_network('test_acking_stream_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

  def test_stream_feeder_fail(self):
    """
    Tests the stream feeder fail support.
    """
    network = self._create_fail_network('test_failing_stream_feeder.py')
    cluster = LocalCluster()
    def deploy_handler(error, context):
      self.assert_null(error)
      self.assert_not_null(context)
    cluster.deploy(network, deploy_handler)

run_test(FeederTestCase())
