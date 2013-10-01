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
from vitis.cluster import LocalCluster, ViaCluster
from vitis import create_network

class ClusterTestCase(TestCase):
  """
  A cluster test case.
  """
  def _create_test_network(self):
    network = create_network()
    network.address = "test"
    network.enable_acking()
    network.from_root('test_feeder', 'test_feeder_verticle.py').to_node('test_worker', 'test_worker_verticle.py')
    return network

  def test_local_deploy(self):
    network = self._create_test_network()
    cluster = LocalCluster()
    def deploy_handler(error, context):
      if error:
        self.assert_true(False)
      self.complete()
    cluster.deploy(network, deploy_handler)

run_test(ClusterTestCase())
