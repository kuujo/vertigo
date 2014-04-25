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
package net.kuujo.vertigo.io.connection.impl;

import java.util.ArrayList;
import java.util.List;

import net.kuujo.vertigo.hook.IOHook;
import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.hook.OutputHook;
import net.kuujo.vertigo.io.connection.ConnectionConfig;
import net.kuujo.vertigo.io.selector.AllSelector;
import net.kuujo.vertigo.io.selector.CustomSelector;
import net.kuujo.vertigo.io.selector.FairSelector;
import net.kuujo.vertigo.io.selector.HashSelector;
import net.kuujo.vertigo.io.selector.RandomSelector;
import net.kuujo.vertigo.io.selector.RoundRobinSelector;
import net.kuujo.vertigo.io.selector.Selector;
import net.kuujo.vertigo.network.NetworkConfig;

/**
 * Default connection configuration implementation.
 * 
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class DefaultConnectionConfig implements ConnectionConfig {
  private static final String DEFAULT_OUT_PORT = "out";
  private static final String DEFAULT_IN_PORT = "in";

  private Source source = new DefaultSource();
  private Target target = new DefaultTarget();
  private List<IOHook> hooks = new ArrayList<>();
  private Selector selector;

  public DefaultConnectionConfig() {
    super();
    selector = new RoundRobinSelector();
  }

  public DefaultConnectionConfig(String source, String target, NetworkConfig network) {
    this(source, DEFAULT_OUT_PORT, target, DEFAULT_IN_PORT, new RoundRobinSelector(), network);
  }

  public DefaultConnectionConfig(String source, String target, Selector selector, NetworkConfig network) {
    this(source, DEFAULT_OUT_PORT, target, DEFAULT_IN_PORT, selector, network);
  }

  public DefaultConnectionConfig(String source, String out, String target, String in, NetworkConfig network) {
    this(source, out, target, in, new RoundRobinSelector(), network);
  }

  public DefaultConnectionConfig(String source, String out, String target, String in, Selector selector, NetworkConfig network) {
    this.source.setComponent(source);
    this.source.setPort(out);
    this.target.setComponent(target);
    this.target.setPort(in);
    if (selector == null) {
      selector = new RoundRobinSelector();
    }
    this.selector = selector;
  }

  @Override
  public Source getSource() {
    return source;
  }

  @Override
  public Target getTarget() {
    return target;
  }

  @Override
  public ConnectionConfig addHook(IOHook hook) {
    this.hooks.add(hook);
    return this;
  }

  @Override
  public List<IOHook> getHooks() {
    return hooks;
  }

  @Override
  public Selector getSelector() {
    return selector;
  }

  @Override
  public ConnectionConfig setSelector(Selector selector) {
    return customSelect(selector);
  }

  @Override
  public ConnectionConfig roundSelect() {
    this.selector = new RoundRobinSelector();
    return this;
  }

  @Override
  public ConnectionConfig randomSelect() {
    this.selector = new RandomSelector();
    return this;
  }

  @Override
  public ConnectionConfig hashSelect() {
    this.selector = new HashSelector();
    return this;
  }

  @Override
  public ConnectionConfig fairSelect() {
    this.selector = new FairSelector();
    return this;
  }

  @Override
  public ConnectionConfig allSelect() {
    this.selector = new AllSelector();
    return this;
  }

  @Override
  public ConnectionConfig customSelect(Selector selector) {
    if (selector instanceof RoundRobinSelector || selector instanceof RandomSelector || selector instanceof HashSelector
        || selector instanceof FairSelector || selector instanceof AllSelector) {
      this.selector = selector;
    } else {
      this.selector = new CustomSelector(selector);
    }
    return this;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ConnectionConfig)) {
      return false;
    }
    ConnectionConfig connection = (ConnectionConfig) other;
    return connection.getSource().getComponent().equals(source.getComponent())
        && connection.getSource().getPort().equals(source.getPort())
        && connection.getTarget().getComponent().equals(target.getComponent())
        && connection.getTarget().getPort().equals(target.getPort());
  }

  /**
   * Default source implementation.
   * 
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static class DefaultSource implements Source {
    private String component;
    private String port;
    private List<OutputHook> hooks = new ArrayList<>();

    private DefaultSource() {
    }

    @Override
    public String getComponent() {
      return component;
    }

    @Override
    public Source setComponent(String component) {
      this.component = component;
      return this;
    }

    @Override
    public String getPort() {
      return port;
    }

    @Override
    public Source setPort(String port) {
      this.port = port;
      return this;
    }

    @Override
    public Source addHook(OutputHook hook) {
      this.hooks.add(hook);
      return this;
    }

    @Override
    public List<OutputHook> getHooks() {
      return hooks;
    }

  }

  /**
   * Default target implementation.
   * 
   * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
   */
  public static class DefaultTarget implements Target {
    private String component;
    private String port;
    private List<InputHook> hooks = new ArrayList<>();

    private DefaultTarget() {
    }

    @Override
    public String getComponent() {
      return component;
    }

    @Override
    public Target setComponent(String component) {
      this.component = component;
      return this;
    }

    @Override
    public String getPort() {
      return port;
    }

    @Override
    public Target setPort(String port) {
      this.port = port;
      return this;
    }

    @Override
    public Target addHook(InputHook hook) {
      this.hooks.add(hook);
      return this;
    }

    @Override
    public List<InputHook> getHooks() {
      return hooks;
    }

  }

}
