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

package net.kuujo.vertigo.io.port.impl;

import net.kuujo.vertigo.hook.InputHook;
import net.kuujo.vertigo.io.InputInfo;
import net.kuujo.vertigo.io.connection.InputConnectionInfo;
import net.kuujo.vertigo.io.port.InputPortInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Input port info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class InputPortInfoImpl extends BasePortInfoImpl<InputPortInfo> implements InputPortInfo {
  private InputInfo input;
  private List<InputConnectionInfo> connections = new ArrayList<>();
  private List<InputHook> hooks = new ArrayList<>();

  @Override
  public InputInfo input() {
    return input;
  }

  @Override
  public Collection<InputConnectionInfo> connections() {
    return connections;
  }

  @Override
  public List<InputHook> hooks() {
    return hooks;
  }

}
