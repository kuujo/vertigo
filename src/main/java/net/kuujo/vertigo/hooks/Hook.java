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

/**
 * A base hook.
 *
 * @author Jordan Halterman
 *
 * @param <T> The hook subject
 */
public interface Hook<T> {

  /**
   * Called when the hook subject has started.
   *
   * @param subject
   *   The hook subject.
   */
  void start(T subject);

  /**
   * Called when the hook subject has stopped.
   *
   * @param subject
   *   The hook subject.
   */
  void stop(T subject);

}
