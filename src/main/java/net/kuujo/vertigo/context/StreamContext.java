package net.kuujo.vertigo.context;

/**
 * Stream context.
 *
 * @author Jordan Halterman
 *
 * @param <T>
 */
public abstract class StreamContext<T extends StreamContext<T>> extends Context<T> {
}
