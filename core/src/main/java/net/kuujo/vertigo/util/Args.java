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

import java.net.URI;
import java.net.URISyntaxException;

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
  public static void checkNull(Object value) {
    if (value != null) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Validates that an argument is null.
   *
   * @param value The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void checkNull(Object value, String message, Object... args) {
    if (value != null) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }

  /**
   * Validates that an argument is not null.
   *
   * @param value The value to check.
   */
  public static void checkNotNull(Object value) {
    if (value == null) {
      throw new NullPointerException();
    }
  }

  /**
   * Validates that an argument is not null.
   *
   * @param value The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void checkNotNull(Object value, String message, Object... args) {
    if (value == null) {
      throw new NullPointerException(String.format(message, args));
    }
  }

  /**
   * Validates that an argument is positive.
   *
   * @param number The value to check.
   */
  public static void checkPositive(int number) {
    if (number < 0) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Validates that an argument is positive.
   *
   * @param number The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void checkPositive(int number, String message, Object... args) {
    if (number < 0) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }

  /**
   * Validates that an argument is a valid URI.
   *
   * @param uri The value to check.
   */
  public static void checkUri(String uri) {
    try {
      new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Validates that an argument is a valid URI.
   *
   * @param uri The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void checkUri(String uri, String message, Object... args) {
    try {
      new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format(message, args), e);
    }
  }

  /**
   * Validates that an argument is a valid URI scheme.
   *
   * @param scheme The value to check.
   */
  public static void checkUriScheme(String scheme) {
    String uri = String.format("%s://null", scheme);
    try {
      new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Validates that an argument is a valid URI scheme.
   *
   * @param scheme The value to check.
   * @param message An exception message.
   * @param args Exception message arguments.
   */
  public static void checkUriScheme(String scheme, String message, Object... args) {
    String uri = String.format("%s://null", scheme);
    try {
      new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(String.format(message, args));
    }
  }

}
