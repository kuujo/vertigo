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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.annotations.ClusterType;
import net.kuujo.vertigo.annotations.Factory;
import net.kuujo.vertigo.data.AsyncIdGenerator;
import net.kuujo.vertigo.data.AsyncList;
import net.kuujo.vertigo.data.AsyncLock;
import net.kuujo.vertigo.data.AsyncQueue;
import net.kuujo.vertigo.data.AsyncSet;
import net.kuujo.vertigo.data.WatchableAsyncMap;
import net.kuujo.vertigo.data.impl.XyncIdGenerator;
import net.kuujo.vertigo.data.impl.XyncList;
import net.kuujo.vertigo.data.impl.XyncLock;
import net.kuujo.vertigo.data.impl.XyncMap;
import net.kuujo.vertigo.data.impl.XyncQueue;
import net.kuujo.vertigo.data.impl.XyncSet;
import net.kuujo.xync.XyncCluster;
import net.kuujo.xync.impl.DefaultXyncCluster;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;

/**
 * Remote cluster implementation.<p>
 *
 * The remote cluster is backed by the custom Xync platform manager
 * and Hazelcast data structures via the Vert.x event bus. When running
 * the remote cluster the Vert.x instance <b>must</b> have been run with
 * the custom Xync platform manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@ClusterType
public class RemoteCluster implements VertigoCluster {
  private final Vertx vertx;
  private final XyncCluster cluster;

  @Factory
  public static VertigoCluster factory(Vertx vertx, Container container) {
    return new RemoteCluster(vertx);
  }

  public RemoteCluster(Verticle verticle) {
    this(verticle.getVertx());
  }

  public RemoteCluster(Vertx vertx) {
    this.vertx = vertx;
    this.cluster = new DefaultXyncCluster(vertx);
  }

  @Override
  public VertigoCluster isDeployed(String deploymentID, final Handler<AsyncResult<Boolean>> resultHandler) {
    cluster.isDeployed(deploymentID, resultHandler);
    return this;
  }

  @Override
  public VertigoCluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    cluster.deployModule(deploymentID, moduleName, config, instances, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    cluster.deployModuleTo(deploymentID, groupID, moduleName, config, instances, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler) {
    cluster.deployVerticle(deploymentID, main, config, instances, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, final Handler<AsyncResult<String>> doneHandler) {
    cluster.deployVerticleTo(deploymentID, groupID, main, config, instances, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler) {
    cluster.deployWorkerVerticle(deploymentID, main, config, instances, multiThreaded, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, final Handler<AsyncResult<String>> doneHandler) {
    cluster.deployWorkerVerticleTo(deploymentID, groupID, main, config, instances, multiThreaded, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster undeployModule(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.undeployModule(deploymentID, doneHandler);
    return this;
  }

  @Override
  public VertigoCluster undeployVerticle(String deploymentID, final Handler<AsyncResult<Void>> doneHandler) {
    cluster.undeployVerticle(deploymentID, doneHandler);
    return this;
  }

  @Override
  public <T> AsyncList<T> getList(String name) {
    return XyncList.factory(name, vertx);
  }

  @Override
  public <K, V> WatchableAsyncMap<K, V> getMap(String name) {
    return XyncMap.factory(name, vertx);
  }

  @Override
  public <T> AsyncSet<T> getSet(String name) {
    return XyncSet.factory(name, vertx);
  }

  @Override
  public <T> AsyncQueue<T> getQueue(String name) {
    return XyncQueue.factory(name, vertx);
  }

  @Override
  public AsyncIdGenerator getIdGenerator(String name) {
    return XyncIdGenerator.factory(name, vertx);
  }

  @Override
  public AsyncLock getLock(String name) {
    return XyncLock.factory(name, vertx);
  }

}
