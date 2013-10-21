package net.kuujo.vertigo.output;

/**
 * A message dealer.
 *
 * @author Jordan Halterman
 */
public interface Dealer {

  /**
   * Returns the dealer address.
   *
   * @return
   *   The dealer address.
   */
  public String address();

  /**
   * Starts the dealer.
   *
   * @return
   *   The called dealer instance.
   */
  public Dealer start();
}
