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
  public String id();

  /**
   * Creates an output selector from the grouping.
   *
   * @return
   *   An output selector.
   */
  public Selector createSelector();

}
