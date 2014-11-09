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
 * Validation utilities.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public final class Args {

  /**
   * Validates that an argument is null.
   *
   * @param value The value to check.
   */
  public static <T> T checkNull(T value) {
    if (value != null) {
      throw new IllegalArgumentException();
    }
    return null;
  }

  /**
   * Validates that an argument is null.
   *
   * @param value The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static <T> T checkNull(T value, String message, Object... args) {
    if (value != null) {
      throw new IllegalArgumentException(String.format(message, args));
    }
    return null;
  }

  /**
   * Validates that an argument is not null.
   *
   * @param value The value to check.
   */
  public static <T> T checkNotNull(T value) {
    if (value == null) {
      throw new NullPointerException();
    }
    return value;
  }

  /**
   * Validates that an argument is not null.
   *
   * @param value The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static <T> T checkNotNull(T value, String message, Object... args) {
    if (value == null) {
      throw new NullPointerException(String.format(message, args));
    }
    return value;
  }

  /**
   * Validates that an argument is positive.
   *
   * @param number The value to check.
   */
  public static int checkPositive(int number) {
    if (number < 0) {
      throw new IllegalArgumentException();
    }
    return number;
  }

  /**
   * Validates that an argument is positive.
   *
   * @param number The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static int checkPositive(int number, String message, Object... args) {
    if (number < 0) {
      throw new IllegalArgumentException(String.format(message, args));
    }
    return number;
  }

  /**
   * Checks an arbitrary condition.
   *
   * @param check The condition result.
   */
  public static void check(boolean check) {
    if (!check) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Checks an arbitrary condition.
   *
   * @param check The condition result.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void check(boolean check, String message, Object... args) {
    if (!check) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }

}
