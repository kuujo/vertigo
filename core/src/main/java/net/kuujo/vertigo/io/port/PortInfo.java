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
package net.kuujo.vertigo.io.port;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java port info annotation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PortInfo {

  /**
   * The port name.
   */
  String name();

  /**
   * The port type.
   */
  Class<?> type() default Object.class;

  /**
   * The port message codec.
   */
  Class<? extends MessageCodec> codec() default None.class;

  /**
   * Indicates whether the port is persistent.
   */
  boolean persistent() default false;

  /**
   * Empty message codec.
   */
  public static class None implements MessageCodec {
    @Override
    public String name() {
      throw new IllegalStateException("Invalid message codec");
    }
    @Override
    public void encodeToWire(Buffer buffer, Object o) {
      throw new IllegalStateException("Invalid message codec");
    }
    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
      throw new IllegalStateException("Invalid message codec");
    }
    @Override
    public Object transform(Object o) {
      throw new IllegalStateException("Invalid message codec");
    }
    @Override
    public byte systemCodecID() {
      throw new IllegalStateException("Invalid message codec");
    }
  }

}
