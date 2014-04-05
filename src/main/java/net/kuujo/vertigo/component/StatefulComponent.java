package net.kuujo.vertigo.component;

import net.kuujo.vertigo.state.Snapshot;

import org.vertx.java.core.Handler;

/**
 * The stateful component is a component type that supports periodic
 * persistence of internal state. The component provides an interface
 * for checkpoint creation and installation handlers which may be
 * periodically called by Vertigo depending on the component implementation.
 *
 * @author Jordan Halterman
 */
public interface StatefulComponent extends Component {

  /**
   * Registers a handler to be called when a checkpoint is reached.<p>
   *
   * This handler should persist any internal component state that should be
   * recovered if the component or a transaction fails. State is persisted
   * by simply setting values on the given {@link Snapshot} instance. <b>Data
   * should be provided synchronously</b> since Vertigo will automatically
   * store the snapshot state once the handler has completed. Note also that
   * the snapshot may contain state that was previously persisted which can
   * optionally be removed from the snapshot as well.
   *
   * @param handler A handler to be called at a check point. The handler
   *        will be called with a snapshot object.
   * @return The stateful component.
   */
  StatefulComponent checkpointHandler(Handler<Snapshot> handler);

  /**
   * Registers a handler to be called when a snapshot is installed.<p>
   *
   * This handler should retrieve necessary state from the {@link Snapshot}.
   * When the component dies or a transaction is rolled back, the component
   * state will be reinstalled by calling the install handler.
   *
   * @param handler A handler to be called at snapshot installation. The handler
   *        will be called with a snapshot object.
   * @return The stateful component.
   */
  StatefulComponent installHandler(Handler<Snapshot> handler);

}
