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
package net.kuujo.vertigo.examples.filesend;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import net.kuujo.vertigo.Vertigo;
import net.kuujo.vertigo.cluster.Cluster;
import net.kuujo.vertigo.io.group.InputGroup;
import net.kuujo.vertigo.io.group.OutputGroup;
import net.kuujo.vertigo.java.ComponentVerticle;
import net.kuujo.vertigo.network.ActiveNetwork;
import net.kuujo.vertigo.network.NetworkConfig;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxException;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * File sender example.<p>
 *
 * This example demonstrates how message groups can be used to send files
 * through an output port. It takes advantage of the fact that Vertigo
 * guarantees ordering of messages and guarantees that messages within
 * a message group will always be sent to the same target component instance.<p>
 *
 * Note that Vertigo provides custom <code>FileSender</code> and
 * <code>FileReceiver</code> helpers.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class FileSendNetwork extends Verticle {

  /**
   * Component that sends a file on an output port.
   */
  public static class FileSender extends ComponentVerticle {
    private final Handler<Message<String>> messageHandler = new Handler<Message<String>>() {
      @Override
      public void handle(final Message<String> message) {
        // Allow other verticles to trigger sending a file from the component
        // over the event bus.
        sendFile(message.body(), new Handler<AsyncResult<Void>>() {
          @Override
          public void handle(AsyncResult<Void> result) {
            if (result.failed()) {
              message.reply(new JsonObject().putString("status", "error").putString("message", result.cause().getMessage()));
            } else {
              message.reply(new JsonObject().putString("status", "ok"));
            }
          }
        });
      }
    };

    @Override
    public void start(final Future<Void> startResult) {
      // Register a handler on the event bus to which users can send
      // a message telling the file sender to send a file.
      vertx.eventBus().registerHandler("sendfile", messageHandler, new Handler<AsyncResult<Void>>() {
        @Override
        public void handle(AsyncResult<Void> result) {
          // Once the handler has been registered, tell Vert.x and Vertigo
          // that the component has finished setup.
          if (result.failed()) {
            startResult.setFailure(result.cause());
          } else {
            startResult.setResult(null);
          }
        }
      });
    }

    /**
     * Sends the given file on the "file" output port.
     */
    private void sendFile(final String fileName, final Handler<AsyncResult<Void>> doneHandler) {
      // First we need to check whether the file exists on the local file system.
      vertx.fileSystem().exists(fileName, new Handler<AsyncResult<Boolean>>() {
        @Override
        public void handle(AsyncResult<Boolean> result) {
          if (result.failed()) {
            new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
          } else if (!result.result()) {
            new DefaultFutureResult<Void>(new VertxException("File not found.")).setHandler(doneHandler);
          } else {
            // In order to send a file, we need to create a new output group.
            // The group will guarantee that all messages within it go to the
            // same target component instances. And since messages are guaranteed
            // to be ordered, we can stream the file to the appropriate out port.
            output.port("file").group(new File(fileName).getName(), new Handler<OutputGroup>() {
              @Override
              public void handle(final OutputGroup group) {
                // Once the output group has been opened, the other side of all
                // connections are ready to receive the file. We can open it
                // and start sending its contents.
                vertx.fileSystem().open(fileName, new Handler<AsyncResult<AsyncFile>>() {
                  @Override
                  public void handle(AsyncResult<AsyncFile> result) {
                    if (result.failed()) {
                      new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
                    } else {
                      doSendFile(group, result.result(), 0, doneHandler);
                    }
                  }
                });
              }
            });
          }
        }
      });
    }

    /**
     * Sends a file on an output group.
     */
    private void doSendFile(final OutputGroup group, final AsyncFile file, final long position, final Handler<AsyncResult<Void>> doneHandler) {
      if (!group.sendQueueFull()) {
        // If the group's queue is not full then read the next portion of the file
        // and send it through the group.
        file.read(new Buffer(4096), 0, position, 4096, new Handler<AsyncResult<Buffer>>() {
          @Override
          public void handle(AsyncResult<Buffer> result) {
            if (result.failed()) {
              new DefaultFutureResult<Void>(result.cause()).setHandler(doneHandler);
            } else {
              Buffer buffer = result.result();
              if (buffer.length() > 0) {
                // Send the buffer and try to read the next portion of the file.
                group.send(buffer);
                doSendFile(group, file, position+buffer.length(), doneHandler);
              } else {
                // If there's no more data to send then end the group. This will
                // indicate to the other side of the connection that all the
                // file contents have been sent.
                group.end();
                new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
              }
            }
          }
        });
      } else {
        // If the group's output queue is full then we set a drain handler to
        // be called once the group can handler more messages. This handler will
        // be called when the group's queue reaches half of its full size.
        group.drainHandler(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            doSendFile(group, file, position, doneHandler);
          }
        });
      }
    }
  }

  /**
   * Component that receives a file on an input port.
   */
  public static class FileReceiver extends ComponentVerticle {
    @Override
    public void start() {
      input.port("file").groupHandler(new Handler<InputGroup>() {
        @Override
        public void handle(InputGroup group) {
          receiveFile(group);
        }
      });
    }

    /**
     * Handles receiving a file on an input group.
     */
    private void receiveFile(final InputGroup group) {
      // The file name will be the group's name.
      String fileName = group.name();
      final File file = new File(fileName);

      // The group won't begin sending messages until we register a message
      // handler. This gives us time to call asynchronous APIs before the
      // group starts.
      vertx.fileSystem().open(file.getAbsolutePath(), new Handler<AsyncResult<AsyncFile>>() {
        @Override
        public void handle(AsyncResult<AsyncFile> result) {
          if (result.succeeded()) {
            // If we opened the file successfully then start receiving it.
            receiveFile(file.getAbsolutePath(), result.result(), group);
          } else {
            // Log an error message to the Vertigo port logger. The error message
            // will be logged both to the default Vert.x logger and the "error"
            // output port.
            logger.error("Failed to open file");
          }
        }
      });
    }

    /**
     * Handles receiving a file on an input group.
     */
    private void receiveFile(final String filePath, final AsyncFile file, final InputGroup group) {
      // Track the current pointer position in the file.
      final AtomicLong position = new AtomicLong();

      // The message group cannot begin sending messages until we register a
      // message handler, indicating that we're prepared to receive messages.
      group.messageHandler(new Handler<Buffer>() {
        @Override
        public void handle(Buffer buffer) {

          // Since all messages are guaranteed to be received in order, we can
          // simply write the buffer to the file at the last known position.
          file.write(buffer, position.get(), new Handler<AsyncResult<Void>>() {
            @Override
            public synchronized void handle(AsyncResult<Void> result) {
              // If the write failed then close the file and try to delete it.
              if (result.failed()) {
                file.close();

                // Since messages are sent asynchronously, we need to explicitly
                // unregister message handlers from the group in order to ensure
                // we don't try to write any more messages to the closed file.
                group.messageHandler(null);
                group.endHandler(null);

                // Try to delete the file since we failed to write it all.
                try {
                  vertx.fileSystem().deleteSync(filePath);
                } catch (Exception e) {
                }

                // Log the error to the Vertigo logger. This will log a message to
                // the default Vert.x logger and the "error" output port.
                logger.error(result.cause());
              }
            }
          });

          // Increment the current buffer position.
          position.addAndGet(buffer.length());
        }
      });

      // In order to determine when we've received the entire file, we need to set
      // a group end handler. This will be called once all the messages or groups
      // within the group have been received.
      group.endHandler(new Handler<Void>() {
        @Override
        public void handle(Void event) {
          // Once the entire file has been received, close the file.
          file.close(new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(AsyncResult<Void> result) {
              if (result.failed()) {
                // If the close fails, log the error message to the Vertigo logger.
                // This will cause a message to be logged to the default Vert.x
                // logger and the "error" output port.
                logger.error(result.cause());
              }
            }
          });
        }
      });
    }
  }

  @Override
  public void start(final Future<Void> startResult) {
    final Vertigo vertigo = new Vertigo(this);

    // Deploy a single-node Vertigo cluster at the "default" event bus address.
    // Note: In production clusters you should deploy instances of the
    // net.kuujo~vertigo-cluster~0.7.x module.
    vertigo.deployCluster("default", new Handler<AsyncResult<Cluster>>() {
      @Override
      public void handle(AsyncResult<Cluster> result) {
        if (result.failed()) {
          startResult.setFailure(result.cause());
        } else {
          // If the cluster was successfully deployed, create the file sender network.
          NetworkConfig network = vertigo.createNetwork("file-send");

          // Add the "sender" component.
          network.addVerticle("sender", FileSender.class.getName());

          // We can create four instances of the "receiver" and Vertigo will still
          // ensure that each file is sent to the same "receiver" instance.
          network.addVerticle("receiver", FileReceiver.class.getName(), 4);

          // Create a connection between "sender" and "receiver" via the "file" port.
          network.createConnection("sender", "file", "receiver", "file");

          // Now deploy the network to the cluster.
          Cluster cluster = result.result();
          cluster.deployNetwork(network, new Handler<AsyncResult<ActiveNetwork>>() {
            @Override
            public void handle(AsyncResult<ActiveNetwork> result) {
              if (result.failed()) {
                startResult.setFailure(result.cause());
              } else {
                startResult.setResult(null);
              }
            }
          });
        }
      }
    });
  }

}
