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
package net.kuujo.vertigo.platform;

import java.util.Collection;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Local module system manager.<p>
 *
 * The platform manager facilitates zipping, installing, uninstalling, and
 * deploying modules. This allows Vertigo to read the local module system
 * and distribute modules across the Vertigo cluster.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PlatformManager {

  /**
   * Loads module info for all local modules.
   *
   * @param resultHandler An asynchronous handler to be called once complete.
   *        The handler will be called with a collection of module info from which
   *        the module identifier and configuration can be read.
   * @return The platform manager.
   */
  PlatformManager getModuleInfo(Handler<AsyncResult<Collection<ModuleInfo>>> resultHandler);

  /**
   * Loads module info for a local module.
   *
   * @param moduleName The name of the module for which to load info.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager getModuleInfo(String moduleName, Handler<AsyncResult<ModuleInfo>> doneHandler);

  /**
   * Zips a local module.
   *
   * @param moduleName The name of the module to zip.
   * @param doneHandler An asynchronous handler to be called once complete. The handler will
   *        be called with the full path to the module zip file.
   * @return The platform manager.
   */
  PlatformManager zipModule(String zipFile, Handler<AsyncResult<String>> doneHandler);

  /**
   * Installs a module from a zip file.
   *
   * @param moduleZip The full path to the module zip file.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager installModule(String moduleZip, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Uninstalls a locally installed module.
   *
   * @param moduleName The name of the module to uninstall.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Deploys a local module.
   *
   * @param moduleName The name of the module to deploy.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called with the unique deployment ID.
   * @return The platform manager.
   */
  PlatformManager deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle.
   *
   * @param main The verticle main.
   * @param config The verticle configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Whether the verticle should be deployed multi-threaded.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the local container.
   *
   * @param deploymentID The module deployment ID.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the local container.
   *
   * @param deploymentID The verticle deployment ID.
   * @param doneHandler An asynchronous handler to be called once complete.
   * @return The platform manager.
   */
  PlatformManager undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

}
