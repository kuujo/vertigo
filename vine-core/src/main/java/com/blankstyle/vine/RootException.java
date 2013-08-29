package com.blankstyle.vine;

import org.vertx.java.core.VertxException;

/**
 * A root exception.
 *
 * @author Jordan Halterman
 */
@SuppressWarnings("serial")
public class RootException extends VertxException {

  public RootException(String message) {
    super(message);
  }

  public RootException(String message, Throwable cause) {
    super(message, cause);
  }

  public RootException(Throwable cause) {
    super(cause);
  }

}
