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
package com.blankstyle.vine.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A random dispatcher implementation.
 *
 * @author Jordan Halterman
 */
public class RandomDispatcher implements Dispatcher {

  private Map<Double, Channel> channelMap;

  private int channelCount;

  @Override
  public void init(Collection<Channel> channels) {
    channelMap = new HashMap<Double, Channel>();
    Iterator<Channel> iter = channels.iterator();
    int i = 0;
    while (iter.hasNext()) {
      channelMap.put(Double.valueOf(i++), iter.next());
    }
    channelCount = channels.size();
  }

  @Override
  public <T> void dispatch(Message<T> message) {
    channelMap.get(Math.random() * channelCount).send(message);
  }

}
