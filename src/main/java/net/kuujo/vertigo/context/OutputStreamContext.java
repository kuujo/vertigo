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
package net.kuujo.vertigo.context;

import java.util.List;

import net.kuujo.vertigo.context.impl.DefaultOutputStreamContext;
import net.kuujo.vertigo.input.grouping.Grouping;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Output stream context.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultOutputStreamContext.class
)
public interface OutputStreamContext extends Context<OutputStreamContext> {

  /**
   * Returns the parent output port context.
   *
   * @return The parent port context.
   */
  OutputPortContext port();

  /**
   * Returns the stream grouping.
   *
   * @return The stream grouping.
   */
  Grouping grouping();

  /**
   * Returns a list of output connections.
   *
   * @return A list of output connections.
   */
  List<OutputConnectionContext> connections();

}
