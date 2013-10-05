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
import org.vertx.java.platform.impl.JythonVerticleFactory.vertx
import org.vertx.java.platform.impl.JythonVerticleFactory.container
import org.vertx.testtools.VertxAssert
from core.javautils import map_from_java, map_to_java

def run_test(test):
  """
  Runs a Python test.
  """
  org.vertx.testtools.VertxAssert.initialize(org.vertx.java.platform.impl.JythonVerticleFactory.vertx)
  method = org.vertx.java.platform.impl.JythonVerticleFactory.container.config().getString('methodName')
  getattr(test, method)()

class Test(object):
  """
  Static test methods.
  """
  @staticmethod
  def complete():
    """
    Completes the test.
    """
    org.vertx.testtools.VertxAssert.testComplete()

class Assert(object):
  """
  Static assertion methods.
  """
  @staticmethod
  def true(value, message=None):
    """
    Asserts that an expression is true.
    """
    if message is not None:
      org.vertx.testtools.VertxAssert.assertTrue(message, value)
    else:
      org.vertx.testtools.VertxAssert.assertTrue(value)

  @staticmethod
  def false(value, message=None):
    """
    Asserts that an expression is false.
    """
    if message is not None:
      org.vertx.testtools.VertxAssert.assertFalse(message, value)
    else:
      org.vertx.testtools.VertxAssert.assertFalse(value)

  @staticmethod
  def equals(val1, val2, message=None):
    """
    Asserts that two values are equal.
    """
    if message is not None:
      org.vertx.testtools.VertxAssert.assertEquals(message, map_to_java(val1), map_to_java(val2))
    else:
      org.vertx.testtools.VertxAssert.assertEquals(map_to_java(val1), map_to_java(val2))

  @staticmethod
  def null(value, message=None):
    """
    Asserts that a value is null.
    """
    if message is not None:
      org.vertx.testtools.VertxAssert.assertNull(message, value)
    else:
      org.vertx.testtools.VertxAssert.assertNull(value)

  @staticmethod
  def not_null(value, message=None):
    """
    Asserts that a value is not null.
    """
    if message is not None:
      org.vertx.testtools.VertxAssert.assertNotNull(message, value)
    else:
      org.vertx.testtools.VertxAssert.assertNotNull(value)

class TestCase(object):
  """
  A Vertigo integration test.
  """
  def assert_true(self, value, message=None):
    """
    Asserts that an expression is true.
    """
    Assert.true(value, message)

  def assert_false(self, value, message=None):
    """
    Asserts that an expression is false.
    """
    Assert.false(value, message)

  def assert_equals(self, val1, val2, message=None):
    """
    Asserts that two values are equal.
    """
    Assert.equals(val1, val2, message)

  def assert_null(self, value, message=None):
    """
    Asserts that a value is null.
    """
    Assert.null(value, message)

  def assert_not_null(self, value, message=None):
    """
    Asserts that a value is not null.
    """
    Assert.not_null(value, message)

  def complete(self):
    """
    Completes the test.
    """
    Test.complete()
