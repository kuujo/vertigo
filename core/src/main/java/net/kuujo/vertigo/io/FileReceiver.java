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
package net.kuujo.vertigo.io;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import net.kuujo.vertigo.io.group.InputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.AsyncFile;

/**
 * Input file receiver.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class FileReceiver {
  private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
  private final Input<?> input;
  private Handler<String> fileHandler;
  private Handler<Throwable> exceptionHandler;
  private File tempDir;
  private final Handler<InputGroup> groupHandler = new Handler<InputGroup>() {
    @Override
    public void handle(InputGroup group) {
      handleFile(group);
    }
  };

  public FileReceiver(Input<?> input) {
    this.input = input;
    this.tempDir = new File(TEMP_DIR);
  }

  /**
   * Registers a handler to be called when a file is received.
   *
   * @param handler A handler to be called when a file is received on the port.
   * @return The file receiver.
   */
  public FileReceiver fileHandler(Handler<String> handler) {
    this.fileHandler = handler;
    init();
    return this;
  }

  /**
   * Registers a handler to be called when an exception occurs while a file is being received.
   *
   * @param handler A handler to be called if an exception occurs.
   * @return The file receiver.
   */
  public FileReceiver exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  /**
   * Initializes the file receiver.
   */
  private void init() {
    if (fileHandler != null) {
      input.groupHandler(groupHandler);
    } else {
      input.groupHandler(null);
    }
  }

  /**
   * Handles a group input file.
   */
  private void handleFile(final InputGroup group) {
    final File file = new File(tempDir, String.format("%s-%s", group.name(), UUID.randomUUID().toString()));
    input.vertx().fileSystem().open(file.getAbsolutePath(), new Handler<AsyncResult<AsyncFile>>() {
      @Override
      public void handle(AsyncResult<AsyncFile> result) {
        if (result.succeeded()) {
          handleFile(file.getAbsolutePath(), result.result(), group);
        }
      }
    });
  }

  /**
   * Handles a group input file.
   */
  private void handleFile(final String filePath, final AsyncFile file, final InputGroup group) {
    final AtomicLong position = new AtomicLong();
    group.messageHandler(new Handler<Buffer>() {
      @Override
      public void handle(Buffer buffer) {
        file.write(buffer, position.get(), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              file.close();
              group.messageHandler(null);
              group.endHandler(null);
              try {
                input.vertx().fileSystem().deleteSync(filePath);
              } catch (Exception e) {
              }
              if (exceptionHandler != null) {
                exceptionHandler.handle(result.cause());
              }
            }
          }
        });
        position.addAndGet(buffer.length());
      }
    });
    group.endHandler(new Handler<Void>() {
      @Override
      public void handle(Void event) {
        file.close();
        if (fileHandler != null) {
          fileHandler.handle(filePath);
        }
      }
    });
  }

}
