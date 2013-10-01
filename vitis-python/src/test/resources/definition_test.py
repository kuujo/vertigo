from test import TestCase, run_test
from vitis import create_worker

class DefinitionTestCase(TestCase):
  """
  A definition test case.
  """
  def test_something(self):
    worker = create_worker()
    self.complete()

run_test(DefinitionTestCase())
