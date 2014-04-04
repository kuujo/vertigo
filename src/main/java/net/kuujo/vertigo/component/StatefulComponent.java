package net.kuujo.vertigo.component;

import net.kuujo.vertigo.state.Snapshot;

import org.vertx.java.core.Handler;

/**
 * Stateful network component.
 *
 * @author Jordan Halterman
 */
public interface StatefulComponent extends Component {

  /**
   * Registers a handler to be called when a checkpoint is reached.
   *
   * @param handler A handler to be called at a check point. The handler
   *        will be called with a snapshot object.
   * @return The stateful component.
   */
  StatefulComponent checkpointHandler(Handler<Snapshot> handler);

  /**
   * Registers a handler to be called when a snapshot is installed.
   *
   * @param handler A handler to be called at snapshot installation. The handler
   *        will be called with a snapshot object.
   * @return The stateful component.
   */
  StatefulComponent installHandler(Handler<Snapshot> handler);

}
