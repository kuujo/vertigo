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
package net.kuujo.vertigo.cluster.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MultiMap;
import com.hazelcast.monitor.LocalMultiMapStats;

/**
 * Vert.x based cluster data.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
class VertxClusterData implements ClusterData {
  private final Vertx vertx;

  public VertxClusterData(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public <K, V> MultiMap<K, V> getMultiMap(String name) {
    return new SharedDataMultiMap<K, V>(vertx.sharedData().<K, String>getMap(String.format("__map.%s", name)));
  }

  @Override
  public <K, V> Map<K, V> getMap(String name) {
    return vertx.sharedData().getMap(name);
  }

  @Override
  public <T> Set<T> getSet(String name) {
    return vertx.sharedData().getSet(name);
  }

  @Override
  public <T> List<T> getList(String name) {
    return new SharedDataList<T>(vertx.sharedData().<Integer, Object>getMap(String.format("__list.%s", name)));
  }

  @Override
  public <T> Queue<T> getQueue(String name) {
    return new SharedDataQueue<T>(vertx.sharedData().<Integer, Object>getMap(String.format("__queue.%s", name)));
  }

  private static class SharedDataMultiMap<K, V> implements MultiMap<K, V> {
    private final Map<K, String> map;

    public SharedDataMultiMap(Map<K, String> map) {
      this.map = map;
    }

    @Override
    public void destroy() {
      map.clear();
    }

    @Override
    public Object getId() {
      return null;
    }

    @Override
    public String getPartitionKey() {
      throw new UnsupportedOperationException("getPartitionKey not supported.");
    }

    @Override
    public String getServiceName() {
      throw new UnsupportedOperationException("getServiceName not supported.");
    }

    @Override
    public String addEntryListener(EntryListener<K, V> arg0, boolean arg1) {
      throw new UnsupportedOperationException("addEntryListener not supported.");
    }

    @Override
    public String addEntryListener(EntryListener<K, V> arg0, K arg1, boolean arg2) {
      throw new UnsupportedOperationException("addEntryListener not supported.");
    }

    @Override
    public String addLocalEntryListener(EntryListener<K, V> arg0) {
      throw new UnsupportedOperationException("addLocalEntryListener not supported.");
    }

    @Override
    public void clear() {
      map.clear();
    }

    @Override
    public boolean containsEntry(K key, V value) {
      String sdata = map.get(key);
      if (sdata == null) {
        return false;
      }
      JsonArray data = new JsonArray(sdata);
      return data.contains(value);
    }

    @Override
    public boolean containsKey(K key) {
      return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
      return map.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException("entrySet not supported.");
    }

    @Override
    public void forceUnlock(K key) {
      throw new UnsupportedOperationException("forceUnlock not supported.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> get(K key) {
      String sdata = map.get(key);
      if (sdata == null) {
        return null;
      }
      JsonArray data = new JsonArray(sdata);
      return data.toList();
    }

    @Override
    public LocalMultiMapStats getLocalMultiMapStats() {
      throw new UnsupportedOperationException("getLocalMultiMapStats not supported.");
    }

    @Override
    public String getName() {
      throw new UnsupportedOperationException("getName not supported.");
    }

    @Override
    public boolean isLocked(K key) {
      return false;
    }

    @Override
    public Set<K> keySet() {
      return map.keySet();
    }

    @Override
    public Set<K> localKeySet() {
      return map.keySet();
    }

    @Override
    public void lock(K key) {
      throw new UnsupportedOperationException("lock not supported.");
    }

    @Override
    public void lock(K key, long timeout, TimeUnit timeUnit) {
      throw new UnsupportedOperationException("lock not supported.");
    }

    @Override
    public boolean put(K key, V value) {
      synchronized (map) {
        String sdata = map.get(key);
        JsonArray data = sdata != null ? new JsonArray(sdata) : new JsonArray();
        data.add(value);
        map.put(key, data.encode());
      }
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> remove(Object key) {
      synchronized (map) {
        String sdata = map.remove(key);
        if (sdata == null) {
          return null;
        }
        JsonArray data = new JsonArray(sdata);
        return data.toList();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object key, Object value) {
      synchronized (map) {
        String sdata = map.get(key);
        if (sdata == null) {
          return false;
        }
        JsonArray data = new JsonArray(sdata);
        if (data.contains(value)) {
          JsonArray newData = new JsonArray();
          boolean removed = false;
          for (Object item : data) {
            if (!removed) {
              if (!item.equals(value)) {
                newData.add(item);
              } else {
                removed = true;
              }
            } else {
              newData.add(item);
            }
          }
          if (newData.size() > 0) {
            map.put((K) key, newData.encode());
          } else {
            map.remove((K) key);
          }
          return true;
        }
        return false;
      }
    }

    @Override
    public boolean removeEntryListener(String registrationId) {
      throw new UnsupportedOperationException("removeEntryListener not supported.");
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean tryLock(K key) {
      throw new UnsupportedOperationException("tryLock not supported.");
    }

    @Override
    public boolean tryLock(K key, long timeout, TimeUnit timeUnit) throws InterruptedException {
      throw new UnsupportedOperationException("tryLock not supported.");
    }

    @Override
    public void unlock(K key) {
      throw new UnsupportedOperationException("unlock not supported.");
    }

    @Override
    public int valueCount(K key) {
      String sdata = map.get(key);
      JsonArray data = sdata != null ? new JsonArray(sdata) : new JsonArray();
      return data.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
      List<V> values = new ArrayList<>();
      for (Map.Entry<K, String> entry : map.entrySet()) {
        values.addAll(new JsonArray(entry.getValue()).toList());
      }
      return values;
    }
    
  }

  private static class SharedDataList<T> implements List<T> {
    private final Map<Integer, Object> map;
    private int currentSize = 0;
  
    public SharedDataList(Map<Integer, Object> map) {
      this.map = map;
      this.currentSize = (int) (map.containsKey(-1) ? map.get(-1) : 0);
    }
  
    @Override
    public int size() {
      return currentSize;
    }
  
    @Override
    public boolean isEmpty() {
      return currentSize == 0;
    }
  
    @Override
    public boolean contains(Object o) {
      return map.values().contains(o);
    }
  
    @Override
    public Iterator<T> iterator() {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    public boolean containsAll(Collection<?> c) {
      return map.values().containsAll(c);
    }
  
    @Override
    public boolean addAll(Collection<? extends T> c) {
      for (T value : c) {
        add(value);
      }
      return true;
    }
  
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
      int i = index;
      for (T value : c) {
        add(i, value);
        i++;
      }
      return true;
    }
  
    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
      if (index > currentSize-1) {
        throw new IndexOutOfBoundsException("Index out of bounds.");
      } else {
        return (T) map.get(index);
      }
    }
  
    @Override
    public boolean add(T e) {
      map.put(currentSize, e);
      currentSize++;
      map.put(-1, currentSize);
      return true;
    }
  
    @Override
    public void add(int index, T element) {
      map.put(currentSize, element);
      currentSize++;
      map.put(-1, currentSize);
    }
  
    @Override
    public int indexOf(Object o) {
      for (int i = 0; i < currentSize; i++) {
        if (map.get(i).equals(o)) {
          return i;
        }
      }
      return -1;
    }
  
    @Override
    public int lastIndexOf(Object o) {
      for (int i = currentSize-1; i > 0; i--) {
        if (map.get(i).equals(o)) {
          return i;
        }
      }
      return -1;
    }
  
    @Override
    public ListIterator<T> listIterator() {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    public ListIterator<T> listIterator(int index) {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException("Not supported.");
    }
  
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
      synchronized (map) {
        for (int i = 0; i < currentSize; i++) {
          T value = (T) map.get(i);
          if (value != null && value.equals(o)) {
            map.remove(i);
            currentSize--;
            i++;
            while (map.containsKey(i)) {
              map.put(i-1, map.remove(i));
              i++;
            }
            return true;
          }
        }
        return false;
      }
    }
  
    @Override
    public void clear() {
      map.clear();
      currentSize = 0;
      map.put(-1, currentSize);
    }
  
    @Override
    @SuppressWarnings("unchecked")
    public T set(int index, T element) {
      return (T) map.put(index, element);
    }
  
    @Override
    @SuppressWarnings("unchecked")
    public T remove(int index) {
      if (index > currentSize-1) {
        throw new IndexOutOfBoundsException("Index out of bounds.");
      } else {
        synchronized (map) {
          T value = (T) map.remove(index);
          int i = index+1;
          while (map.containsKey(i)) {
            map.put(i-1, map.remove(i));
            i++;
          }
          currentSize--;
          map.put(-1, currentSize);
          return value;
        }
      }
    }
  
  }

  private static class SharedDataQueue<T> implements Queue<T> {
    private final Map<Integer, Object> map;
    private int currentIndex;

    public SharedDataQueue(Map<Integer, Object> map) {
      this.map = map;
      this.currentIndex = (int) (map.containsKey(-1) ? map.get(-1) : 0);
    }

    @Override
    public int size() {
      return map.size() - 1;
    }

    @Override
    public boolean isEmpty() {
      return map.size() == 1;
    }

    @Override
    public boolean contains(Object o) {
      return map.values().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object[] toArray() {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] a) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return map.values().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove() {
      synchronized (map) {
        if (map.containsKey(currentIndex)) {
          T value = (T) map.remove(currentIndex);
          currentIndex++;
          map.put(-1, currentIndex);
          return value;
        } else {
          throw new IllegalStateException("Queue is empty.");
        }
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T poll() {
      T value = (T) map.remove(currentIndex);
      if (value != null) {
        currentIndex++;
        map.put(-1, currentIndex);
      }
      return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T element() {
      T value = (T) map.get(currentIndex);
      if (value != null) {
        return value;
      } else {
        throw new IllegalStateException("Queue is empty.");
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T peek() {
      return (T) map.get(currentIndex);
    }

    @Override
    public boolean offer(T e) {
      int index = currentIndex + map.size() - 1;
      map.put(index, e);
      return true;
    }

    @Override
    public boolean add(T e) {
      int index = currentIndex + map.size() - 1;
      map.put(index, e);
      return true;
    }

    @Override
    public boolean remove(Object o) {
      synchronized (map) {
        Iterator<Map.Entry<Integer, Object>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
          Map.Entry<Integer, Object> entry = iter.next();
          if (entry.getValue().equals(o)) {
            iter.remove();
            int index = entry.getKey()+1;
            while (map.containsKey(index)) {
              map.put(index-1, map.remove(index));
              index++;
            }
            return true;
          }
        }
        return false;
      }
    }

    @Override
    public void clear() {
      map.clear();
      map.put(-1, currentIndex);
    }

  }

}
