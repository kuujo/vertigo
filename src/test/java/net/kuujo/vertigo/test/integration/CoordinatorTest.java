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

import net.kuujo.vertigo.cluster.ClusterClient;
import net.kuujo.vertigo.cluster.LocalClusterClient;
import net.kuujo.vertigo.component.ComponentCoordinator;
import net.kuujo.vertigo.component.impl.DefaultComponentCoordinator;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.context.NetworkContext;
import net.kuujo.vertigo.context.VerticleContext;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.fail;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * A component coordinator test.
 *
 * @author Jordan Halterman
 */
public class CoordinatorTest extends TestVerticle {

  @Test
  public void testStartWithExistingContext() {
    final NetworkContext context = NetworkContext.Builder.newBuilder()
        .setAddress("test")
        .setStatusAddress("test.__status")
        .addComponent(VerticleContext.Builder.newBuilder()
            .setName("test")
            .setAddress("test.test")
            .setStatusAddress("test.test.__status")
            .addInstance(InstanceContext.Builder.newBuilder().setAddress("test.test-1").setStatusAddress("test.test-1.__status").build()).build()).build();
    final InstanceContext instance = context.component("test").instances().iterator().next();

    final ClusterClient cluster = new LocalClusterClient(vertx, container);
    cluster.set(instance.address(), InstanceContext.toJson(instance).encode(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          final ComponentCoordinator coordinator = new DefaultComponentCoordinator(instance.address(), cluster);
          coordinator.start(new Handler<AsyncResult<InstanceContext>>() {
            @Override
            public void handle(AsyncResult<InstanceContext> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                assertEquals(instance.address(), result.result().address());
                testComplete();
              }
            }
          });
          cluster.set(context.status(), true);
        }
      }
    });
  }

  @Test
  public void testPauseHandler() {
    final NetworkContext context = NetworkContext.Builder.newBuilder()
        .setAddress("test")
        .setStatusAddress("test.__status")
        .addComponent(VerticleContext.Builder.newBuilder()
            .setName("test")
            .setAddress("test.test")
            .setStatusAddress("test.test.__status")
            .addInstance(InstanceContext.Builder.newBuilder().setAddress("test.test-1").setStatusAddress("test.test-1.__status").build()).build()).build();
    final InstanceContext instance = context.component("test").instances().iterator().next();

    final ClusterClient cluster = new LocalClusterClient(vertx, container);
    cluster.set(instance.address(), InstanceContext.toJson(instance).encode(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          final ComponentCoordinator coordinator = new DefaultComponentCoordinator(instance.address(), cluster);
          coordinator.start(new Handler<AsyncResult<InstanceContext>>() {
            @Override
            public void handle(AsyncResult<InstanceContext> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                assertEquals(instance.address(), result.result().address());
                coordinator.pauseHandler(new Handler<Void>() {
                  @Override
                  public void handle(Void _) {
                    testComplete();
                  }
                });
                cluster.delete(context.status());
              }
            }
          });
          cluster.set(context.status(), true);
        }
      }
    });
  }

  @Test
  public void testResumeHandler() {
    final NetworkContext context = NetworkContext.Builder.newBuilder()
        .setAddress("test")
        .setStatusAddress("test.__status")
        .addComponent(VerticleContext.Builder.newBuilder()
            .setName("test")
            .setAddress("test.test")
            .setStatusAddress("test.test.__status")
            .addInstance(InstanceContext.Builder.newBuilder().setAddress("test.test-1").setStatusAddress("test.test-1.__status").build()).build()).build();
    final InstanceContext instance = context.component("test").instances().iterator().next();

    final ClusterClient cluster = new LocalClusterClient(vertx, container);
    cluster.set(instance.address(), InstanceContext.toJson(instance).encode(), new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        if (result.failed()) {
          fail(result.cause().getMessage());
        }
        else {
          final ComponentCoordinator coordinator = new DefaultComponentCoordinator(instance.address(), cluster);
          coordinator.start(new Handler<AsyncResult<InstanceContext>>() {
            @Override
            public void handle(AsyncResult<InstanceContext> result) {
              if (result.failed()) {
                fail(result.cause().getMessage());
              }
              else {
                assertEquals(instance.address(), result.result().address());
                coordinator.resumeHandler(new Handler<Void>() {
                  @Override
                  public void handle(Void _) {
                    testComplete();
                  }
                });
                cluster.delete(context.status(), new Handler<AsyncResult<Void>>() {
                  @Override
                  public void handle(AsyncResult<Void> result) {
                    if (result.failed()) {
                      fail(result.cause().getMessage());
                    }
                    else {
                      cluster.set(context.status(), true);
                    }
                  }
                });
              }
            }
          });
          cluster.set(context.status(), true);
        }
      }
    });
  }

}
