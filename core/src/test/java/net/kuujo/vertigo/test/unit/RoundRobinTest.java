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
package net.kuujo.vertigo.test.unit;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.kuujo.vertigo.util.RoundRobin;

import org.junit.Test;

/**
 * Round-robin tests.
 *
 * @author Jordan Halterman
 */
public class RoundRobinTest {

  @Test
  public void testRoundRobin() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    RoundRobin<Integer> round = new RoundRobin<Integer>(list);
    Iterator<Integer> iter = round.iterator();
    int first = iter.next();
    assertEquals(1, first);
    int second = iter.next();
    assertEquals(2, second);
    int third = iter.next();
    assertEquals(3, third);
    int fourth = iter.next();
    assertEquals(1, fourth);
    int fifth = iter.next();
    assertEquals(2, fifth);
    int sixth = iter.next();
    assertEquals(3, sixth);
  }

}
