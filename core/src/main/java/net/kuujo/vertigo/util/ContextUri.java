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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.vertx.java.core.json.impl.Base64;

/**
 * Context URI.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class ContextUri {
  public static final String ENDPOINT_IN = "in";
  public static final String ENDPOINT_OUT = "out";
  private final URI uri;

  public ContextUri(String uri) {
    try {
      this.uri = new URI(uri);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Creates a valid unique URI scheme.
   *
   * @return A unique URI scheme.
   */
  public static String createUniqueScheme() {
    return Base64.encodeBytes(UUID.randomUUID().toString().getBytes());
  }

  /**
   * Returns the cluster name.
   *
   * @return The cluster name defined in the URI.
   */
  public String getCluster() {
    return uri.getScheme();
  }

  /**
   * Returns a boolean indicating whether the URI has a cluster.
   *
   * @return Indicates whether the URI defines a cluster.
   */
  public boolean hasCluster() {
    return getCluster() != null;
  }

  /**
   * Returns the network name.
   *
   * @return The network name defined in the URI.
   */
  public String getNetwork() {
    return uri.getHost();
  }

  /**
   * Returns a boolean indicating whether the URI has a network.
   *
   * @return Indicates whether the URI defines a network.
   */
  public boolean hasNetwork() {
    return getNetwork() != null;
  }

  /**
   * Returns the component name.
   *
   * @return The component name defined in the URI.
   */
  public String getComponent() {
    try {
      return uri.getPath().split("/")[1];
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns a boolean indicating whether the URI has a component.
   *
   * @return Indicates whether the URI defines a component.
   */
  public boolean hasComponent() {
    return getComponent() != null;
  }

  /**
   * Returns the instance number.
   *
   * @return The instance number defined in the URI.
   */
  public Integer getInstance() {
    try {
      return Integer.valueOf(uri.getPath().split("/")[2]);
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns a boolean indicating whether the URI has a instance.
   *
   * @return Indicates whether the URI defines a instance.
   */
  public boolean hasInstance() {
    return getInstance() != null;
  }

  /**
   * Returns the context endpoint type.
   *
   * @return The context endpoint type defined in the URI.
   */
  public String getEndpoint() {
    try {
      String endpointString = uri.getPath().split("/")[3];
      if (!endpointString.equals(ENDPOINT_IN) && !endpointString.equals(ENDPOINT_OUT)) {
        throw new IllegalArgumentException(endpointString + " is not a valid context endpoint");
      }
      return endpointString;
    } catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns a boolean indicating whether the URI has a endpoint.
   *
   * @return Indicates whether the URI defines a endpoint.
   */
  public boolean hasEndpoint() {
    return getEndpoint() != null;
  }

  /**
   * Returne the context port.
   *
   * @return The context port defined in the URI.
   */
  public String getPort() {
    return uri.getUserInfo();
  }

  /**
   * Returns a boolean indicating whether the URI has a port.
   *
   * @return Indicates whether the URI defines a port.
   */
  public boolean hasPort() {
    return getPort() != null;
  }

  /**
   * Returns a map of all query arguments.
   *
   * @return A map of all query arguments.
   */
  public Map<String, String> getQuery() {
    String query = uri.getQuery();
    String[] pairs = query.split("&");
    Map<String, String> args = new HashMap<>();
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      try {
        args.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new IllegalArgumentException(e);
      }
    }
    return args;
  }

  /**
   * Returns a specific query argument.
   *
   * @param name The name of the argument to return.
   * @return The argument value.
   */
  @SuppressWarnings("unchecked")
  public <T> T getQuery(String name) {
    return (T) getQuery().get(name);
  }

  /**
   * Returns a boolean indicating whether the URI has a query argument.
   *
   * @return Indicates whether the URI defines a query argument.
   */
  public boolean hasQuery(String name) {
    return getQuery(name) != null;
  }

}
