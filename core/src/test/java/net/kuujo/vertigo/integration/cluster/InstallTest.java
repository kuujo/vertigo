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
package net.kuujo.vertigo.integration.cluster;

import static org.vertx.testtools.VertxAssert.assertNotNull;
import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.testComplete;
import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.cluster.impl.DefaultCluster;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.JavaClassRunner;
import org.vertx.testtools.TestVerticle;

/**
 * Module installation and deployment tests.
 *
 * @author Jordan Halterman
 */
@RunWith(InstallTest.InstallClassRunner.class)
public class InstallTest extends TestVerticle {

  public static class InstallClassRunner extends JavaClassRunner {
    static {
      System.setProperty("vertx.mods", "src/test/resources/server-mods");
    }
    public InstallClassRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }
  }

  @Test
  public void testInstallAndDeploy() {
    // Deploy a cluster. This cluster will be deployed with the vertx.mods
    // property set to server-mods where it will install uploaded modules.
    Vertigo vertigo = new Vertigo(this);
    vertigo.deployCluster("test", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        assertTrue(result.succeeded());
        // In order to trick the cluster client into thinking our modules are
        // in the test-mods directory (where they actually are) we have to set
        // the vertx.mods property and then construct a new cluster instance.
        // When the cluster client installs a module to the cluster, it will
        // pull the module from the test-mods directory.
        System.setProperty("vertx.mods", "src/test/resources/test-mods");
        final Cluster cluster = new DefaultCluster("test", vertx, container);
        cluster.deployModule("net.kuujo~test-mod-2~1.0", new Handler<AsyncResult<String>>() {
          @Override
          public void handle(AsyncResult<String> result) {
            assertTrue(result.failed());
            cluster.installModule("net.kuujo~test-mod-2~1.0", new Handler<AsyncResult<Void>>() {
              @Override
              public void handle(AsyncResult<Void> result) {
                assertTrue(result.succeeded());
                cluster.deployModule("net.kuujo~test-mod-2~1.0", new Handler<AsyncResult<String>>() {
                  @Override
                  public void handle(AsyncResult<String> result) {
                    assertTrue(result.succeeded());
                    assertNotNull(result.result());
                    vertx.fileSystem().deleteSync("src/test/resources/server-mods", true);
                    testComplete();
                  }
                });
              }
            });
          }
        });
      }
    });
  }

  @AfterClass
  public static void after() {
    System.clearProperty("vertx.mods");
  }

}
