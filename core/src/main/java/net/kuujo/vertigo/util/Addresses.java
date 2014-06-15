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

/**
 * Vertigo/event bus address utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class Addresses {

  /**
   * Creates a unique address that starts with a letter and contains
   * only alpha-numeric characters plus: +.-
   */
  public static String createUniqueAddress() {
    return ContextUri.createUniqueScheme();
  }

  /**
   * Creates a unique address with a string prefix.
   */
  public static String createUniqueAddress(String prefix) {
    return String.format("%s-%s", prefix, createUniqueAddress());
  }

}
