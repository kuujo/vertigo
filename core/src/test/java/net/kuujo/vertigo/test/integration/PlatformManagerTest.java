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
package net.kuujo.vertigo.test.integration;

import static org.vertx.testtools.VertxAssert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

import java.util.Collection;

import net.kuujo.vertigo.platform.ModuleInfo;
import net.kuujo.vertigo.platform.PlatformManager;
import net.kuujo.vertigo.platform.impl.DefaultPlatformManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

/**
 * Platform manager tests.
 *
 * @author Jordan Halterman
 */
public class PlatformManagerTest extends TestVerticle {

  @BeforeClass
  public static void beforeClass() {
    System.setProperty("vertx.mods", "src/test/resources/test-mods");
  }

  @Test
  public void testGetModules() {
    PlatformManager platform = new DefaultPlatformManager(vertx, container);
    platform.getModuleInfo(new Handler<AsyncResult<Collection<ModuleInfo>>>() {
      @Override
      public void handle(AsyncResult<Collection<ModuleInfo>> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        } else {
          assertTrue(result.succeeded());
          assertEquals(4, result.result().size());
          testComplete();
        }
      }
    });
  }

  @Test
  public void testGetModule() {
    PlatformManager platform = new DefaultPlatformManager(vertx, container);
    platform.getModuleInfo("net.kuujo~test-mod-1~1.0", new Handler<AsyncResult<ModuleInfo>>() {
      @Override
      public void handle(AsyncResult<ModuleInfo> result) {
        if (result.failed()) {
          assertTrue(result.cause().getMessage(), result.succeeded());
        } else {
          ModuleInfo info = result.result();
          assertEquals("net.kuujo~test-mod-1~1.0", info.id().toString());
          assertEquals("app.js", info.fields().getMain());
          assertEquals("net.kuujo~test-include-1~1.0", info.fields().getIncludes());
          assertEquals("net.kuujo~test-include-2~1.0", info.fields().getDeploys());
          assertEquals("in", info.fields().getInPorts().get(0));
          assertEquals("out", info.fields().getOutPorts().get(0));
          testComplete();
        }
      }
    });
  }

  @AfterClass
  public static void afterClass() {
    System.setProperty("vertx.mods", "target/mods");
  }

}
