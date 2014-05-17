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
 * Vert.x platform manager.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface PlatformManager {

  PlatformManager getModuleInfo(Handler<AsyncResult<Collection<ModuleInfo>>> resultHandler);

  PlatformManager getModuleInfo(String moduleName, Handler<AsyncResult<ModuleInfo>> doneHandler);

  PlatformManager zipModule(String moduleName, Handler<AsyncResult<String>> doneHandler);

  PlatformManager installModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  PlatformManager uninstallModule(String moduleName, Handler<AsyncResult<Void>> doneHandler);

  PlatformManager deployModule(String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  PlatformManager deployVerticle(String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  PlatformManager deployWorkerVerticle(String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  PlatformManager undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  PlatformManager undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

}
