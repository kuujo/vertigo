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

import net.kuujo.vertigo.io.group.OutputGroup;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.impl.DefaultFutureResult;

/**
 * Output file sender.<p>
 * 
 * The file sender uses an output group to send a file to target component(s).
 * Since Vertigo guarantees message ordering, the file can be sent asynchronously
 * rather than waiting for replies from the target components. Once the entire
 * file has been sent the group will be closed.<p>
 *
 * You should use a {@link FileReceiver} to receive files on an input port.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class FileSender {
  private static final int BUFFER_SIZE = 4096;
  private final Output<?> output;

  public FileSender(Output<?> output) {
    this.output = output;
  }

  /**
   * Sends a file on the output port.
   *
   * @param file The file to send.
   * @return The file sender.
   */
  public FileSender sendFile(AsyncFile file) {
    return sendFile(file, null);
  }

  /**
   * Sends a file on the output port.
   *
   * @param file The file to send.
   * @param doneHandler An asynchronous handler to be called once the file has been sent.
   * @return The file sender.
   */
  public FileSender sendFile(final AsyncFile file, final Handler<AsyncResult<Void>> doneHandler) {
    output.group("file", new Handler<OutputGroup>() {
      @Override
      public void handle(OutputGroup group) {
        doSendFile(file, group, doneHandler);
      }
    });
    return this;
  }

  /**
   * Sends a file on the output port.
   *
   * @param filePath The path to the file to send.
   * @return The file sender.
   */
  public FileSender sendFile(String filePath) {
    return sendFile(filePath, null);
  }

  /**
   * Sends a file on the output port.
   *
   * @param filePath The path to the file to send.
   * @param doneHandler An asynchronous handler to be called once the file has been sent.
   * @return The file sender.
   */
  public FileSender sendFile(String filePath, final Handler<AsyncResult<Void>> doneHandler) {
    final File file = new File(filePath);
    output.vertx().fileSystem().exists(file.getAbsolutePath(), new Handler<AsyncResult<Boolean>>() {
      @Override
      public void handle(AsyncResult<Boolean> result) {
        if (result.failed()) {
          new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
        } else if (!result.result()) {
          new DefaultFutureResult<Void>(new IOException("File not found.")).setHandler(doneHandler);
        } else {
          output.group(file.getName(), new Handler<OutputGroup>() {
            @Override
            public void handle(final OutputGroup group) {
              output.vertx().fileSystem().open(file.getAbsolutePath(), new Handler<AsyncResult<AsyncFile>>() {
                @Override
                public void handle(AsyncResult<AsyncFile> result) {
                  if (result.failed()) {
                    new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                  } else {
                    doSendFile(result.result(), group, doneHandler);
                  }
                }
              });
            }
          });
        }
      }
    });
    return this;
  }

  /**
   * Sends the file to the given output group.
   */
  private void doSendFile(final AsyncFile file, final OutputGroup group, final Handler<AsyncResult<Void>> doneHandler) {
    doSendFile(file, group, 0, doneHandler);
  }

  /**
   * Sends the file to the given output group.
   */
  private void doSendFile(final AsyncFile file, final OutputGroup group, final int position, final Handler<AsyncResult<Void>> doneHandler) {
    if (!group.sendQueueFull()) {
      file.read(new Buffer(BUFFER_SIZE), 0, position, BUFFER_SIZE, new Handler<AsyncResult<Buffer>>() {
        @Override
        public void handle(AsyncResult<Buffer> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else {
            Buffer buffer = result.result();
            if (buffer.length() > 0) {
              group.send(buffer);
              doSendFile(file, group, position+buffer.length(), doneHandler);
            } else {
              group.end();
              new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
            }
          }
        }
      });
    } else {
      group.drainHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          doSendFile(file, group, position, doneHandler);
        }
      });
    }
  }

}
