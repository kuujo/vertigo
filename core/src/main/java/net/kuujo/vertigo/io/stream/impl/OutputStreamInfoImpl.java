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

package net.kuujo.vertigo.io.stream.impl;

import net.kuujo.vertigo.impl.BaseTypeInfoImpl;
import net.kuujo.vertigo.io.connection.OutputConnectionInfo;
import net.kuujo.vertigo.io.partitioner.Partitioner;
import net.kuujo.vertigo.io.port.OutputPortInfo;
import net.kuujo.vertigo.io.stream.OutputStreamInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Output stream info implementation.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class OutputStreamInfoImpl extends BaseTypeInfoImpl<OutputStreamInfo> implements OutputStreamInfo {
  private OutputPortInfo port;
  private Partitioner partitioner;
  private List<OutputConnectionInfo> connections = new ArrayList<>();

  @Override
  public OutputPortInfo port() {
    return port;
  }

  @Override
  public Partitioner partitioner() {
    return partitioner;
  }

  @Override
  public List<OutputConnectionInfo> connections() {
    return connections;
  }

}
