/*
 * Copyright 2013 the original author or authors.
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
package net.kuujo.vertigo.hooks;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import net.kuujo.vertigo.component.Component;
import net.kuujo.vertigo.output.OutputCollector;
import net.kuujo.vertigo.serializer.Serializable;
import net.kuujo.vertigo.input.InputCollector;
import net.kuujo.vertigo.network.Network;

/**
 * A base hook.
 *
 * Hooks may be used to receive notification of certain events that occur
 * within various Vertigo objects.
 *
 * This hook receives notifications of when a Vertigo object starts or
 * stops, such as an {@link InputCollector}, {@link OutputCollector},
 * or a {@link Component} instance. Hooks can be added either directly
 * on the relevant object or added externally via a {@link Network} definition.
 *
 * @author Jordan Halterman
 *
 * @param <T> The hook subject
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="type")
public interface Hook<T> extends Serializable {

  /**
   * Called when the hook subject has started.
   *
   * @param subject
   *   The hook subject.
   */
  void handleStart(T subject);

  /**
   * Called when the hook subject has stopped.
   *
   * @param subject
   *   The hook subject.
   */
  void handleStop(T subject);

}
