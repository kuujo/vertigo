/*
 * Copyright 2013-2014 the original author or authors.
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
package net.kuujo.vertigo.log;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Component event log.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public interface EventLog {

  /**
   * Appends an entry to the log.
   *
   * @param entry The entry to append.
   * @return The appended entry's index.
   */
  long appendEntry(Entry entry);

  /**
   * Appends a list of entries to the log.
   *
   * @param entries The entries to append.
   * @return A list of indices for the appended entries,
   */
  List<Long> appendEntries(Entry... entries);

  /**
   * Appends a list of entries to the log.
   *
   * @param entries The entries to append.
   * @return A list of indices for the appended entries,
   */
  List<Long> appendEntries(Collection<Entry> entries);

  /**
   * Gets an entry by index.
   *
   * @param index The index of the entry to get.
   * @param <T> The entry type.
   * @return The entry at the given index.
   */
  <T extends Entry> T getEntry(long index);

  /**
   * Gets a list of entries by index.
   *
   * @param from The starting index from which to get entries.
   * @param to The ending index from which to get entries.
   * @param <T> The entry type.
   * @return A collection of entries at the given index.
   */
  <T extends Entry> Collection<T> getEntries(long from, long to);

  /**
   * Compacts the log, removing all entries up to the given index.
   *
   * @param index The index before which to remove entries.
   */
  void compact(long index);

  /**
   * Replays log entries to the given consumer.
   *
   * @param consumer The consumer to which to replay entries.
   */
  <T extends Entry> void replay(Consumer<T> consumer);

  /**
   * Replays entries of the given type to the consumer.
   *
   * @param consumer The consumer to which to replay entries.
   * @param entryType The type of entries to replay.
   * @param <T> The entry type.
   */
  <T extends Entry> void replay(Consumer<T> consumer, Class<T> entryType);

}
