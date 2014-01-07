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
package net.kuujo.vertigo.network;

import net.kuujo.vertigo.input.grouping.Grouping;

/**
 * Network component input.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("deprecation")
public class Input extends net.kuujo.vertigo.input.Input {

  protected Input() {
  }

  public Input(String address) {
    super(address);
  }

  public Input(String address, String stream) {
    super(address, stream);
  }

  public Input(String address, Grouping grouping) {
    super(address, grouping);
  }

  public Input(String address, String stream, Grouping grouping) {
    super(address, stream, grouping);
  }

  @Override
  public String id() {
    return super.id();
  }

  @Override
  public int getCount() {
    return super.getCount();
  }

  @Override
  public Input setCount(int count) {
    super.setCount(count);
    return this;
  }

  @Override
  public String getAddress() {
    return super.getAddress();
  }

  @Override
  public String getStream() {
    return super.getStream();
  }

  @Override
  public Input setStream(String stream) {
    super.setStream(stream);
    return this;
  }

  @Override
  public Input groupBy(Grouping grouping) {
    super.groupBy(grouping);
    return this;
  }

  @Override
  public Input groupBy(String grouping) {
    super.groupBy(grouping);
    return this;
  }

  @Override
  public Input randomGrouping() {
    super.randomGrouping();
    return this;
  }

  @Override
  public Input roundGrouping() {
    super.roundGrouping();
    return this;
  }

  @Override
  public Input fieldsGrouping(String... fields) {
    super.fieldsGrouping(fields);
    return this;
  }

  @Override
  public Input allGrouping() {
    super.allGrouping();
    return this;
  }

  @Override
  public Grouping getGrouping() {
    return super.getGrouping();
  }

  @Override
  public String toString() {
    return getAddress();
  }

}
