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
package net.kuujo.vertigo.util;

import java.util.ArrayDeque;
import java.util.Queue;

import org.vertx.java.core.Handler;

/**
 * Runs handlers sequentially to prevent race conditions in asynchronous coordination.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class TaskRunner {
  private final Queue<Handler<Task>> queue = new ArrayDeque<>();
  private Task currentTask;

  /**
   * Runs a sequential task.
   *
   * @param task A handler to be called once the task is started.
   */
  public void runTask(Handler<Task> task) {
    if (currentTask == null) {
      currentTask = new Task(this);
      task.handle(currentTask);
    } else {
      queue.add(task);
    }
  }

  /**
   * Completes the given task.
   */
  void complete(Task task) {
    if (task.equals(currentTask)) {
      currentTask = null;
      checkTasks();
    }
  }

  /**
   * Starts the next task if necessary.
   */
  private void checkTasks() {
    if (currentTask == null) {
      Handler<Task> task = queue.poll();
      if (task != null) {
        currentTask = new Task(this);
        task.handle(currentTask);
      }
    }
  }

}
