/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.vertigo.integration;

import static org.vertx.testtools.VertxAssert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticle;

/**
 * Tests deploying networks from the command line.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@RunWith(DeployerTest.DeployerClassRunner.class)
public class DeployerTest extends TestVerticle {

  public static class DeployerClassRunner extends JavaClassRunner {
    static {
      System.setProperty("vertx.mods", "src/test/mods");
    }
    public DeployerClassRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }
  }

  @Test
  public void testDeploy() {
    container.deployVerticle("test.network", new Handler<AsyncResult<String>>() {
      @Override
      public void handle(AsyncResult<String> result) {
        assertTrue(result.succeeded());
      }
    });
  }

  @AfterClass
  public static void afterClass() {
    System.setProperty("vertx.mods", "target/mods");
  }

}
