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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.context.impl.DefaultOutputConnectionContext;
import net.kuujo.vertigo.input.grouping.MessageGrouping;

/**
 * Output connection context.
 *
 * @author Jordan Halterman
 */
@JsonTypeInfo(
  use=JsonTypeInfo.Id.CLASS,
  include=JsonTypeInfo.As.PROPERTY,
  property="class",
  defaultImpl=DefaultOutputConnectionContext.class
)
public interface OutputConnectionContext extends ConnectionContext<OutputConnectionContext> {

  /**
   * Returns the parent output port context.
   *
   * @return The parent port context.
   */
  OutputPortContext port();

  /**
   * Returns a list of output addresses.
   *
   * @return A list of output addresses.
   */
  List<String> targets();

  /**
   * Returns the output connection grouping.
   *
   * @return The output connection grouping.
   */
  MessageGrouping grouping();

}
