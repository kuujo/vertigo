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
package net.kuujo.vertigo.cluster.data.impl;

import java.util.List;

import net.kuujo.vertigo.cluster.data.AsyncList;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.shareddata.ConcurrentSharedMap;

/**
 * Shared data based list.
 *
 * @author Jordan Halterman
 *
 * @param <T> The list data type.
 */
public class SharedDataList<T> implements AsyncList<T> {
  private static final String LIST_MAP_NAME = "__LIST__";
  private final String name;
  private final Vertx vertx;
  private final ConcurrentSharedMap<String, String> map;

  public SharedDataList(String name, Vertx vertx) {
    this.name = name;
    this.vertx = vertx;
    this.map = vertx.sharedData().getMap(LIST_MAP_NAME);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public void add(T value) {
    add(value, null);
  }

  @Override
  public void add(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        JsonArray jsonArray = getArray();
        jsonArray.add(value);
        map.put(name, jsonArray.encode());
        new DefaultFutureResult<Boolean>(true).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void remove(T value) {
    remove(value, null);
  }

  @Override
  public void remove(final T value, final Handler<AsyncResult<Boolean>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        List<T> list = getList();
        list.remove(value);
        setList(list);
        new DefaultFutureResult<Boolean>(false).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void remove(int index) {
    remove(index, null);
  }

  @Override
  public void remove(final int index, final Handler<AsyncResult<T>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        List<T> list = getList();
        T value = list.remove(index);
        setList(list);
        new DefaultFutureResult<T>(value).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void contains(final Object value, final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(getList().contains(value)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void size(final Handler<AsyncResult<Integer>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Integer>(getArray().size()).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void isEmpty(final Handler<AsyncResult<Boolean>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<Boolean>(getArray().size() == 0).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void clear() {
    clear(null);
  }

  @Override
  public void clear(final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        map.remove(name);
        new DefaultFutureResult<Void>((Void) null).setHandler(doneHandler);
      }
    });
  }

  @Override
  public void get(final int index, final Handler<AsyncResult<T>> resultHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        new DefaultFutureResult<T>(getList().get(index)).setHandler(resultHandler);
      }
    });
  }

  @Override
  public void set(int index, T value) {
    set(index, value, null);
  }

  @Override
  public void set(final int index, final T value, final Handler<AsyncResult<Void>> doneHandler) {
    vertx.runOnContext(new Handler<Void>() {
      @Override
      public void handle(Void _) {
        List<T> list = getList();
        list.set(index, value);
        setList(list);
      }
    });
  }

  private JsonArray getArray() {
    String array = map.get(name);
    JsonArray jsonArray;
    if (array == null) {
      jsonArray = new JsonArray();
    }
    else {
      jsonArray = new JsonArray(array);
    }
    return jsonArray;
  }

  @SuppressWarnings("unchecked")
  private List<T> getList() {
    return getArray().toList();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setList(List list) {
    map.put(name, new JsonArray(list).encode());
  }

}
