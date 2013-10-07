package net.kuujo.vertigo;

import net.kuujo.vertigo.filter.FieldFilter;
import net.kuujo.vertigo.filter.SourceFilter;
import net.kuujo.vertigo.filter.TagsFilter;

/**
 * Static helper for creating filter definitions.
 *
 * @author Jordan Halterman
 */
public final class Filters {

  /**
   * Creates a new tags filter.
   *
   * @param tags
   *   A list of tags by which to filter.
   * @return
   *   A new tags filter instance.
   */
  public static TagsFilter tag(String... tags) {
    return new TagsFilter(tags);
  }

  /**
   * Creates a new source filter.
   *
   * @param source
   *   A source by which to filter.
   * @return
   *   A new source filter instance.
   */
  public static SourceFilter source(String source) {
    return new SourceFilter(source);
  }

  /**
   * Creates a new field filter.
   *
   * @param fieldName
   *   The name of the field by which to filter.
   * @param value
   *   The value by which to filter.
   * @return
   *   A new field filter instance.
   */
  public static FieldFilter field(String fieldName, Object value) {
    return new FieldFilter(fieldName, value);
  }

}
