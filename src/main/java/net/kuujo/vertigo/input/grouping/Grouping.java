package net.kuujo.vertigo.input.grouping;

import net.kuujo.vertigo.output.selector.Selector;
import net.kuujo.vertigo.serializer.Serializable;

/**
 * An input grouping.
 *
 * @author Jordan Halterman
 */
public interface Grouping extends Serializable {

  /**
   * Returns the grouping identifier.
   *
   * @return
   *   The unique grouping identifier.
   */
  String id();

  /**
   * Returns the grouping count.
   *
   * @return
   *   The grouping count.
   */
  int count();

  /**
   * Sets the grouping count.
   *
   * @param count
   *   The grouping count.
   * @return
   *   The called grouping instance.
   */
  Grouping setCount(int count);

  /**
   * Creates an output selector from the grouping.
   *
   * @return
   *   An output selector.
   */
  Selector createSelector();

}
