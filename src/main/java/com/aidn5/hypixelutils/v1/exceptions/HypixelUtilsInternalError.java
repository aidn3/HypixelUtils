
package com.aidn5.hypixelutils.v1.exceptions;

/**
 * Indicates that the library has encountered an internal error.
 * 
 * 
 * @author aidn5
 * @version 1.0
 * @since 1.0
 * @category Exception
 */
public class HypixelUtilsInternalError extends RuntimeException {
  public HypixelUtilsInternalError() {
    super();
  }

  public HypixelUtilsInternalError(String message) {
    super(message);
  }

  public HypixelUtilsInternalError(String message, Throwable cause) {
    super(message, cause);
  }

  public HypixelUtilsInternalError(Throwable cause) {
    super(cause);
  }
}
