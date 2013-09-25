package net.kuujo.vitis;

import org.vertx.java.core.VertxException;

/**
 * A root exception.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class VitisException extends VertxException {

  public VitisException(String message) {
    super(message);
  }

  public VitisException(String message, Throwable cause) {
    super(message, cause);
  }

  public VitisException(Throwable cause) {
    super(cause);
  }

}
